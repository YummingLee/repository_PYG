package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;


	private void setItemValus(Goods goods,TbItem item){
		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//更新日期

		item.setGoodsId(goods.getGoods().getId());//商品ID
		item.setSellerId(goods.getGoods().getSellerId());//商家ID

		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		//图片
		List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage( (String)imageList.get(0).get("url"));
		}

	}
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());

	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);


	}

	private void saveItemList(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for (TbItem item : goods.getItemList()){
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> map = JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title +=  " "+map.get(key);
				}
//			 String title;
				item.setTitle(title);
//			 Long goodsId;
				item.setGoodsId(goods.getGoods().getId());
//			 String sellerId;
				item.setSellerId(goods.getGoods().getSellerId());
//			 Long categoryid;
				item.setCategoryid(goods.getGoods().getCategory3Id());
//			 Date createTime;
				item.setCreateTime(new Date());
//			 Date updateTime;
				item.setUpdateTime(new Date());
//			 String brand;
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
				item.setBrand(tbBrand.getName());
//			 String category;
				TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
				item.setCategory(tbItemCat.getName());
//			 String seller;
				TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
				item.setSeller(tbSeller.getNickName());
//			 String image;
				List<Map> imgList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
				if (imgList.size() > 0){
					item.setImage((String) imgList.get(0).get("url"));
				}
				itemMapper.insert(item);
			}

		}else {

			TbItem item=new TbItem();
			//名称
			item.setTitle(goods.getGoods().getGoodsName());
			//价格
			item.setPrice( goods.getGoods().getPrice() );
			//状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			//数量
			item.setNum(99999);
			item.setSpec("{}");

			setItemValus(goods,item);

			itemMapper.insert(item);
		}

	}
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

		goods.getGoods().setAuditStatus("0"); //设置申请状态
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//删除原来的值
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goods.setItemList(tbItems);
		return goods;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	@Override
	public Boolean isAuditStatus(Long[] ids) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			String auditStatus =  tbGoods.getAuditStatus();
			System.out.println(auditStatus);
			if(!"1".equals(auditStatus)){ //有没上架的商品
				return false;
			}
		}
		return true;
	}

	@Override
	public void isMarketable(Long[] ids) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			String isMarketable = tbGoods.getIsMarketable();
			if(isMarketable!=null){
				if("1".equals(isMarketable)){
					tbGoods.setIsMarketable("0");
				}else if ("0".equals(isMarketable)){
					tbGoods.setIsMarketable("1");
				}
				goodsMapper.updateByPrimaryKey(tbGoods);
			}else {
				tbGoods.setIsMarketable("1");
				goodsMapper.updateByPrimaryKey(tbGoods);
			}
		}
	}

	@Override
	public List<TbItem> renewalItemList(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));

        List<TbItem> list = itemMapper.selectByExample(example);
        return list;
    }

}
