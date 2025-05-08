package com.ruoyi.testcase.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.testcase.domain.TestMindmap;
import com.ruoyi.testcase.service.ITestMindmapService;
import com.ruoyi.testcase.service.IAiConfigService;
import com.ruoyi.testcase.domain.AiConfig;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

/**
 * 测试用例脑图Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/testcase/mindmap")
public class TestMindmapController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TestMindmapController.class);
    // 定义默认脑图JSON结构为常量
    private static final String DEFAULT_MINDMAP_JSON = "{\"root\":{\"data\":{\"id\":\"root\",\"text\":\"测试用例\"},\"children\":[]}}";

    @Autowired
    private ITestMindmapService testMindmapService;
    
    @Autowired
    private IAiConfigService aiConfigService;

    /**
     * 查询测试用例脑图列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestMindmap testMindmap)
    {
        startPage();
        List<TestMindmap> list = testMindmapService.selectTestMindmapList(testMindmap);
        return getDataTable(list);
    }
    
    /**
     * 根据文件夹ID查询测试用例列表
     */
    @GetMapping("/folder/{folderId}")
    public TableDataInfo listByFolder(@PathVariable("folderId") Long folderId, TestMindmap testMindmap)
    {
        startPage();
        List<TestMindmap> list = testMindmapService.selectTestMindmapByFolderId(folderId, testMindmap);
        return getDataTable(list);
    }

    /**
     * 导出测试用例脑图列表
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:export')")
    @Log(title = "测试用例脑图", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, TestMindmap testMindmap)
    {
        List<TestMindmap> list = testMindmapService.selectTestMindmapList(testMindmap);
        ExcelUtil<TestMindmap> util = new ExcelUtil<TestMindmap>(TestMindmap.class);
        util.exportExcel(response, list, "测试用例脑图数据");
    }

    /**
     * 获取测试用例脑图详细信息
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(testMindmapService.selectTestMindmapById(id));
    }

    /**
     * 新增测试用例脑图
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:add')")
    @Log(title = "测试用例脑图", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestMindmap testMindmap)
    {
        Long id = testMindmapService.insertTestMindmap(testMindmap);
        
        AjaxResult ajax = AjaxResult.success("新增成功");
        ajax.put("id", id);
        return ajax;
    }

    /**
     * 修改测试用例脑图
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:edit')")
    @Log(title = "测试用例脑图", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestMindmap testMindmap)
    {
        return toAjax(testMindmapService.updateTestMindmap(testMindmap));
    }

    /**
     * 删除测试用例脑图
     */
    @PreAuthorize("@ss.hasPermi('testcase:mindmap:remove')")
    @Log(title = "测试用例脑图", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(testMindmapService.deleteTestMindmapById(id));
    }
    
    /**
     * 获取脑图数据
     */
    @Log(title = "测试用例脑图", businessType = BusinessType.OTHER)
    @GetMapping("/data/{id}")
    public AjaxResult getMindmapData(@PathVariable("id") Long id)
    {
        try {
            log.info("获取脑图数据, mindmapId: {}", id);
            
            if (id == null) {
                log.error("获取脑图数据失败: 脑图ID为空");
                return AjaxResult.error("脑图ID不能为空");
            }
            
            String data = testMindmapService.getMindmapData(id);
            
            if (data != null && !data.isEmpty() && data.trim().startsWith("{") && data.trim().endsWith("}")) {
                log.info("获取脑图数据成功, mindmapId: {}, 数据长度: {}", id, data.length());
                return success(data);
            } else {
                // 数据为空或格式无效，返回默认结构
                log.warn("未找到有效脑图数据, mindmapId: {}, 返回默认结构", id);
                
                // 同时更新数据库
                try {
                    testMindmapService.saveMindmapData(id, DEFAULT_MINDMAP_JSON);
                    log.info("已将默认脑图结构保存到数据库");
                } catch (Exception e) {
                    log.error("保存默认脑图结构失败: {}", e.getMessage());
                }
                
                return success(DEFAULT_MINDMAP_JSON);
            }
        } catch (Exception e) {
            log.error("获取脑图数据异常: {}", e.getMessage(), e);
            // 发生异常也返回默认结构
            return success(DEFAULT_MINDMAP_JSON);
        }
    }
    
    /**
     * 保存脑图数据
     */
    @Log(title = "测试用例脑图", businessType = BusinessType.UPDATE)
    @PostMapping("/data")
    public synchronized AjaxResult saveMindmapData(@RequestBody TestMindmap testMindmap)
    {
        try {          
            if (testMindmap.getId() == null) {
                log.error("保存脑图数据失败: 脑图ID为空");
                return AjaxResult.error("脑图ID不能为空");
            }
            
            if (testMindmap.getContent() == null) {
                log.error("保存脑图数据失败: 脑图内容为空");
                return AjaxResult.error("脑图内容不能为空");
            }
            
            int rows = testMindmapService.saveMindmapData(testMindmap.getId(), testMindmap.getContent());
            
            log.info("保存脑图数据完成, ID: {}, 影响行数: {}", testMindmap.getId(), rows);
            
            if (rows > 0) {
                return AjaxResult.success("保存成功");
            } else {
                log.warn("保存脑图数据失败, 未更新任何记录, ID: {}", testMindmap.getId());
                return AjaxResult.error("保存失败，可能是脑图ID不存在");
            }
        } catch (Exception e) {
            log.error("保存脑图数据异常: {}", e.getMessage(), e);
            return AjaxResult.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * AI生成测试用例
     */
    @Log(title = "AI生成测试用例", businessType = BusinessType.OTHER)
    @PostMapping("/generate-testcase")
    public AjaxResult generateTestCase(@RequestBody Map<String, Object> requestData)
    {
        log.info("AI生成测试用例请求开始");
        
        try {
            // 获取AI配置
            AiConfig aiConfig = aiConfigService.getDefaultAiConfig();
            
            // 检查AI配置是否存在
            if (aiConfig == null) {
                log.error("AI配置未设置，请先在数据库中配置AI参数");
                return AjaxResult.error("AI配置未设置，请先在数据库中配置AI参数");
            }
            
            // 检查必要的配置项是否存在
            if (aiConfig.getAuthorizationKey() == null || aiConfig.getAuthorizationKey().isEmpty()) {
                log.error("API认证密钥未配置");
                return AjaxResult.error("API认证密钥未配置");
            }
            
            if (aiConfig.getModel() == null || aiConfig.getModel().isEmpty()) {
                log.error("AI模型名称未配置");
                return AjaxResult.error("AI模型名称未配置");
            }
            
            if (aiConfig.getPromptTemplate() == null || aiConfig.getPromptTemplate().isEmpty()) {
                log.error("提示词模板未配置");
                return AjaxResult.error("提示词模板未配置");
            }
            
            // 准备请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 添加必要的浏览器头信息
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json, text/plain, */*");
            // 设置授权信息
            headers.set("Authorization", "Bearer" + aiConfig.getAuthorizationKey());
            
            // 从请求中获取内容
            String userContent = requestData.containsKey("content") ? 
                requestData.get("content").toString() : "";
                
            // 构造提示词
            String promptTemplate = aiConfig.getPromptTemplate() + "\n\n功能详述: " + userContent;
            
            // 使用Map和ObjectMapper处理JSON对象
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", aiConfig.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", promptTemplate);
            messages.add(message);
            
            requestBodyMap.put("messages", messages);
            requestBodyMap.put("stream", false);

            // 转换为JSON字符串
            String jsonBody = objectMapper.writeValueAsString(requestBodyMap);
            log.info("jsonBody:" + jsonBody);
            
            // 创建HTTP实体
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            // 创建RestTemplate执行请求
            RestTemplate restTemplate = new RestTemplate();
            String copilotUrl = "https://copilot.sanuya.com/restful/v1/chat/completions";
            
            // 发送请求并获取响应
            ResponseEntity<Object> responseEntity = restTemplate.exchange(
                copilotUrl, 
                HttpMethod.POST, 
                entity, 
                Object.class
            );
            
            // 返回AI服务的响应
            log.info("AI生成测试用例成功");
            return AjaxResult.success(responseEntity.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("AI服务客户端错误: {}, 响应内容: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return AjaxResult.error("AI服务错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("AI服务器错误: {}, 响应内容: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return AjaxResult.error("AI服务器错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("AI生成测试用例异常: {}", e.getMessage(), e);
            return AjaxResult.error("生成测试用例失败: " + e.getMessage());
        }
    }
} 