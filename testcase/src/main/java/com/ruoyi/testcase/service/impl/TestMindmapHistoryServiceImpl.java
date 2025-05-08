package com.ruoyi.testcase.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Date;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.testcase.mapper.TestMindmapHistoryMapper;
import com.ruoyi.testcase.domain.TestMindmapHistory;
import com.ruoyi.testcase.service.ITestMindmapHistoryService;

/**
 * 测试用例脑图历史版本Service实现
 * 
 * @author ruoyi
 * @date 2024-05-05
 */
@Service
public class TestMindmapHistoryServiceImpl implements ITestMindmapHistoryService
{
    @Autowired
    private TestMindmapHistoryMapper testMindmapHistoryMapper;

    /**
     * 查询测试用例脑图历史版本
     * 
     * @param id 测试用例脑图历史版本主键
     * @return 测试用例脑图历史版本
     */
    @Override
    public TestMindmapHistory selectTestMindmapHistoryById(Long id)
    {
        return testMindmapHistoryMapper.selectTestMindmapHistoryById(id);
    }

    /**
     * 查询测试用例脑图历史版本列表
     * 
     * @param mindmapId 测试用例脑图ID
     * @return 测试用例脑图历史版本集合
     */
    @Override
    public List<TestMindmapHistory> selectTestMindmapHistoryListByMindmapId(Long mindmapId)
    {
        TestMindmapHistory history = new TestMindmapHistory();
        history.setMindmapId(mindmapId);
        return testMindmapHistoryMapper.selectTestMindmapHistoryList(history);
    }

    /**
     * 新增测试用例脑图历史版本
     * 
     * @param params 包含content和description的参数Map
     * @return 结果
     */
    @Override
    public Long insertTestMindmapHistory(Map<String, String> params)
    {
        TestMindmapHistory history = new TestMindmapHistory();
        history.setMindmapId(Long.valueOf(params.get("mindmapId")));
        history.setContent(params.get("content"));
        history.setDescription(params.get("description"));
        history.setCreateBy(SecurityUtils.getUsername());
        history.setCreateTime(DateUtils.getNowDate());
        testMindmapHistoryMapper.insertTestMindmapHistory(history);
        return history.getId();
    }

    /**
     * 删除测试用例脑图历史版本
     * 
     * @param id 测试用例脑图历史版本主键
     * @return 结果
     */
    @Override
    public int deleteTestMindmapHistoryById(Long id)
    {
        return testMindmapHistoryMapper.deleteTestMindmapHistoryById(id);
    }

    /**
     * 批量删除测试用例脑图历史版本
     * 
     * @param ids 需要删除的测试用例脑图历史版本主键集合
     * @return 结果
     */
    @Override
    public int deleteTestMindmapHistoryByIds(Long[] ids)
    {
        return testMindmapHistoryMapper.deleteTestMindmapHistoryByIds(ids);
    }
} 