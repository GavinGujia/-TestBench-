package com.ruoyi.testcase.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI配置对象 ai_config
 * 
 * @author ruoyi
 */
public class AiConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long id;

    /** API认证密钥 */
    @Excel(name = "API认证密钥")
    private String authorizationKey;

    /** 提示词模板 */
    @Excel(name = "提示词模板")
    private String promptTemplate;

    /** AI模型名称 */
    @Excel(name = "AI模型名称")
    private String model;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setAuthorizationKey(String authorizationKey) 
    {
        this.authorizationKey = authorizationKey;
    }

    public String getAuthorizationKey() 
    {
        return authorizationKey;
    }

    public void setPromptTemplate(String promptTemplate) 
    {
        this.promptTemplate = promptTemplate;
    }

    public String getPromptTemplate() 
    {
        return promptTemplate;
    }

    public void setModel(String model) 
    {
        this.model = model;
    }

    public String getModel() 
    {
        return model;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("authorizationKey", getAuthorizationKey())
            .append("promptTemplate", getPromptTemplate())
            .append("model", getModel())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
} 