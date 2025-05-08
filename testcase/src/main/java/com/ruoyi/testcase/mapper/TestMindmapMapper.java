package com.ruoyi.testcase.mapper;

import java.util.List;
import com.ruoyi.testcase.domain.TestMindmap;

/**
 * 测试用例脑图Mapper接口
 * 
 * @author ruoyi
 */
public interface TestMindmapMapper 
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
     * @return 测试用例脑图集合
     */
    public List<TestMindmap> selectTestMindmapByFolderId(Long folderId);
    
    /**
     * 新增测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public int insertTestMindmap(TestMindmap testMindmap);

    /**
     * 修改测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public int updateTestMindmap(TestMindmap testMindmap);

    /**
     * 删除测试用例脑图
     * 
     * @param id 测试用例脑图主键
     * @return 结果
     */
    public int deleteTestMindmapById(Long id);

    /**
     * 批量删除测试用例脑图
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestMindmapByIds(Long[] ids);
    
    /**
     * 插入脑图内容
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public int insertMindmapContent(TestMindmap testMindmap);
    
    /**
     * 更新脑图内容
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    public int updateMindmapContent(TestMindmap testMindmap);
} 