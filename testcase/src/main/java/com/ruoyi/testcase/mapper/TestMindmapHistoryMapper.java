package com.ruoyi.testcase.mapper;

import java.util.List;
import com.ruoyi.testcase.domain.TestMindmapHistory;

/**
 * 测试用例脑图历史版本Mapper接口
 * 
 * @author ruoyi
 */
public interface TestMindmapHistoryMapper 
{
    /**
     * 查询测试用例脑图历史版本
     * 
     * @param versionId 测试用例脑图历史版本主键
     * @return 测试用例脑图历史版本
     */
    public TestMindmapHistory selectTestMindmapHistoryById(Long versionId);

    /**
     * 查询测试用例脑图历史版本列表
     * 
     * @param testMindmapHistory 测试用例脑图历史版本
     * @return 测试用例脑图历史版本集合
     */
    public List<TestMindmapHistory> selectTestMindmapHistoryList(TestMindmapHistory testMindmapHistory);

    /**
     * 根据脑图ID查询历史版本列表
     * 
     * @param mindmapId 脑图ID
     * @return 测试用例脑图历史版本集合
     */
    public List<TestMindmapHistory> selectHistoryByMindmapId(Long mindmapId);
    
    /**
     * 新增测试用例脑图历史版本
     * 
     * @param testMindmapHistory 测试用例脑图历史版本
     * @return 结果
     */
    public int insertTestMindmapHistory(TestMindmapHistory testMindmapHistory);

    /**
     * 删除测试用例脑图历史版本
     * 
     * @param versionId 测试用例脑图历史版本主键
     * @return 结果
     */
    public int deleteTestMindmapHistoryById(Long versionId);

    /**
     * 批量删除测试用例脑图历史版本
     * 
     * @param versionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestMindmapHistoryByIds(Long[] versionIds);
    
    /**
     * 删除脑图所有历史版本
     * 
     * @param mindmapId 脑图ID
     * @return 结果
     */
    public int deleteHistoryByMindmapId(Long mindmapId);
} 