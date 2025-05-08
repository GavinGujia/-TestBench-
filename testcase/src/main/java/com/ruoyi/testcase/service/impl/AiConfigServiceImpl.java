package com.ruoyi.testcase.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.testcase.mapper.AiConfigMapper;
import com.ruoyi.testcase.domain.AiConfig;
import com.ruoyi.testcase.service.IAiConfigService;

/**
 * AI配置Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class AiConfigServiceImpl implements IAiConfigService 
{
    @Autowired
    private AiConfigMapper aiConfigMapper;

    /**
     * 查询AI配置
     * 
     * @param id 配置ID
     * @return AI配置
     */
    @Override
    public AiConfig selectAiConfigById(Long id)
    {
        return aiConfigMapper.selectAiConfigById(id);
    }

    /**
     * 获取默认AI配置
     * 
     * @return AI配置
     */
    @Override
    public AiConfig getDefaultAiConfig()
    {
        // 获取默认配置
        return aiConfigMapper.selectDefaultAiConfig();
    }
} 