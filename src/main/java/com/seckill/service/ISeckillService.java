package com.seckill.service;

public interface ISeckillService {
    /**
     * 秒杀
     * @param userId 用户ID
     * @param goodsId 秒杀商品ID（seckill_goods.id）
     * @return true: 秒杀请求已排队，false: 秒杀失败（库存不足、重复抢购等）
     */
    boolean seckill(Long userId, Long goodsId);
}