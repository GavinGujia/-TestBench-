const mindmap = {
  state: {
    // 脑图数据
    mindmapData: null,
    // 全局查看模式：edit或view（应用级权限）
    viewMode: 'view',
    // 节点编辑模式：true或false（节点级权限）
    nodeEditMode: false
  },

  mutations: {
    SET_MINDMAP_DATA: (state, data) => {
      state.mindmapData = data
    },
    SET_VIEW_MODE: (state, mode) => {
      state.viewMode = mode
    },
    SET_NODE_EDIT_MODE: (state, enabled) => {
      state.nodeEditMode = enabled
    }
  },

  actions: {
    // 设置脑图数据
    setMindmapData({ commit }, data) {
      commit('SET_MINDMAP_DATA', data)
    },
    // 设置全局查看模式
    setViewMode({ commit }, mode) {
      commit('SET_VIEW_MODE', mode)
    },
    // 设置节点编辑模式
    setNodeEditMode({ commit }, enabled) {
      commit('SET_NODE_EDIT_MODE', enabled)
    }
  }
}

export default mindmap 