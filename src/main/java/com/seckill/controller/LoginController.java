package com.seckill.controller;

import com.seckill.dto.LoginDto;
import com.seckill.entity.User;
import com.seckill.service.IUserService;
import com.seckill.util.JwtUtil;
import com.seckill.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody LoginDto loginDto) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.register(loginDto.getNickname(), loginDto.getPassword());
        if (success) {
            result.put("code", 0);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 500);
            result.put("msg", "昵称已存在");
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDto loginDto) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getByNickname(loginDto.getNickname());
        if (user == null) {
            result.put("code", 500);
            result.put("msg", "用户不存在");
            return result;
        }
        String encryptPwd = MD5Util.md5(loginDto.getPassword() + user.getSalt());
        if (!encryptPwd.equals(user.getPassword())) {
            result.put("code", 500);
            result.put("msg", "密码错误");
            return result;
        }
        String token = JwtUtil.generateToken(user.getId());
        result.put("code", 0);
        result.put("msg", "登录成功");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        result.put("data", data);
        return result;
    }
}