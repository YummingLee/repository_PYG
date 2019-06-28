package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbTypeTemplate;

import java.io.Serializable;


public class ItemCat implements Serializable {

    private Long id;

    private Long parentId;

    private String name;


    private TbTypeTemplate tbTypeTemplate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TbTypeTemplate getTbTypeTemplate() {
        return tbTypeTemplate;
    }

    public void setTbTypeTemplate(TbTypeTemplate tbTypeTemplate) {
        this.tbTypeTemplate = tbTypeTemplate;
    }
}
