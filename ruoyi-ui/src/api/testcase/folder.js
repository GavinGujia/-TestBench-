import request from '@/utils/request'

// 获取文件夹树结构
export function getFolderTree() {
  return request({
    url: '/testcase/folder/tree',
    method: 'get'
  })
}

// 查询文件夹列表
export function listFolder(query) {
  return request({
    url: '/testcase/folder/list',
    method: 'get',
    params: query
  })
}

// 获取文件夹详情
export function getFolderDetail(id) {
  return request({
    url: '/testcase/folder/' + id,
    method: 'get'
  })
}

// 新增文件夹
export function addFolder(data) {
  return request({
    url: '/testcase/folder',
    method: 'post',
    data: data
  })
}

// 修改文件夹
export function updateFolder(data) {
  return request({
    url: '/testcase/folder',
    method: 'put',
    data: data
  })
}

// 删除文件夹
export function deleteFolder(id) {
  return request({
    url: '/testcase/folder/' + id,
    method: 'delete'
  })
}

// 根据文件夹ID获取测试用例列表
export function getMindmapListByFolder(folderId, query) {
  return request({
    url: '/testcase/mindmap/folder/' + folderId,
    method: 'get',
    params: query
  })
}

// 导出文件夹数据
export function exportFolder(query) {
  return request({
    url: '/testcase/folder/export',
    method: 'get',
    params: query
  })
} 