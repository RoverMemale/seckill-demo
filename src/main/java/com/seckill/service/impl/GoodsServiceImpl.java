package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.dto.GoodsVo;
import com.seckill.entity.Goods;
import com.seckill.entity.SeckillGoods;
import com.seckill.mapper.GoodsMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.service.IGoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<GoodsVo> listGoodsVo() {
        List<Goods> goodsList = goodsMapper.selectList(null);
        List<GoodsVo> result = new ArrayList<>();
        for (Goods goods : goodsList) {
            GoodsVo vo = new GoodsVo();
            BeanUtils.copyProperties(goods, vo);
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillGoods::getGoodsId, goods.getId());
            SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(wrapper);
            if (seckillGoods != null) {
                vo.setSeckillPrice(seckillGoods.getSeckillPrice());
                vo.setStockCount(seckillGoods.getStockCount());
                vo.setStartDate(seckillGoods.getStartDate());
                vo.setEndDate(seckillGoods.getEndDate());
            }
            result.add(vo);
        }
        return result;
    }
}