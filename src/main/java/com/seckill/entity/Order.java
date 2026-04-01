package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("`order`")   // 注意：反引号包裹
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private String goodsName;
    private BigDecimal goodsPrice;
    private Integer orderStatus;
    private Date createDate;
    private Date payDate;
}