package com.ruoyi.testcase.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 测试用例脑图对象 test_mindmap
 * 
 * @author ruoyi
 */
public class TestMindmap extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 脑图ID */
    private Long id;

    /** 用例名称 */
    @Excel(name = "用例名称")
    private String name;

    /** 所属文件夹ID */
    @Excel(name = "所属文件夹ID")
    private Long folderId;

    /** 关联需求链接 */
    @Excel(name = "关联需求链接")
    private String requirement;

    /** 负责人 */
    @Excel(name = "负责人")
    private String owner;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 当前脑图内容 */
    private String content;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    
    public void setFolderId(Long folderId) 
    {
        this.folderId = folderId;
    }

    public Long getFolderId() 
    {
        return folderId;
    }
    
    public void setRequirement(String requirement) 
    {
        this.requirement = requirement;
    }

    public String getRequirement() 
    {
        return requirement;
    }
    
    public void setOwner(String owner) 
    {
        this.owner = owner;
    }

    public String getOwner() 
    {
        return owner;
    }
    
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }
    
    public void setContent(String content)
    {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("folderId", getFolderId())
            .append("requirement", getRequirement())
            .append("owner", getOwner())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("content", getContent())
            .toString();
    }
} 