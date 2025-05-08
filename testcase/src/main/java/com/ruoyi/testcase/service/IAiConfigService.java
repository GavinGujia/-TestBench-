package com.ruoyi.testcase.service;

import com.ruoyi.testcase.domain.AiConfig;

/**
 * AI配置Service接口
 * 
 * @author ruoyi
 */
public interface IAiConfigService 
{
    /**
     * 查询AI配置
     * 
     * @param id 配置ID
     * @return AI配置
     */
    public AiConfig selectAiConfigById(Long id);

    /**
     * 获取默认AI配置
     * 
     * @return AI配置
     */
    public AiConfig getDefaultAiConfig();
} 