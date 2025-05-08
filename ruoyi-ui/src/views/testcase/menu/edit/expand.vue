<template lang="">
<div class="expand-group">

  <el-button class="tab-icons expand" @click="expandAll"></el-button>
  <el-row class="block-col-1">
    <el-col :span="24">
      <!-- <span class="demonstration">click 激活</span> -->
      <el-dropdown trigger="click" :hide-on-click="true" class="dropdown-toggle menu-btn" @command="handleCommand" placement="bottom-start" popper-class="expand-dropdown-custom">
        <span class="el-dropdown-link ">
          展开
          <i class="el-icon-caret-bottom el-icon--right"></i>
        </span>
        <el-dropdown-menu slot="dropdown" class="expand-dropdown-list">
          <el-dropdown-item class="expand-all dropdown-item" command="all">展开全部</el-dropdown-item>
          <el-dropdown-item class="expand-1 dropdown-item " command="1">展开到一级节点</el-dropdown-item>
          <el-dropdown-item class="expand-2 dropdown-item " command="2">展开到二级节点</el-dropdown-item>
          <el-dropdown-item class="expand-3 dropdown-item " command="3">展开到三级节点</el-dropdown-item>
          <el-dropdown-item class="expand-4 dropdown-item " command="4">展开到四级节点</el-dropdown-item>
          <el-dropdown-item class="expand-5 dropdown-item " command="5">展开到五级节点</el-dropdown-item>
          <el-dropdown-item class="expand-6 dropdown-item " command="6">展开到六级节点</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </el-col>
  </el-row>
</div>
</template>

<script>
import {
  mapGetters
} from 'vuex';
export default {
  name: 'expand',
  computed: {
    ...mapGetters({
      'minder': 'getMinder'
    })

  },
  methods: {
    handleCommand(command) {
      // 检查minder是否可用
      if (!this.minder || !this.minder.execCommand) {
        return;
      }
      
      if (command === 'all') {
        // 使用足够大的数值来展开所有节点
        this.minder.execCommand('ExpandToLevel', 9999);
      } else {
        // 正常展开到指定级别
        this.minder.execCommand('ExpandToLevel', command);
      }
    },

    expandAll() {
      // 确保使用正确的minder引用
      if (this.minder && this.minder.execCommand) {
        this.minder.execCommand('ExpandToLevel', 9999);
      }
    }
  }
}
</script>

<style scoped>
.expand-group {
  position: relative;
}

.el-dropdown {
  position: relative;
}

.dropdown-toggle {
  position: relative;
}

.el-dropdown-menu {
  min-width: 180px !important;
  width: auto !important;
  white-space: nowrap !important;
}

:deep(.el-dropdown-menu__item) {
  width: auto !important;
  white-space: nowrap !important;
  padding-right: 20px !important;
  padding-left: 20px !important;
}

/* 展开全部选项特殊样式 */
:deep(.expand-all) {
  color: #409EFF;
  font-weight: 500;
}
</style>

<style>
/* 全局样式，应用到自定义下拉菜单 */
.expand-dropdown-custom {
  min-width: 180px !important;
  padding: 5px 0 !important;
}

.expand-dropdown-custom .el-dropdown-menu__item {
  padding: 0 25px !important;
  height: 35px !important;
  line-height: 35px !important;
  font-size: 14px !important;
}
</style>
