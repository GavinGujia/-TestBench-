import request from '@/utils/request'

// 查询脑图列表
export function listMindmap(query) {
  return request({
    url: '/testcase/mindmap/list',
    method: 'get',
    params: query
  })
}

// 查询脑图详细
export function getMindmap(id) {
  return request({
    url: '/testcase/mindmap/' + id,
    method: 'get'
  })
}

// 新增脑图
export function addMindmap(data) {
  return request({
    url: '/testcase/mindmap',
    method: 'post',
    data: data
  })
}

// 修改脑图
export function updateMindmap(data) {
  return request({
    url: '/testcase/mindmap',
    method: 'put',
    data: data
  })
}

// 删除脑图
export function delMindmap(id) {
  return request({
    url: '/testcase/mindmap/' + id,
    method: 'delete'
  })
}

// 导出脑图
export function exportMindmap(query) {
  return request({
    url: '/testcase/mindmap/export',
    method: 'get',
    params: query
  })
}

// 获取脑图数据
export function getMindmapData(id) {
  return request({
    url: '/testcase/mindmap/data/' + id,
    method: 'get'
  }).then(response => {
    if (response.code === 200 && response.data) {
      // 尝试检查数据格式
      if (typeof response.data === 'string') {
        try {
          // 检查是否为有效的JSON字符串
          JSON.parse(response.data);
        } catch (e) {
          console.warn("API调用: getMindmapData 数据不是有效的JSON字符串，可能需要特殊处理");
        }
      }
    }
    
    return response;
  }).catch(error => {
    console.error("API调用: getMindmapData 请求失败:", error);
    throw error;
  });
}

// 保存脑图数据
export function saveMindmapData(data) {
  // 确保content是字符串类型
  if (data && data.content && typeof data.content !== 'string') {
    data.content = JSON.stringify(data.content);
  }
  
  // 如果id未定义，说明是新建的脑图，需要先创建脑图记录
  if (!data.id) {
    try {
      const contentObj = typeof data.content === 'string' ? JSON.parse(data.content) : data.content;
      if (contentObj.root && contentObj.root.data && contentObj.root.data.text) {
        mindmapName = contentObj.root.data.text;
      }
    } catch (e) {
      console.error("API调用: 解析content获取名称失败:", e);
    }
    
    // 准备新脑图数据
    const newMindmap = {
      name: mindmapName,
      folderId: data.folderId, // 使用传入的文件夹ID或默认为0
      content: data.content
    };
    
    return addMindmap(newMindmap).then(response => {
      if (response.code === 200 && response.data) {
        // 设置返回的数据，确保前端能够获取到新创建的脑图ID
        return {
          code: 200,
          msg: "操作成功",
          data: response.data
        };
      }
      return response;
    }).catch(error => {
      console.error("API调用: 创建新脑图失败:", error);
      throw error;
    });
  }
  
  // 已有ID，更新现有脑图
  return request({
    url: '/testcase/mindmap/data',
    method: 'post',
    data: data
  }).then(response => {
    return response;
  }).catch(error => {
    console.error("API调用: saveMindmapData 请求失败:", error);
    throw error;
  });
}

// 生成测试用例
export function generateTestCase(content) {
  return request({
    url: '/testcase/mindmap/generate-testcase',
    method: 'post',
    data: { content },
    timeout: 180000 // 设置超时时间为180秒
  });
} 