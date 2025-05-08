<template>
<div class="container">
  <header-menu></header-menu>
  <main-editor></main-editor>
  <navigator></navigator>
  
  <!-- AI生成用例按钮和弹窗 -->
  <div v-show="viewMode === 'edit'" class="ai-button-container">
    <el-button type="primary" size="small" @click="showAIDialog">AI生成用例</el-button>
  </div>
  
  <!-- AI生成用例弹窗 -->
  <el-dialog
    title="AI生成用例"
    :visible.sync="aiDialogVisible"
    width="800px"
    :close-on-click-modal="false"
  >
    <el-form>
      <el-form-item label="功能详述">
        <el-input
          type="textarea"
          :rows="5"
          placeholder="请输入功能详述"
          v-model="aiDescription"
        ></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="aiDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="generateTestCase" :loading="generating">开始生成</el-button>
    </span>
  </el-dialog>
</div>

</template>

<script>
import headerMenu from './header'
import mainEditor from './main/mainEditor'
import navigator from './main/navigator'
import { getMindmapData } from '@/api/testcase/mindmap'
import request from '@/utils/request'
import { generateTestCase } from '@/api/testcase/mindmap'  // 导入新定义的接口函数

export default {
  name: 'Editor',
  components: {
    headerMenu,
    mainEditor,
    navigator
  },
  data() {
    return {
      // 脑图ID
      mindmapId: undefined,
      // 查看模式：默认为view，只有明确指定edit才启用编辑
      viewMode: 'view',
      // 脑图数据
      mindmapData: null,
      // 记录文件夹ID
      folderId: undefined,
      // AI生成用例相关数据
      aiDialogVisible: false,
      aiDescription: '',
      generating: false
    }
  },
  created() {
    // 获取路由参数
    const { id, mode, folderId } = this.$route.query;
    
    // 默认为查看模式
    let initialMode = 'view';
    let allowNodeEdit = false;
    
    // 只有明确指定edit模式才启用编辑模式
    if (mode === 'edit') {
      initialMode = 'edit';
      allowNodeEdit = true;
    }
    
    // 设置组件状态和全局状态
    this.viewMode = initialMode;
    this.$store.commit('SET_VIEW_MODE', initialMode);
    this.$store.commit('SET_NODE_EDIT_MODE', allowNodeEdit);
    
    if (id) {
      this.mindmapId = id;
      
      // 保存文件夹ID
      if (folderId) {
        this.folderId = folderId;
      }
      
      // 获取脑图数据
      this.getMindmapData();
    }
    
    // 监听编辑模式切换事件
    this.$root.$on('toggle-edit-mode', mode => {
      this.viewMode = mode;
    });
  },
  beforeDestroy() {
    // 清理事件监听
    this.$root.$off('toggle-edit-mode');
  },
  methods: {
    // 获取脑图数据
    getMindmapData() {
      if (!this.mindmapId) {
        console.warn("缺少mindmapId参数，无法获取脑图数据");
        return;
      }
      
      this.$modal.loading("正在加载脑图数据...");
      
      // 加载当前版本数据
      getMindmapData(this.mindmapId).then(response => {
        this.$modal.closeLoading();
        
        if (response.code === 200) {
          // 处理数据
          this.processData(response.msg);
        } else {
          this.$modal.msgError(response.msg || "获取脑图数据失败");
          console.error(`获取脑图数据服务器返回错误: ${response.msg || "未知错误"}`);
        }
      }).catch(error => {
        this.$modal.closeLoading();
        console.error(`获取脑图数据请求异常: ${error.message}`);
        this.$modal.msgError("获取脑图数据失败: " + (error.message || "未知错误"));
      });
    },
    
    // 处理获取的数据
    processData(rawData) {
      this.mindmapData = rawData;
      
      try {
        // 解析数据（如果是字符串）
        let parsedData = typeof rawData === 'string' ? JSON.parse(rawData) : rawData;
        
        // 确保数据不为空后再传递给子组件
        if (parsedData) {
          // 将数据传递给子组件
          this.$store.commit('SET_MINDMAP_DATA', parsedData);
          
          // 设置查看模式
          this.$store.commit('SET_VIEW_MODE', this.viewMode);
          
          // 通知子组件强制刷新
          this.$nextTick(() => {
            this.$root.$emit('force-render-mindmap', parsedData);
          });
        } else {
          console.error('解析后的数据为空，无法渲染脑图');
          this.$modal.msgError("解析后的数据为空，无法渲染脑图");
        }
        
      } catch (error) {
        console.error(`处理脑图数据出错: ${error.message}`);
        this.$modal.msgError("处理脑图数据出错: " + error.message);
      }
    },
    
    // AI生成用例相关方法
    showAIDialog() {
      this.aiDialogVisible = true;
      this.aiDescription = '';
    },
    // 生成测试用例
    generateTestCase() {
      // 获取当前编辑器实例和选中节点
      const editor = window.editor;
      if (!editor || !editor.minder) {
        this.$message.warning('编辑器未初始化');
        return;
      }
      
      // 检查是否有选中节点
      const selectedNode = editor.minder.getSelectedNode();
      if (!selectedNode) {
        this.$message.warning('请先选中一个节点');
        return;
      }

      if (!this.aiDescription.trim()) {
        this.$message.warning('请输入功能详述');
        return;
      }

      this.generating = true;

      // 构造请求数据，只传递content
      const content = this.aiDescription;

      // 发送请求到后端代理接口，使用新定义的带有超时设置的函数
      generateTestCase(content).then(response => {
        if (response && response.data) {
          try {
            // 获取返回的内容
            const responseData = response.data || {};
            const rawContent = responseData.data?.content?.content || responseData.data?.content || responseData.content || '';
            
            // 尝试解析API返回的数据
            let childrenData = [];
            
            // 移除可能的Markdown代码块标记
            let cleanContent = rawContent
              .replace(/^```.*\n/, '')  // 移除开头的 ```
              .replace(/\n```$/, '');   // 移除结尾的 ```
              
            try {
              // 将缩进格式转换为节点树
              childrenData = this.parseIndentedFormat(cleanContent);
            } catch (indentError) {
              console.error('解析缩进格式失败:', indentError);
              
              // 解析失败时创建错误提示节点
              childrenData = [{
                data: { text: "解析失败: " + indentError.message },
                children: []
              }];
            }
            
            // 在选中节点下添加内容
            const minder = editor.minder;
            if (minder && selectedNode) {
              // 创建"AI生成的用例"父节点
              minder.execCommand('AppendChildNode', 'AI生成的用例');
              
              // 获取刚创建的节点
              const newNode = minder.getSelectedNode();
              
              // 递归函数，用于添加节点及其子节点
              const addChildrenNodes = (parentNode, children) => {
                if (!children || !children.length) return;
                
                // 遍历子节点数组
                children.forEach(child => {
                  // 选中父节点
                  minder.select(parentNode, true);
                  
                  // 从data.text获取节点文本（特定格式）
                  const nodeText = child.data && child.data.text ? child.data.text : 
                                  (child.text || child.name || child.label || "未命名节点");
                  
                  // 添加子节点
                  minder.execCommand('AppendChildNode', nodeText);
                  
                  // 获取刚添加的子节点
                  const childNode = minder.getSelectedNode();
                  
                  // 如果有孙节点，递归添加
                  if (child.children && child.children.length > 0) {
                    addChildrenNodes(childNode, child.children);
                  }
                });
              };
              
              // 开始添加子节点
              addChildrenNodes(newNode, childrenData);
              
              this.$message.success('AI生成用例已添加到节点');
            }
          } catch (error) {
            this.$message.error('处理AI返回内容失败: ' + error.message);
            console.error('处理AI返回内容失败:', error);
          }
        } else {
          this.$message.error('AI生成失败: ' + (response.msg || '未知错误'));
        }
      }).catch(error => {
        this.$message.error('请求失败: ' + (error.message || '未知错误'));
        console.error('AI生成用例请求失败:', error);
      }).finally(() => {
        this.generating = false;
        this.aiDialogVisible = false;
      });
    },

    // 解析缩进格式的测试用例文本为节点树
    parseIndentedFormat(text) {
      // 移除可能的前后空行和空格
      text = text.trim();
      
      // 分割成行
      const lines = text.split('\n');
      
      // 如果没有内容，返回空数组
      if (lines.length === 0) {
        return [];
      }
      
      // 保存当前层级的节点和其父节点
      let nodeStack = [];
      // 根节点列表
      const rootNodes = [];
      
      // 上一个节点的缩进级别
      let prevIndentLevel = -1;
      
      // 临时存储当前处理的节点
      let currentNode = null;
      
      // 遍历每一行
      lines.forEach(line => {
        // 计算当前行的缩进级别
        const indentMatch = line.match(/^(\s*)/);
        const indent = indentMatch ? indentMatch[1].length : 0;
        const indentLevel = Math.floor(indent / 4); // 假设每4个空格为一级缩进
        
        // 内容（移除前导空格）
        const content = line.trim();
        
        if (content.length === 0) {
          // 忽略空行
          return;
        }
        
        // 检查行内容是否以特定模式开始，比如"内容1:"或"内容2:"
        const isContentLine = /^内容\d+:/.test(content);
        
        // 如果是内容行，添加到当前节点的描述中
        if (isContentLine && currentNode) {
          if (!currentNode.description) {
            currentNode.description = [];
          }
          currentNode.description.push(content);
          return;
        }
        
        // 创建新节点
        const newNode = {
          data: { text: content },
          children: []
        };
        
        // 当前节点设置为新节点（为下一行内容行准备）
        currentNode = newNode;
        
        // 处理缩进级别变化
        if (indentLevel === 0) {
          // 根级别节点
          rootNodes.push(newNode);
          // 清空节点栈，只保留当前根节点
          nodeStack = [newNode];
          prevIndentLevel = 0;
        } else if (indentLevel > prevIndentLevel) {
          // 子节点 - 缩进增加
          if (nodeStack.length > 0) {
            const parentNode = nodeStack[nodeStack.length - 1];
            parentNode.children.push(newNode);
          }
          // 添加到栈中
          nodeStack.push(newNode);
          prevIndentLevel = indentLevel;
        } else if (indentLevel === prevIndentLevel) {
          // 同级节点 - 先移除当前级别
          nodeStack.pop();
          // 然后添加同级节点
          if (nodeStack.length > 0) {
            const parentNode = nodeStack[nodeStack.length - 1];
            parentNode.children.push(newNode);
          }
          // 添加到栈中
          nodeStack.push(newNode);
        } else {
          // 回到上一级 - 移除多余的级别
          // 创建新的栈以避免错误
          const newStack = [];
          // 保留需要的级别
          for (let i = 0; i < indentLevel && i < nodeStack.length; i++) {
            newStack.push(nodeStack[i]);
          }
          nodeStack = newStack;
          
          // 添加到当前级别的父节点
          if (nodeStack.length > 0) {
            const parentNode = nodeStack[nodeStack.length - 1];
            parentNode.children.push(newNode);
          }
          // 添加到栈中
          nodeStack.push(newNode);
          prevIndentLevel = indentLevel;
        }
      });
      
      // 处理描述作为子节点
      const processDescriptions = (node) => {
        if (node.description && node.description.length > 0) {
          // 将描述转换为子节点
          node.description.forEach(desc => {
            node.children.push({
              data: { text: desc },
              children: []
            });
          });
          // 清除描述数组，因为已转换为子节点
          delete node.description;
        }
        
        // 递归处理所有子节点
        if (node.children && node.children.length > 0) {
          node.children.forEach(child => processDescriptions(child));
        }
      };
      
      // 处理所有节点的描述
      rootNodes.forEach(node => processDescriptions(node));
      
      return rootNodes;
    }
  },
  watch: {
    // 监听视图模式变化
    viewMode(newVal) {
      // 移除日志打印
    }
  }
}

</script>

<style lang="scss">
.container {
  position: relative;
  width: 100%;
  height: 100%;
}

.ai-button-container {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
}
</style>

