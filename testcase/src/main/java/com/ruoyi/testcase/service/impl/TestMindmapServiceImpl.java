package com.ruoyi.testcase.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.testcase.domain.TestFolder;
import com.ruoyi.testcase.domain.TestMindmap;
import com.ruoyi.testcase.domain.TestMindmapHistory;
import com.ruoyi.testcase.mapper.TestFolderMapper;
import com.ruoyi.testcase.mapper.TestMindmapHistoryMapper;
import com.ruoyi.testcase.mapper.TestMindmapMapper;
import com.ruoyi.testcase.service.ITestMindmapService;

/**
 * 测试用例脑图Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class TestMindmapServiceImpl implements ITestMindmapService 
{
    private static final Logger logger = LoggerFactory.getLogger(TestMindmapServiceImpl.class);

    @Autowired
    private TestMindmapMapper testMindmapMapper;
    
    @Autowired
    private TestFolderMapper testFolderMapper;
    
    @Autowired
    private TestMindmapHistoryMapper testMindmapHistoryMapper;

    /**
     * 查询测试用例脑图
     * 
     * @param id 测试用例脑图主键
     * @return 测试用例脑图
     */
    @Override
    public TestMindmap selectTestMindmapById(Long id)
    {
        return testMindmapMapper.selectTestMindmapById(id);
    }

    /**
     * 查询测试用例脑图列表
     * 
     * @param testMindmap 测试用例脑图
     * @return 测试用例脑图
     */
    @Override
    public List<TestMindmap> selectTestMindmapList(TestMindmap testMindmap)
    {
        List<TestMindmap> mindmapList = testMindmapMapper.selectTestMindmapList(testMindmap);
        
        // 查询文件夹名称
        for (TestMindmap mindmap : mindmapList)
        {
            if (mindmap.getFolderId() != null)
            {
                TestFolder folder = testFolderMapper.selectTestFolderById(mindmap.getFolderId());
                if (folder != null)
                {
                    mindmap.getParams().put("folderName", folder.getName());
                }
            }
        }
        
        return mindmapList;
    }
    
    /**
     * 根据文件夹ID查询测试用例脑图列表
     * 
     * @param folderId 文件夹ID
     * @param testMindmap 查询条件
     * @return 测试用例脑图集合
     */
    @Override
    public List<TestMindmap> selectTestMindmapByFolderId(Long folderId, TestMindmap testMindmap)
    {
        if (testMindmap == null)
        {
            testMindmap = new TestMindmap();
        }
        
        testMindmap.setFolderId(folderId);
        return selectTestMindmapList(testMindmap);
    }

    /**
     * 新增测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    @Override
    @Transactional
    public Long insertTestMindmap(TestMindmap testMindmap)
    {
        testMindmap.setCreateBy(SecurityUtils.getUsername());
        testMindmap.setCreateTime(DateUtils.getNowDate());
        testMindmap.setDelFlag("0");

        // 设置默认的脑图内容结构
        if (StringUtils.isEmpty(testMindmap.getContent())) {
            // 默认脑图内容结构
            String defaultContent = "{\"root\":{\"data\":{\"id\":\"root\",\"text\":\"" + testMindmap.getName() + "\"},\"children\":[{\"data\":{\"id\":\"1\",\"text\":\"分支1\"},\"children\":[]}]}}";
            testMindmap.setContent(defaultContent);
        }
        
        // 新增脑图基本信息
        testMindmapMapper.insertTestMindmap(testMindmap);
        
        // 如果有脑图内容数据，则新增内容
        if (StringUtils.isNotEmpty(testMindmap.getContent()))
        {
            testMindmapMapper.insertMindmapContent(testMindmap);
            
            // 创建首个历史版本记录
            try {
                createHistory(testMindmap.getId(), testMindmap.getContent(), "初始版本");
            } catch (Exception e) {
                logger.error("创建历史版本失败: {}", e.getMessage());
            }
        }
        
        return testMindmap.getId();
    }

    /**
     * 修改测试用例脑图
     * 
     * @param testMindmap 测试用例脑图
     * @return 结果
     */
    @Override
    public int updateTestMindmap(TestMindmap testMindmap)
    {
        testMindmap.setUpdateBy(SecurityUtils.getUsername());
        testMindmap.setUpdateTime(DateUtils.getNowDate());
        
        // 如果有脑图内容数据，则同时更新内容
        if (StringUtils.isNotEmpty(testMindmap.getContent()))
        {
            // 先创建历史版本
            TestMindmap oldMindmap = testMindmapMapper.selectTestMindmapById(testMindmap.getId());
            if (oldMindmap != null && StringUtils.isNotEmpty(oldMindmap.getContent()))
            {
                try {
                    createHistory(testMindmap.getId(), oldMindmap.getContent(), "自动保存版本");
                } catch (Exception e) {
                    logger.error("创建历史版本失败: {}", e.getMessage());
                }
            }
            
            // 更新内容
            testMindmapMapper.updateMindmapContent(testMindmap);
        }
        
        return testMindmapMapper.updateTestMindmap(testMindmap);
    }

    /**
     * 批量删除测试用例脑图
     * 
     * @param ids 需要删除的测试用例脑图主键
     * @return 结果
     */
    @Override
    public int deleteTestMindmapByIds(Long[] ids)
    {
        return testMindmapMapper.deleteTestMindmapByIds(ids);
    }

    /**
     * 删除测试用例脑图信息
     * 
     * @param id 测试用例脑图主键
     * @return 结果
     */
    @Override
    public int deleteTestMindmapById(Long id)
    {
        return testMindmapMapper.deleteTestMindmapById(id);
    }
    
    /**
     * 获取脑图数据
     * 
     * @param id 测试用例脑图主键
     * @return 脑图数据JSON
     */
    @Override
    public String getMindmapData(Long id)
    {
        TestMindmap mindmap = testMindmapMapper.selectTestMindmapById(id);
        if (mindmap != null)
        {
            return mindmap.getContent();
        }
        return null;
    }
    
    /**
     * 保存脑图数据
     * 
     * @param id 测试用例脑图主键
     * @param data 脑图JSON数据
     * @return 结果
     */
    @Override
    @Transactional
    public int saveMindmapData(Long id, String data)
    {
        // 增加日志记录
        logger.info("保存脑图数据 ID: {}, 数据长度: {}", id, (data != null ? data.length() : 0));
        
        if (id == null) {
            logger.error("保存脑图数据失败: 脑图ID为空");
            return 0;
        }
        
        if (data == null) {
            logger.error("保存脑图数据失败: 脑图内容为空");
            return 0;
        }

        try {
            // 查询当前脑图
            TestMindmap mindmap = testMindmapMapper.selectTestMindmapById(id);
            if (mindmap == null)
            {
                logger.error("保存脑图数据失败: 找不到ID为{}的脑图", id);
                return 0;
            }
            
            logger.info("找到脑图: {}", mindmap.getName());
            
            // 更新内容
            mindmap.setContent(data);
            mindmap.setUpdateBy(SecurityUtils.getUsername());
            mindmap.setUpdateTime(DateUtils.getNowDate());
            
            return testMindmapMapper.updateMindmapContent(mindmap);
        } catch (Exception e) {
            logger.error("保存脑图数据异常: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 创建历史版本
     * 
     * @param mindmapId 脑图ID
     * @param content 脑图内容
     * @param description 版本描述
     * @return 结果
     */
    @Override
    public int createHistory(Long mindmapId, String content, String description)
    {
        // 参数验证
        if (mindmapId == null) {
            logger.error("创建历史版本失败: 脑图ID为空");
            return 0;
        }
        
        if (StringUtils.isEmpty(content)) {
            logger.error("创建历史版本失败: 脑图内容为空");
            return 0;
        }
        
        try {
            // 创建历史版本记录
            TestMindmapHistory history = new TestMindmapHistory();
            history.setMindmapId(mindmapId);
            history.setContent(content);
            history.setDescription(description);
            history.setCreateBy(SecurityUtils.getUsername());
            history.setCreateTime(DateUtils.getNowDate());
            
            return testMindmapHistoryMapper.insertTestMindmapHistory(history);
        } catch (Exception e) {
            logger.error("创建历史版本异常: {}", e.getMessage(), e);
            return 0;
        }
    }

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
} 