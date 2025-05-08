package com.ruoyi.testcase.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.testcase.domain.TestFolder;
import com.ruoyi.testcase.domain.TestMindmap;
import com.ruoyi.testcase.mapper.TestFolderMapper;
import com.ruoyi.testcase.mapper.TestMindmapMapper;
import com.ruoyi.testcase.service.ITestFolderService;

/**
 * 测试用例文件夹Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class TestFolderServiceImpl implements ITestFolderService 
{
    @Autowired
    private TestFolderMapper testFolderMapper;
    
    @Autowired
    private TestMindmapMapper testMindmapMapper;

    /**
     * 查询测试用例文件夹
     * 
     * @param id 测试用例文件夹主键
     * @return 测试用例文件夹
     */
    @Override
    public TestFolder selectTestFolderById(Long id)
    {
        return testFolderMapper.selectTestFolderById(id);
    }

    /**
     * 查询测试用例文件夹列表
     * 
     * @param testFolder 测试用例文件夹
     * @return 测试用例文件夹
     */
    @Override
    public List<TestFolder> selectTestFolderList(TestFolder testFolder)
    {
        return testFolderMapper.selectTestFolderList(testFolder);
    }
    
    /**
     * 构建文件夹树结构
     * 
     * @return 文件夹树列表
     */
    @Override
    public List<TestFolder> buildFolderTree()
    {
        List<TestFolder> folderList = testFolderMapper.selectTestFolderAll();
        
        // 计算每个文件夹的测试用例数量
        for (TestFolder folder : folderList)
        {
            TestMindmap query = new TestMindmap();
            query.setFolderId(folder.getId());
            List<TestMindmap> mindmaps = testMindmapMapper.selectTestMindmapByFolderId(folder.getId());
        }
        
        return buildFolderTree(folderList);
    }
    
    /**
     * 构建文件夹树结构
     * 
     * @param folderList 文件夹列表
     * @return 树结构列表
     */
    private List<TestFolder> buildFolderTree(List<TestFolder> folderList)
    {
        List<TestFolder> returnList = new ArrayList<TestFolder>();
        List<Long> tempList = folderList.stream().map(TestFolder::getId).collect(Collectors.toList());
        
        for (TestFolder folder : folderList)
        {
            // 如果是顶级节点，遍历该父节点的所有子节点
            if (!tempList.contains(folder.getParentId()))
            {
                recursionFn(folderList, folder);
                returnList.add(folder);
            }
        }
        
        if (returnList.isEmpty())
        {
            returnList = folderList;
        }
        
        return returnList;
    }
    
    /**
     * 递归列表
     * 
     * @param list 文件夹列表
     * @param t 文件夹
     */
    private void recursionFn(List<TestFolder> list, TestFolder t)
    {
        // 得到子节点列表
        List<TestFolder> childList = getChildList(list, t);
        t.setChildren(childList);
        
        for (TestFolder tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }
    
    /**
     * 得到子节点列表
     * 
     * @param list 文件夹列表
     * @param t 文件夹
     * @return 子节点列表
     */
    private List<TestFolder> getChildList(List<TestFolder> list, TestFolder t)
    {
        List<TestFolder> tList = new ArrayList<TestFolder>();
        
        for (TestFolder n : list)
        {
            if (n.getParentId().longValue() == t.getId().longValue())
            {
                tList.add(n);
            }
        }
        
        return tList;
    }
    
    /**
     * 判断是否有子节点
     * 
     * @param list 文件夹列表
     * @param t 文件夹
     * @return 结果
     */
    private boolean hasChild(List<TestFolder> list, TestFolder t)
    {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 新增测试用例文件夹
     * 
     * @param testFolder 测试用例文件夹
     * @return 结果
     */
    @Override
    public Long insertTestFolder(TestFolder testFolder)
    {
        testFolder.setCreateBy(SecurityUtils.getUsername());
        testFolder.setCreateTime(DateUtils.getNowDate());
        testFolder.setUpdateTime(DateUtils.getNowDate());
        testFolder.setDelFlag("0");
        
        testFolderMapper.insertTestFolder(testFolder);
        
        return testFolder.getId();
    }

    /**
     * 修改测试用例文件夹
     * 
     * @param testFolder 测试用例文件夹
     * @return 结果
     */
    @Override
    public int updateTestFolder(TestFolder testFolder)
    {
        testFolder.setUpdateBy(SecurityUtils.getUsername());
        testFolder.setUpdateTime(DateUtils.getNowDate());
        
        // 不能将文件夹的父ID修改为自己或自己的子文件夹
        if (testFolder.getId().equals(testFolder.getParentId()))
        {
            throw new ServiceException("修改文件夹'" + testFolder.getName() + "'失败，上级文件夹不能选择自己");
        }
        
        if (testFolder.getParentId() != 0)
        {
            List<TestFolder> childList = getChildList(testFolderMapper.selectTestFolderAll(), testFolder);
            for (TestFolder child : childList)
            {
                if (child.getId().equals(testFolder.getParentId()))
                {
                    throw new ServiceException("修改文件夹'" + testFolder.getName() + "'失败，上级文件夹不能选择自己的子文件夹");
                }
            }
        }
        
        return testFolderMapper.updateTestFolder(testFolder);
    }

    /**
     * 批量删除测试用例文件夹
     * 
     * @param ids 需要删除的测试用例文件夹主键
     * @return 结果
     */
    @Override
    public int deleteTestFolderByIds(Long[] ids)
    {
        return testFolderMapper.deleteTestFolderByIds(ids);
    }

    /**
     * 删除测试用例文件夹信息
     * 
     * @param id 测试用例文件夹主键
     * @return 结果
     */
    @Override
    public int deleteTestFolderById(Long id)
    {
        // 查询是否有子文件夹
        TestFolder folder = new TestFolder();
        folder.setParentId(id);
        List<TestFolder> childFolders = testFolderMapper.selectTestFolderList(folder);
        if (childFolders.size() > 0)
        {
            throw new ServiceException("存在子文件夹，不允许删除");
        }
        
        // 查询文件夹下是否有测试用例
        List<TestMindmap> mindmaps = testMindmapMapper.selectTestMindmapByFolderId(id);
        if (mindmaps.size() > 0)
        {
            throw new ServiceException("文件夹下存在测试用例，不允许删除");
        }
        
        return testFolderMapper.deleteTestFolderById(id);
    }
} 