package com.seckill.service;

import com.seckill.entity.User;

public interface IUserService {
    User getByNickname(String nickname);
    boolean register(String nickname, String password);
}