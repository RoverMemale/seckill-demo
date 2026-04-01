package com.seckill;

import com.seckill.entity.SeckillGoods;
import com.seckill.mapper.SeckillGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

@SpringBootApplication
@MapperScan("com.seckill.mapper")   // 确保这行存在，让 Spring 扫描 mapper 接口
public class SecKillApplication implements CommandLineRunner {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 项目启动时，将所有秒杀商品的库存加载到 Redis 中
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(null);
        for (SeckillGoods sg : seckillGoodsList) {
            redisTemplate.opsForValue().set("seckill:stock:" + sg.getGoodsId(), sg.getStockCount());
            System.out.println("初始化 Redis 库存: 商品ID " + sg.getGoodsId() + " -> " + sg.getStockCount());
        }
    }
}