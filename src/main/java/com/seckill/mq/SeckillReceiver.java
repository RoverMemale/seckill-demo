package com.seckill.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillGoods;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.seckill.config.RabbitMQConfig;

import java.util.Date;
import java.util.UUID;

@Component
public class SeckillReceiver {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillMessage(SeckillMessage message) {
        try {
            Long userId = message.getUserId();
            Long goodsId = message.getGoodsId(); // seckill_goods.id

            // 1. 扣减数据库库存（乐观锁）
            SeckillGoods seckillGoods = seckillGoodsMapper.selectById(goodsId);
            if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
                // 库存不足，删除 Redis 中该用户的抢购记录
                redisTemplate.delete("seckill:user:" + goodsId + ":" + userId);
                return;
            }

            // 使用乐观锁更新库存
            int update = seckillGoodsMapper.reduceStock(goodsId);
            if (update <= 0) {
                // 更新失败，库存不足
                redisTemplate.delete("seckill:user:" + goodsId + ":" + userId);
                return;
            }

            // 2. 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setGoodsId(seckillGoods.getGoodsId()); // 普通商品ID
            // 获取商品名称（可进一步查询，简化处理）
            order.setGoodsName("秒杀商品");
            order.setGoodsPrice(seckillGoods.getSeckillPrice());
            order.setOrderStatus(0);
            order.setCreateDate(new Date());
            orderMapper.insert(order);

            // 3. 将秒杀成功的结果存入 Redis，供前端轮询查询
            redisTemplate.opsForValue().set("seckill:result:" + userId + ":" + goodsId, order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();  // 打印完整异常堆栈
            // 可选：记录日志后重新抛出，让 Spring 重试
            throw e;
        }
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}