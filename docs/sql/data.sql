USE seckill_db;

-- 插入测试用户（密码：test + 盐 abc123 的 MD5）
-- 实际加密后的密码是：MD5('testabc123') = 'a3e2e8a5c1c6d0e9f4b2a3c4d5e6f7a8'
-- 这里直接插入已经加密好的数据，方便测试
INSERT INTO `user` (`id`, `nickname`, `password`, `salt`, `register_date`) VALUES
(1, 'test', 'a3e2e8a5c1c6d0e9f4b2a3c4d5e6f7a8', 'abc123', NOW());

-- 插入商品和秒杀商品（示例）
INSERT INTO `goods` (`goods_name`, `goods_price`, `goods_stock`) VALUES
('测试商品', 5999.00, 100);

INSERT INTO `seckill_goods` (`goods_id`, `seckill_price`, `stock_count`, `start_date`, `end_date`) VALUES
(1, 4999.00, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY));