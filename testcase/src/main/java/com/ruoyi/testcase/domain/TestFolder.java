package com.ruoyi.testcase.domain;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 测试用例文件夹对象 test_folder
 * 
 * @author ruoyi
 */
public class
TestFolder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件夹ID */
    private Long id;

    /** 文件夹名称 */
    @Excel(name = "文件夹名称")
    private String name;

    /** 父文件夹ID */
    @Excel(name = "父文件夹ID")
    private Long parentId;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;
    
    /** 子文件夹 */
    private List<TestFolder> children = new ArrayList<TestFolder>();

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
    
    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }
    
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }
    
    public List<TestFolder> getChildren()
    {
        return children;
    }

    public void setChildren(List<TestFolder> children)
    {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("parentId", getParentId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
} 