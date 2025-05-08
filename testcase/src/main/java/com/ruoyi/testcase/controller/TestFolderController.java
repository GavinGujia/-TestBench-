package com.ruoyi.testcase.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.testcase.domain.TestFolder;
import com.ruoyi.testcase.service.ITestFolderService;

/**
 * 测试用例文件夹Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/testcase/folder")
public class TestFolderController extends BaseController
{
    @Autowired
    private ITestFolderService testFolderService;

    /**
     * 查询测试用例文件夹列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestFolder testFolder)
    {
        startPage();
        List<TestFolder> list = testFolderService.selectTestFolderList(testFolder);
        return getDataTable(list);
    }

    /**
     * 获取测试用例文件夹树
     */
    @GetMapping("/tree")
    public AjaxResult tree()
    {
        List<TestFolder> list = testFolderService.buildFolderTree();
        return success(list);
    }

    /**
     * 导出测试用例文件夹列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:export')")
    @Log(title = "测试用例文件夹", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(TestFolder testFolder)
    {
        List<TestFolder> list = testFolderService.selectTestFolderList(testFolder);
        ExcelUtil<TestFolder> util = new ExcelUtil<TestFolder>(TestFolder.class);
        return util.exportExcel(list, "测试用例文件夹数据");
    }

    /**
     * 获取测试用例文件夹详细信息
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(testFolderService.selectTestFolderById(id));
    }

    /**
     * 新增测试用例文件夹
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:add')")
    @Log(title = "测试用例文件夹", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestFolder testFolder)
    {
        Long id = testFolderService.insertTestFolder(testFolder);
        
        AjaxResult ajax = AjaxResult.success("新增成功");
        ajax.put("id", id);
        return ajax;
    }

    /**
     * 修改测试用例文件夹
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:edit')")
    @Log(title = "测试用例文件夹", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestFolder testFolder)
    {
        return toAjax(testFolderService.updateTestFolder(testFolder));
    }

    /**
     * 删除测试用例文件夹
     */
    @PreAuthorize("@ss.hasPermi('testcase:folder:remove')")
    @Log(title = "测试用例文件夹", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(testFolderService.deleteTestFolderById(id));
    }
} 