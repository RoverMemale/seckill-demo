package com.seckill.service.impl;

import com.seckill.entity.SeckillGoods;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.mq.SeckillMessage;
import com.seckill.service.ISeckillService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.seckill.config.RabbitMQConfig;

import java.util.Date;

@Service
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public boolean seckill(Long userId, Long goodsId) {
        // 1. 判断秒杀时间（从数据库查询秒杀商品信息）
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(goodsId);
        if (seckillGoods == null) {
            return false;
        }
        Date now = new Date();
        if (now.before(seckillGoods.getStartDate()) || now.after(seckillGoods.getEndDate())) {
            return false; // 不在秒杀时间范围内
        }

        // 2. 判断是否重复抢购（基于 Redis）
        String userGoodsKey = "seckill:user:" + goodsId + ":" + userId;
        Boolean hasBuy = redisTemplate.opsForValue().setIfAbsent(userGoodsKey, "1");
        if (Boolean.FALSE.equals(hasBuy)) {
            return false; // 已经抢购过
        }
        // 设置过期时间（1小时，防止内存无限增长）
        redisTemplate.expire(userGoodsKey, 3600, java.util.concurrent.TimeUnit.SECONDS);

        // 3. Redis 预减库存（原子操作）
        Long stock = redisTemplate.opsForValue().decrement("seckill:stock:" + goodsId);
        if (stock < 0) {
            // 库存不足，恢复抢购标志（因为未成功）
            redisTemplate.delete(userGoodsKey);
            return false;
        }

        // 4. 发送 MQ 消息，异步下单
        SeckillMessage message = new SeckillMessage(userId, goodsId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_QUEUE, message);

        return true;
    }
}