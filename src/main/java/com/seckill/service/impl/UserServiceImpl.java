package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.IUserService;
import com.seckill.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getByNickname(String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNickname, nickname);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public boolean register(String nickname, String password) {
        // 检查昵称是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNickname, nickname);
        if (userMapper.selectCount(wrapper) > 0) {
            return false; // 昵称已存在
        }

        // 生成随机盐
        String salt = MD5Util.generateSalt();
        // 加密密码：MD5(密码 + 盐)
        String encryptedPwd = MD5Util.md5(password + salt);

        User user = new User();
        user.setNickname(nickname);
        user.setPassword(encryptedPwd);
        user.setSalt(salt);
        user.setRegisterDate(new Date());
        user.setLastLoginDate(new Date());
        user.setLoginCount(0);

        return userMapper.insert(user) > 0;
    }
}