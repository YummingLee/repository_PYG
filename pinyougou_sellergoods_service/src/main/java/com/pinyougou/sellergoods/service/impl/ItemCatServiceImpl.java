package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojogroup.ItemCat;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(ItemCat itemCat) {
		TbItemCat tbItemCat = new TbItemCat();
		tbItemCat.setId(itemCat.getId());
		tbItemCat.setParentId(itemCat.getParentId());
		tbItemCat.setName(itemCat.getName());
		tbItemCat.setTypeId(itemCat.getTbTypeTemplate().getId());
		itemCatMapper.insert(tbItemCat);


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(ItemCat itemCat){
		TbItemCat tbItemCat = new TbItemCat();
		tbItemCat.setId(itemCat.getId());
		tbItemCat.setName(itemCat.getName());
		tbItemCat.setParentId(itemCat.getParentId());
		tbItemCat.setTypeId(itemCat.getTbTypeTemplate().getId());

		itemCatMapper.updateByPrimaryKey(tbItemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public ItemCat findOne(Long id){

		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(id);
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectTypeOption(tbItemCat.getTypeId());
		ItemCat itemCat = new ItemCat();
		itemCat.setId(tbItemCat.getId());
		itemCat.setName(tbItemCat.getName());
		itemCat.setParentId(tbItemCat.getParentId());
		itemCat.setTbTypeTemplate(tbTypeTemplate);

		return itemCat;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 *
	 * @param parentId
	 * @return
	 */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {

		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		return itemCatMapper.selectByExample(example);
	}

	@Override
	public List<Map> typeOptions() {
		return itemCatMapper.typeOptions();
	}

	@Override
	public Result findChild(Long[] ids) {
		Result result = new Result();
		result.setSuccess(true);
		int childSum = 0;
		for (Long id : ids) {
		    int childNum =	itemCatMapper.selectChildNum(id);
		    childSum += childNum;
		}
		if (childSum != 0){
			result.setSuccess(false);
			result.setMessage("所选分类包含子类");
//			return result;
		}
		return result;
	}

}
