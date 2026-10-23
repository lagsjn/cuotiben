package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cuotiben.common.BusinessException;
import com.cuotiben.config.TokenStore;
import com.cuotiben.dto.AuthDtos;
import com.cuotiben.entity.Subject;
import com.cuotiben.entity.User;
import com.cuotiben.mapper.SubjectMapper;
import com.cuotiben.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 注册登录服务。
 */
@Service
public class AuthService {

    /** 新用户默认学科，让「录入错题」第一次就有选项 */
    private static final List<String[]> DEFAULT_SUBJECTS = List.of(
            new String[]{"高等数学", "#2c4bd6"},
            new String[]{"线性代数", "#5b8def"},
            new String[]{"概率论", "#8f7bd8"},
            new String[]{"数据结构", "#2f9e70"},
            new String[]{"大学英语", "#c0894f"}
    );

    private final UserMapper userMapper;
    private final SubjectMapper subjectMapper;
    private final TokenStore tokenStore;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper, SubjectMapper subjectMapper, TokenStore tokenStore) {
        this.userMapper = userMapper;
        this.subjectMapper = subjectMapper;
        this.tokenStore = tokenStore;
    }

    @Transactional
    public AuthDtos.LoginResult register(AuthDtos.RegisterRequest request) {
        Long sameName = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username()));
        if (sameName != null && sameName > 0) {
            throw new BusinessException(40010, "用户名已被占用");
        }
        Long sameEmail = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.email()));
        if (sameEmail != null && sameEmail > 0) {
            throw new BusinessException(40011, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(isNotBlank(request.nickname()) ? request.nickname().trim() : request.username().trim());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        initDefaultSubjects(user.getId());

        String token = tokenStore.issue(user.getId());
        return new AuthDtos.LoginResult(token, toVO(user));
    }

    public AuthDtos.LoginResult login(AuthDtos.LoginRequest request) {
        String account = request.account().trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or()
                .eq(User::getEmail, account));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(40101, "用户名或密码不正确");
        }
        return new AuthDtos.LoginResult(tokenStore.issue(user.getId()), toVO(user));
    }

    public AuthDtos.UserVO currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40102, "用户不存在");
        }
        return toVO(user);
    }

    public void logout(String token) {
        tokenStore.revoke(token);
    }

    private void initDefaultSubjects(Long userId) {
        int sort = 0;
        for (String[] item : DEFAULT_SUBJECTS) {
            Subject subject = new Subject();
            subject.setUserId(userId);
            subject.setName(item[0]);
            subject.setColor(item[1]);
            subject.setSortNo(sort++);
            subject.setCreatedAt(LocalDateTime.now());
            subjectMapper.insert(subject);
        }
    }

    private AuthDtos.UserVO toVO(User user) {
        return new AuthDtos.UserVO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getIdentity(),
                user.getGoal());
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
