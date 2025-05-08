<template lang="">
<div class="sequence-group">
  <ul>
    <li v-for="(item, index) in items" class="menu-btn" 
        :class="[classArray(index)]" 
        @click="execCommand(index)" 
        :title="title(index)"></li>
  </ul>
</div>
</template>

<script>
import {
  mapGetters
} from 'vuex'
export default {
  name: 'sequenceBox',
  data() {
    return {
      items: [
        { id: '0' },
        { id: '1' },
        { id: '2' },
        { id: '3' },
        { id: '4' },
        { id: '5' },
        { id: '6' },
        { id: '7' },
        { id: '8' },
        { id: '9' }
      ],
      isReady: false
    }
  },
  computed: {
    ...mapGetters({
      'minder': 'getMinder'
    })
  },
  methods: {
    execCommand(index) {
      // 直接使用queryCommandState检查命令可用性
      if (!this.minder || !this.minder.queryCommandState || this.minder.queryCommandState('priority') === -1) {
        return;
      }
      
      try {
        // 确保有选中的节点
        const selectedNode = this.minder.getSelectedNode();
        if (!selectedNode) {
          return;
        }
        
        // 执行命令
        this.minder.execCommand('priority', index);
        
        // 手动更新视图
        this.$nextTick(() => {
          if (this.minder.fire) {
            this.minder.fire('selectionchange');
          }
          this.$forceUpdate();
        });
      } catch (error) {
        console.error('执行优先级命令失败:', error);
      }
    },
    classArray(index) {
      if (!this.isReady || !this.minder || !this.minder.queryCommandValue) {
        return ['sequence-' + index];
      }
      
      var isActive = this.minder.queryCommandValue('priority') == index;
      var sequence = 'sequence-' + index;

      // 用数组返回多个class
      var arr = [{
        'active': isActive
      }, sequence]
      return arr
    },
    title(index) {
      switch (index) {
        case 0:
          return '移除优先级';
        default:
          return '优先级' + index;
      }
    },
    initMinderEvents() {
      if (!this.minder) {
        setTimeout(() => this.initMinderEvents(), 300);
        return;
      }
      
      try {
        if (!this.minder.getSelectedNode || !this.minder.queryCommandState) {
          setTimeout(() => this.initMinderEvents(), 500);
          return;
        }
        
        this.isReady = true;
        
        try {
          this.minder.off('selectionchange');
          this.minder.off('contentchange');
        } catch (e) {
          // 忽略可能的错误
        }
        
        // 监听节点选择变化，以便更新高亮状态
        this.minder.on('selectionchange', () => {
          this.$forceUpdate();
        });
        
        // 监听内容变化，以便更新高亮状态
        this.minder.on('contentchange', () => {
          this.$forceUpdate();
        });
        
        // 立即进行一次强制更新
        this.$forceUpdate();
      } catch (error) {
        setTimeout(() => this.initMinderEvents(), 1000);
      }
    }
  },
  mounted() {
    // 监听minder初始化完成事件
    this.$root.$on('minder-initialized', () => {
      this.initMinderEvents();
    });
    
    // 监听数据加载完成事件
    this.$root.$on('mindmap-data-loaded', () => {
      this.initMinderEvents();
    });
  },
  beforeDestroy() {
    // 清理事件监听
    this.$root.$off('minder-initialized');
    this.$root.$off('mindmap-data-loaded');
    
    // 清理minder事件监听
    if (this.isReady && this.minder) {
      try {
        this.minder.off('selectionchange');
        this.minder.off('contentchange');
      } catch (e) {
        // 忽略可能的错误
      }
    }
  }
}
</script>

<style>
/* 保留高亮状态样式 */
</style>
