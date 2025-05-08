package com.ruoyi.testcase.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 测试用例脑图历史版本对象 test_mindmap_history
 * 
 * @author ruoyi
 * @date 2024-05-05
 */
public class TestMindmapHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 历史版本ID */
    private Long id;

    /** 脑图ID */
    @Excel(name = "脑图ID")
    private Long mindmapId;

    /** 历史版本内容 */
    @Excel(name = "历史版本内容")
    private String content;

    /** 版本描述 */
    @Excel(name = "版本描述")
    private String description;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 创建者 */
    @Excel(name = "创建者")
    private String createBy;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setMindmapId(Long mindmapId) 
    {
        this.mindmapId = mindmapId;
    }

    public Long getMindmapId() 
    {
        return mindmapId;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }
    
    @Override
    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    @Override
    public String getCreateBy()
    {
        return createBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("mindmapId", getMindmapId())
            .append("content", getContent())
            .append("description", getDescription())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
} 