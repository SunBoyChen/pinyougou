package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map=new HashMap<>();
        /*Query query=new SimpleQuery();
        //添加查询条件  item_keywords 复制域
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());*/

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));


        //1.查询列表
        map.putAll(searchList(searchMap));
        //2查询分类列表
        map.putAll(searchCategoryList(searchMap));
        //3查询品牌和规格列表
        List list = (List) map.get("categoryList"); //分类列表

        String categoryName = (String) searchMap.get("category");
        if(!"".equals(categoryName)){
            //当传递分类时，我们应该查询该分类的规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if(!list.isEmpty()){
                //默认查询第一个分类的
                map.putAll(searchBrandAndSpecList((String) list.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    /**
     * 查询分类列表 需要用到分组查询
     * @param searchMap
     * @return
     */
    private  Map searchCategoryList(Map searchMap){
        Map map = new HashMap();

        List list = new ArrayList();

        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOption = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOption);

       /* Field field = new SimpleField("item_category");
        query.addGroupByField(field);*/

        //得到分组页
        GroupPage<TbItem> GroupPage = solrTemplate.queryForGroupPage(query, TbItem.class);


        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = GroupPage.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        for (GroupEntry<TbItem> entry: content) {
            String value = entry.getGroupValue();
            list.add(value);
        }
        map.put("categoryList",list);
        return map;
    }

    /**
     * 查询列表
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {

        HashMap map = new HashMap<>();;

        //创建一个高亮查询对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        //创建一个高亮选择对象 并添加高亮域item_title 此处可以添加多个高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        highlightQuery.setHighlightOptions(highlightOptions);


        //1.1添加查询条件 (关键字查询)
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);

        // 1.2按分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //1.3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //1.4按规格筛选
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String str: specMap.keySet()) {
                Criteria filterCriteria=new Criteria("item_spec_" + str).is(specMap.get(str));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //1.5按照价格查询
        if (!"".equals(searchMap.get("price"))) {
            String price = (String) searchMap.get("price");
            //使用 - 对价格进行分割
            String[] split = price.split("-");

            if(!"0".equals(split[0])){ //起始价格不等于0
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(split[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }

            if(!"*".equals(split[1])){ //最高价格不等于*
                Criteria filterCriteria=new Criteria("item_price").lessThan(split[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //1.6设置分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null){
            pageNo = 1;   //默认第一页
        }

        //1.7设置排序

        //排序方法
        String  sort = (String)searchMap.get("sort");  //ASC  DESC
        //排序字段
        String sortField = (String)searchMap.get("sortField");

        if(!StringUtils.isEmpty(sort)) {
            if("ASC".equalsIgnoreCase(sort)){
                Sort s = new Sort(Sort.Direction.ASC,"item_"+sortField);
                highlightQuery.addSort(s);
            }

            if("DESC".equalsIgnoreCase(sort)){
                Sort s = new Sort(Sort.Direction.DESC,"item_"+sortField);
                highlightQuery.addSort(s);
            }
        }



        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize = 20; //默认每页显示20条
        }
        highlightQuery.setOffset((pageNo-1)*pageSize); //设置起始数
        highlightQuery.setRows(pageSize); //设置每页数量

        //获取高亮查询分页对象
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();


        for (HighlightEntry<TbItem> entry : highlighted){
            //获取高亮列表(高亮域的个数)
           List<HighlightEntry.Highlight> highlightList = entry.getHighlights();

            if(highlightList.size()>0 &&  highlightList.get(0).getSnipplets().size()>0 ){
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", highlightPage.getContent());

        map.put("totalPages", highlightPage.getTotalPages());//返回总页数
        map.put("total", highlightPage.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //获取模板id
        Long typeId  = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if(typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

}
