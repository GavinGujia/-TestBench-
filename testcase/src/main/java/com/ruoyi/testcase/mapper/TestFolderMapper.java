package com.ruoyi.testcase.mapper;

import java.util.List;
import com.ruoyi.testcase.domain.TestFolder;

/**
 * 测试用例文件夹Mapper接口
 * 
 * @author ruoyi
 */
public interface TestFolderMapper 
{
    /**
     * 查询测试用例文件夹
     * 
     * @param id 测试用例文件夹主键
     * @return 测试用例文件夹
     */
    public TestFolder selectTestFolderById(Long id);

    /**
     * 查询测试用例文件夹列表
     * 
     * @param testFolder 测试用例文件夹
     * @return 测试用例文件夹集合
     */
    public List<TestFolder> selectTestFolderList(TestFolder testFolder);

    /**
     * 查询所有文件夹
     * 
     * @return 文件夹列表
     */
    public List<TestFolder> selectTestFolderAll();
    
    /**
     * 新增测试用例文件夹
     * 
     * @param testFolder 测试用例文件夹
     * @return 结果
     */
    public int insertTestFolder(TestFolder testFolder);

    /**
     * 修改测试用例文件夹
     * 
     * @param testFolder 测试用例文件夹
     * @return 结果
     */
    public int updateTestFolder(TestFolder testFolder);

    /**
     * 删除测试用例文件夹
     * 
     * @param id 测试用例文件夹主键
     * @return 结果
     */
    public int deleteTestFolderById(Long id);

    /**
     * 批量删除测试用例文件夹
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestFolderByIds(Long[] ids);
} 