import request from '@/utils/request'

// 根据脑图ID查询历史版本列表
export function getMindmapHistoryList(mindmapId) {
  return request({
    url: '/testcase/history/mindmap/' + mindmapId,
    method: 'get'
  })
}

// 获取历史版本详情
export function getHistoryInfo(versionId) {
  return request({
    url: '/testcase/history/' + versionId,
    method: 'get'
  })
}

// 应用历史版本
export function applyHistory(id) {
  return request({
    url: '/testcase/history/apply/' + id,
    method: 'post'
  })
}

// 创建历史版本记录
export function createHistory(mindmapId, content, description) {
  const historyData = {
    content: typeof content === 'string' ? content : JSON.stringify(content),
    description: description
  };
  
  return request({
    url: '/testcase/history/create/' + mindmapId,
    method: 'post',
    data: historyData
  })
} 