package com.ruoyi.testcase.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.testcase.domain.TestMindmapHistory;
import com.ruoyi.testcase.mapper.TestMindmapHistoryMapper;
import com.ruoyi.testcase.service.ITestMindmapService;

/**
 * 测试用例脑图历史版本Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/testcase/history")
public class TestMindmapHistoryController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TestMindmapHistoryController.class);
    
    @Autowired
    private TestMindmapHistoryMapper testMindmapHistoryMapper;
    
    @Autowired
    private ITestMindmapService testMindmapService;

    /**
     * 查询测试用例脑图历史版本列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:history:list')")
    @GetMapping("/list/{mindmapId}")
    public TableDataInfo list(@PathVariable("mindmapId") Long mindmapId)
    {
        startPage();
        List<TestMindmapHistory> list = testMindmapService.selectTestMindmapHistoryListByMindmapId(mindmapId);
        return getDataTable(list);
    }

    /**
     * 根据脑图ID查询历史版本列表
     */
    @GetMapping("/mindmap/{mindmapId}")
    public TableDataInfo listByMindmapId(@PathVariable("mindmapId") Long mindmapId)
    {
        startPage();
        List<TestMindmapHistory> list = testMindmapHistoryMapper.selectHistoryByMindmapId(mindmapId);
        return getDataTable(list);
    }
    
    /**
     * 导出测试用例脑图历史版本列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:history:export')")
    @Log(title = "测试用例脑图历史版本", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(TestMindmapHistory testMindmapHistory)
    {
        List<TestMindmapHistory> list = testMindmapHistoryMapper.selectTestMindmapHistoryList(testMindmapHistory);
        ExcelUtil<TestMindmapHistory> util = new ExcelUtil<TestMindmapHistory>(TestMindmapHistory.class);
        return util.exportExcel(list, "测试用例脑图历史版本数据");
    }

    /**
     * 获取测试用例脑图历史版本详细信息
     */
    @PreAuthorize("@ss.hasPermi('testcase:history:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(testMindmapService.selectTestMindmapHistoryById(id));
    }

    /**
     * 创建历史版本
     */
    @Log(title = "测试用例脑图历史", businessType = BusinessType.INSERT)
    @PostMapping("/create/{mindmapId}")
    public AjaxResult createHistory(@PathVariable("mindmapId") Long mindmapId, @RequestBody TestMindmapHistory history)
    {
        if (mindmapId == null || mindmapId <= 0) {
            return AjaxResult.error("无效的脑图ID");
        }
        
        if (history.getContent() == null || history.getContent().isEmpty()) {
            return AjaxResult.error("内容不能为空");
        }
        
        // 设置脑图ID
        history.setMindmapId(mindmapId);
        
        try {
            int rows = testMindmapService.createHistory(mindmapId, history.getContent(), history.getDescription());
            return toAjax(rows);
        } catch (Exception e) {
            log.error("创建历史版本失败: {}", e.getMessage(), e);
            return AjaxResult.error("创建历史版本失败: " + e.getMessage());
        }
    }

    /**
     * 删除测试用例脑图历史版本
     */
    @PreAuthorize("@ss.hasPermi('testcase:history:remove')")
    @Log(title = "测试用例脑图历史版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{versionId}")
    public AjaxResult remove(@PathVariable Long versionId)
    {
        return toAjax(testMindmapHistoryMapper.deleteTestMindmapHistoryById(versionId));
    }
    
    /**
     * 应用历史版本
     */
    @PreAuthorize("@ss.hasPermi('testcase:history:apply')")
    @Log(title = "应用历史版本", businessType = BusinessType.UPDATE)
    @PostMapping("/apply/{versionId}")
    public AjaxResult apply(@PathVariable("versionId") Long versionId)
    {
        TestMindmapHistory history = testMindmapHistoryMapper.selectTestMindmapHistoryById(versionId);
        if (history != null)
        {
            int rows = testMindmapService.saveMindmapData(history.getMindmapId(), history.getContent());
            return toAjax(rows);
        }
        return error("历史版本不存在");
    }
} 