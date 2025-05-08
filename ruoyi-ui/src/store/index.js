import Vue from 'vue'
import Vuex from 'vuex'
import app from './modules/app'
import dict from './modules/dict'
import user from './modules/user'
import tagsView from './modules/tagsView'
import permission from './modules/permission'
import settings from './modules/settings'
import mindmap from './modules/mindmap'
import getters from './getters'
import {
  actions
} from './actions'
import {
  mutations
} from './mutations'
import {
  state
} from './state'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    dict,
    user,
    tagsView,
    permission,
    settings,
    mindmap
  },
  getters,
  state,
  actions,
  mutations
})

export default store
