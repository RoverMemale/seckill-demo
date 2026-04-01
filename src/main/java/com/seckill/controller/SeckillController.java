package com.seckill.controller;

import com.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SeckillController {

    @Autowired
    private ISeckillService seckillService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/seckill/{goodsId}")
    public Map<String, Object> seckill(@PathVariable("goodsId") Long goodsId,
                                       HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        // 从拦截器存入的 userId 获取
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("code", 401);
            result.put("msg", "未登录");
            return result;
        }

        boolean success = seckillService.seckill(userId, goodsId);
        if (success) {
            result.put("code", 0);
            result.put("msg", "秒杀排队中");
        } else {
            result.put("code", 500);
            result.put("msg", "秒杀失败（库存不足或重复抢购）");
        }
        return result;
    }

    @GetMapping("/seckill/result/{goodsId}")
    public Map<String, Object> getSeckillResult(@PathVariable("goodsId") Long goodsId,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        String orderNo = (String) redisTemplate.opsForValue().get("seckill:result:" + userId + ":" + goodsId);
        if (orderNo != null) {
            result.put("code", 0);
            result.put("msg", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", orderNo);
            data.put("status", 1);
            result.put("data", data);
        } else {
            Boolean hasKey = redisTemplate.hasKey("seckill:user:" + goodsId + ":" + userId);
            if (Boolean.TRUE.equals(hasKey)) {
                result.put("code", 0);
                result.put("msg", "排队中");
                Map<String, Object> data = new HashMap<>();
                data.put("status", 0);
                result.put("data", data);
            } else {
                result.put("code", 500);
                result.put("msg", "秒杀失败");
            }
        }
        return result;
    }
}