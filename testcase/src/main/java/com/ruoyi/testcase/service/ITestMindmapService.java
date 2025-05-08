package com.ruoyi.testcase.service;

import java.util.List;
import com.ruoyi.testcase.domain.TestMindmap;
import com.ruoyi.testcase.domain.TestMindmapHistory;

/**
 * 测试用例脑图Service接口
 * 
 * @author ruoyi
 */
public interface ITestMindmapService 
{
    /**
     * 查询测试用例脑图
     * 
     * @param id 测试用例脑图主键
     * @return 测试用例脑图
     */
    public TestMindmap selectTestMindmapById(Long id);

    /**
     * 查询测试用例脑图列表
     * 
     * @param testMindmap 测试用例脑图
     * @return 测试用例脑图集合
     */
    public List<TestMindmap> selectTestMindmapList(TestMindmap testMindmap);

    /**
     * 根据文件夹ID查询测试用例脑图列表
     * 
     * @param folderId 文件夹ID
     * @param testMindmap 查询条件
     * @return 测试用例脑图集合
     */
    public List<TestMindmap> selectTestMindmapByFolderId(Long folderId, TestMindmap testMindmap);
    
    /**
     * 新增测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public Long insertTestMindmap(TestMindmap testMindmap);

    /**
     * 修改测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public int updateTestMindmap(TestMindmap testMindmap);

    /**
     * 批量删除测试用例脑图
     * 
     * @param ids 需要删除的测试用例脑图主键集合
     * @return 结果
     */
    public int deleteTestMindmapByIds(Long[] ids);

    /**
     * 删除测试用例脑图信息
     * 
     * @param id 测试用例脑图主键
     * @return 结果
     */
    public int deleteTestMindmapById(Long id);
    
    /**
     * 获取脑图数据
     * 
     * @param id 测试用例脑图主键
     * @return 脑图数据JSON
     */
    public String getMindmapData(Long id);
    
    /**
     * 保存脑图数据
     * 
     * @param id 测试用例脑图主键
     * @param data 脑图JSON数据
     * @return 结果
     */
    public int saveMindmapData(Long id, String data);
    
    /**
     * 创建历史版本
     * 
     * @param mindmapId 脑图ID
     * @param content 脑图内容
     * @param description 版本描述
     * @return 结果
     */
    public int createHistory(Long mindmapId, String content, String description);
    
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
} 