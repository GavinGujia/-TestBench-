<template lang="html">
<div class="mind-editor">
</div>
</template>

<script>
import {
  mapActions,
  mapMutations,
  mapGetters
} from 'vuex'
export default {
  data() {
    return {
      editor: null,
      initialDataLoaded: false
    }
  },
  mounted() {
    // 初始化编辑器
    const Editor = require('../../../script/editor');
    window.kityminder = window.kityminder || {};
    window.kityminder.Minder = window.kityminder.Minder || {};
    
    this.$nextTick(() => {
      try {
        // 创建编辑器实例
        const editor = window.editor = new Editor(this.$el);
        this.editor = editor;
        this.setEditor(editor);
        
        window.minder = window.km = editor.minder;
        this.setMinder(editor.minder);
        
        // Monkey Patch: 修改exportJson方法，移除version字段
        if (editor.minder.exportJson) {
          const originalExportJson = editor.minder.exportJson;
          editor.minder.exportJson = function() {
            const json = originalExportJson.call(this);
            // 移除version字段
            if (json.version) {
              delete json.version;
            }
            return json;
          };
        }
        
        // 确保minder已完全初始化
        if (editor.minder) {
          // 触发自定义初始化成功事件
          this.$root.$emit('minder-initialized', editor.minder);
          
          // 添加画布点击事件监听，处理点击空白区域时退出编辑状态的问题
          editor.minder.on('click', (e) => {
            // 如果当前处于编辑状态，且点击的是空白区域（没有目标节点）
            if (this.getNodeEditMode && (!e.getTargetNode || !e.getTargetNode())) {
              // 提交当前编辑内容并退出编辑状态
              editor.minder.fire('contentchange');
              this.$store.commit('SET_NODE_EDIT_MODE', false);
              
              // 确保FSM状态切换到normal
              if (editor && editor.fsm && editor.fsm.state() === 'input') {
                editor.fsm.jump('normal', 'input-commit');
              }
            }
          });
        } else {
          console.error('Minder初始化失败: minder对象不存在');
        }
        
        // 初始化数据
        this.initMindmap();
        
        // 监听强制渲染事件
        this.$root.$on('force-render-mindmap', data => {
          if (data) {
            this.renderMindmap(data, true);
            this.initialDataLoaded = true;
            
            // 检查URL是否明确指定查看模式
            const { mode } = this.$route.query;
            
            if (mode === 'view') {
              // 明确指定查看模式时，强制设置为查看模式
              this.disableEditing(true);
            } else if (mode === 'edit') {
              // 明确指定编辑模式时，启用编辑
              this.enableEditing(true);
            } else {
              // 无特殊指定时，保持当前状态
              // 不主动改变模式，由其他组件控制或用户手动切换
            }
            
            // 数据加载完成后触发加载完成事件
            this.$root.$emit('mindmap-data-loaded', data);
          }
        });

        // 监听编辑模式切换事件
        this.$root.$on('toggle-edit-mode', mode => {
          // 非历史版本，正常切换模式
          if (mode === 'view') {
            this.disableEditing(true);
          } else {
            this.enableEditing(true);
            // 触发编辑模式启用事件
            this.$root.$emit('edit-mode-enabled');
          }
        });

        // 监听脑图内容变化事件，并将数据保存到store
        editor.minder.on('contentchange', () => {
          if (this.initialDataLoaded) {
            const mindmapData = editor.minder.exportJson();
            this.$store.commit('SET_MINDMAP_DATA', mindmapData);
          }
        });
        
        this.executeCallback();
      } catch (error) {
        console.error('初始化编辑器时发生错误:', error);
      }
    });
  },
  beforeDestroy() {
    // 清理定时器
    if (this.timer) {
      clearTimeout(this.timer);
    }
    
    // 清理事件监听
    this.$root.$off('force-render-mindmap');
    this.$root.$off('toggle-edit-mode');
    
    // 清理编辑器
    if (this.editor && this.editor.minder) {
      try {
        // If detach method exists, call it, otherwise just clear DOM references
        if (typeof this.editor.minder.detach === 'function') {
          this.editor.minder.detach();
        }
      } catch (e) {
        // Silently handle any errors during cleanup
      }
    }
  },
  computed: {
    ...mapGetters([
      'minder',
      'getMindmapData',
      'getViewMode',
      'getNodeEditMode'
    ])
  },
  methods: {
    ...mapActions([
      'executeCallback'
    ]),
    ...mapMutations([
      'setMinder',
      'setEditor'
    ]),
    // 初始化脑图
    initMindmap() {
      const mindmapData = this.getMindmapData;
      
      if (mindmapData) {
        this.renderMindmap(mindmapData);
        this.initialDataLoaded = true;
      }
      
      // 检查URL参数
      const { mode } = this.$route.query;
      
      // 明确设置初始模式
      if (mode === 'view') {
        // 明确指定查看模式时，强制设置为查看模式
        this.disableEditing(true);
      } else if (mode === 'edit') {
        // 明确指定编辑模式时，强制启用编辑
        this.enableEditing(true);
      } else {
        // 无特殊指定时，确保默认是查看模式
        this.disableEditing(true);
      }
    },
    // 渲染脑图数据
    renderMindmap(data, force = false) {
      if (!data || !data.root) {
        console.warn('渲染失败: 无效的脑图数据');
        return;
      }
      
      if (force) {
        // 强制模式下重置编辑器
        const rootNode = this.editor.minder.getRoot();
        if (rootNode) {
          this.editor.minder.removeAllSelectedNodes();
          this.editor.minder.execCommand('text', '');
        }
      }
      
      // 导入数据
      this.editor.minder.importJson(data);
      
      // 更新UI
      this.editor.minder.refresh();
      this.editor.minder.execCommand('zoom', 100);
      this.editor.minder.execCommand('camera', this.editor.minder.getRoot(), 300);
    },
    // 设置主题
    setTheme(theme) {
      this.editor.minder.execCommand('theme', theme || 'fresh-blue');
      return true;
    },
    // 禁用编辑功能 - 设置脑图为全局查看模式
    disableEditing(force = false) {
      // 更新状态
      this.$store.commit('SET_VIEW_MODE', 'view');
      this.$store.commit('SET_NODE_EDIT_MODE', false);
      
      // 设置为只读
      this.editor.minder.setStatus('readonly');
      
      // 保存原始的queryCommandState方法（如果尚未保存）
      if (!this.editor.minder._originalQueryCommandState) {
        this.editor.minder._originalQueryCommandState = this.editor.minder.queryCommandState;
      }
      
      // 所有编辑相关命令
      const editCommands = [
        'text', 'appendchildnode', 'appendsiblingnode', 'appendparentnode',
        'removenode', 'arrangeup', 'arrangedown', 'priority', 'progress'
      ];
      
      // 创建一个安全的命令状态检查函数，避免递归调用
      const safeCheckCommand = (name, originalMethod) => {
        if (!name) return -1;
        
        // 将命令名称转为小写 - 避免直接调用可能导致无限递归的toLowerCase()
        let lowerName = '';
        try {
          lowerName = String(name).toLowerCase();
        } catch (e) {
          console.error('命令名称转换出错:', e);
          return -1;
        }
        
        // 检查是否是编辑命令
        if (editCommands.indexOf(lowerName) !== -1) {
          return -1; // 编辑命令禁用
        }
        
        // 对于非编辑命令，使用安全的方式调用原始方法
        try {
          return originalMethod.call(this.editor.minder, name);
        } catch (e) {
          console.error(`获取${name}状态失败:`, e);
          return -1; // 出错时禁用命令
        }
      };
      
      // 覆盖queryCommandState方法
      this.editor.minder.queryCommandState = function(name) {
        return safeCheckCommand(name, this._originalQueryCommandState);
      };
    },
    // 启用编辑功能 - 设置脑图为全局编辑模式
    enableEditing(force = false) {
      // 更新状态
      this.$store.commit('SET_VIEW_MODE', 'edit');
      this.$store.commit('SET_NODE_EDIT_MODE', true);
      
      // 恢复原始方法，确保原始方法存在
      if (this.editor.minder._originalQueryCommandState) {
        this.editor.minder.queryCommandState = this.editor.minder._originalQueryCommandState;
      } else {
        console.warn('无法找到原始queryCommandState方法');
      }
      
      // 设置为正常状态
      this.editor.minder._status = 'normal';
    },
  },
  watch: {
    // 监听脑图数据变化
    mindmapData(newData) {
      if (newData && this.editor) {
        this.renderMindmap(newData);
      }
    }
  }
}
</script>

<style lang="scss">
  @import "../../../style/editor.scss";
</style>
