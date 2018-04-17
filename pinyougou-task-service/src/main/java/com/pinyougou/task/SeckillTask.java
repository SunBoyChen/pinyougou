package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时任务类
 */
@Component
public class SeckillTask {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron="0 * * * * ?")
    public void refreshSeckillGoods(){

        //System.out.println( new Date() + "执行了！");

        //获取缓存中所有的商品id集合
        ArrayList ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        //System.out.println("查询"+ids);
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        //criteria.andIdNotIn(ids);    //排除缓存中已经有的商品
        if(ids.size()>0){
            criteria.andIdNotIn(ids);//排除缓存中已经存在的商品ID集合
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
       // System.out.println("查询复合要求的秒杀商品");
        for (TbSeckillGoods t:seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(t.getId(),t);
        }
    }

    /**
     * 移除秒杀商品
     */
    @Scheduled(cron="* * * * * ?")
    public void removeSeckillGoods(){
        //获取缓存中所有的商品id集合
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        //System.out.println("移除" + seckillGoods);
        for(TbSeckillGoods t : seckillGoods) {
            if(new Date().getTime() > t.getEndTime().getTime()) {  //当前时间大于结束时间
                //保存到数据库
                seckillGoodsMapper.updateByPrimaryKey(t);
                //从缓存删除
                redisTemplate.boundHashOps("seckillGoods").delete(t.getId());
                System.out.println("移除秒杀商品"+t.getId());
            }
        }
        System.out.println("移除秒杀商品任务结束");
    }
}
