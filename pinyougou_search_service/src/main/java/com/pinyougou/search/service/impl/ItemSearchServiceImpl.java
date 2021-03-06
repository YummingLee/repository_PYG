package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;


@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        map.putAll(searchList(searchMap));

        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        if (categoryList.size() > 0) {
            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }

        String categoryName = (String) searchMap.get("category");
        //有名字按名字查
        if (!"".equals(categoryName)) {
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {
            //没名字按集合第一个查
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
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
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    private Map searchList(Map searchMap) {
        Map map = new HashMap();

        HighlightQuery query = new SimpleHighlightQuery();

        HighlightOptions options = new HighlightOptions().addField("item_title"); //后可追加addField方法
        options.setSimplePrefix("<em style='color:pink'>");
        options.setSimplePostfix("</em>");

        query.setHighlightOptions(options);

        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        if (!"".equals(searchMap.get("category"))) {

            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {

                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
        }

        if(!"".equals(searchMap.get("price"))){
            String[] price =((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if(!"*".equals(price[1])){

                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null){
            pageNo = 1;
        }

        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize == null){
            pageSize = 20;
        }

        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if(sortValue != null && !"".equals(sortValue)){
            if("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);

        for (HighlightEntry<TbItem> tbItemHighlightEntry : tbItems.getHighlighted()) {
            TbItem item = tbItemHighlightEntry.getEntity();
            if (tbItemHighlightEntry.getHighlights().size() > 0 && tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", tbItems.getContent());
        map.put("totalPages",tbItems.getTotalPages());
        map.put("total",tbItems.getTotalElements());

        return map;

    }

    private List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<String>();

        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> entriesList = groupResult.getGroupEntries();
        for (GroupEntry<TbItem> entry : entriesList) {
            String groupValue = entry.getGroupValue();
            list.add(groupValue);
        }


        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;


    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();

        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }


        return map;
    }

}
