package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    private TbGoods goods;

    private TbGoodsDesc goodsDesc;

    private List<ItemCat> itemCatList;

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<ItemCat> getItemCatList() {
        return itemCatList;
    }

    public void setItemCatList(List<ItemCat> itemCatList) {
        this.itemCatList = itemCatList;
    }
}
