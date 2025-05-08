const getters = {
  sidebar: state => state.app.sidebar,
  size: state => state.app.size,
  device: state => state.app.device,
  dict: state => state.dict.dict,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  introduction: state => state.user.introduction,
  roles: state => state.user.roles,
  permissions: state => state.user.permissions,
  permission_routes: state => state.permission.routes,
  topbarRouters:state => state.permission.topbarRouters,
  defaultRoutes:state => state.permission.defaultRoutes,
  sidebarRouters:state => state.permission.sidebarRouters,
  getMinder:state => state.minder,
  config:state => {
    return {
      ctrlPanelMin: state.config.ctrlPanelMin,
      ctrlPanelWidth: state.config.ctrlPanelWidth,
      dividerWidth: state.config.dividerWidth,
      defaultLang: state.config.defaultLang,
      zoom: state.config.zoom
    }
  },
  getEditor:state => state.editor,
  count:state => state.count,
  working:state => {
    return {
      saving: state.working.saving,
      draging: state.working.draging,
      editing: state.working.editing
    }
  },
  // 脑图数据相关
  getMindmapData: state => state.mindmap.mindmapData,
  getViewMode: state => state.mindmap.viewMode,
  getNodeEditMode: state => state.mindmap.nodeEditMode
}
export default getters

