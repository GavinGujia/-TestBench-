package com.ruoyi.testcase.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.testcase.domain.TestMindmapHistory;

/**
 * 测试用例脑图历史版本Service接口
 * 
 * @author ruoyi
 * @date 2024-05-05
 */
public interface ITestMindmapHistoryService
{
    /**
     * 查询测试用例脑图历史版本
     * 
     * @param id 测试用例脑图历史版本主键
     * @return 测试用例脑图历史版本
     */
    public TestMindmapHistory selectTestMindmapHistoryById(Long id);

    /**
     * 查询测试用例脑图历史版本列表
     * 
     * @param mindmapId 测试用例脑图ID
     * @return 测试用例脑图历史版本集合
     */
    public List<TestMindmapHistory> selectTestMindmapHistoryListByMindmapId(Long mindmapId);

    /**
     * 新增测试用例脑图历史版本
     * 
     * @param params 包含content和description的参数Map
     * @return 结果
     */
    public Long insertTestMindmapHistory(Map<String, String> params);

    /**
     * 删除测试用例脑图历史版本
     * 
     * @param id 测试用例脑图历史版本主键
     * @return 结果
     */
    public int deleteTestMindmapHistoryById(Long id);

    /**
     * 批量删除测试用例脑图历史版本
     * 
     * @param ids 需要删除的测试用例脑图历史版本主键集合
     * @return 结果
     */
    public int deleteTestMindmapHistoryByIds(Long[] ids);
} 