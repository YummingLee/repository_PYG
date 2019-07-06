package com.pinyougou.search.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;


@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map=new HashMap();

//        Query query=new SimpleQuery("*:*");
//        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows", page.getContent());


        HighlightQuery query =new SimpleHighlightQuery();
        HighlightOptions options = new HighlightOptions().addField("item_title");
        options.setSimplePrefix("<em style='color:pink'>");
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);

        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for (HighlightEntry<TbItem> tbItemHighlightEntry : tbItems.getHighlighted()) {
            TbItem item = tbItemHighlightEntry.getEntity();
            if(tbItemHighlightEntry.getHighlights().size()>0 && tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",tbItems.getContent());

        return map;
    }

}
