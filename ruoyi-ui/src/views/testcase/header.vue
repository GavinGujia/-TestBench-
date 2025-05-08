<template lang="html">
  <div>
    <header>
      <ul id="mind_tab">
        <li :class="{selected:switchShow.showEditMenu}">
          <a href="javascript:;" class="btn-showEditMenu" @click="showMenu" title="思维导图">思维导图</a>
        </li>
        <li :class="{selected:switchShow.showViewMenu}">
          <a href="javascript:;" class="btn-showViewMenu" @click="showMenu" title="外观样式">外观样式</a>
        </li>
        <li v-if="localViewMode !== 'view' && !isHistoryView" class="auto-save-item">
          <span class="countdown-text">
            {{ countdownSeconds }}秒后自动保存
          </span>
        </li>
        <li v-if="localViewMode !== 'view' && !isHistoryView">
          <a href="javascript:;" class="btn-save" @click="manualSaveMindmap" title="保存">保存</a>
        </li>
        <li v-if="!isHistoryView" class="mode-switcher">
          <a href="javascript:;" class="btn-mode" @click="toggleEditMode" :title="modeButtonTitle">
            {{ modeButtonText }}
          </a>
        </li>
        <li>
          <a href="javascript:;" class="btn-back" @click="backToList" title="返回列表">返回列表</a>
        </li>
        <li class="history-item">
          <a href="javascript:;" class="btn-history" @click="viewHistory" title="查看历史保存记录">查看历史保存记录</a>
        </li>
        <li class="fullscreen-item">
          <a href="javascript:;" class="btn-fullscreen" @click="toggleFullScreen" :title="isFullScreen ? '恢复头部栏' : '隐藏头部栏'">
            <i :class="isFullScreen ? 'el-icon-view' : 'el-icon-remove-outline'"></i>
            <span class="fulltext">{{ isFullScreen ? '恢复头部栏' : '隐藏头部栏' }}</span>
            <span class="shorttext">{{ isFullScreen ? '显示' : '隐藏' }}</span>
          </a>
        </li>
        <li class="fullscreen-item">
          <a href="javascript:;" class="btn-fullscreen-map" @click="toggleMapFullScreen" :title="isMapFullScreen ? '恢复页面布局' : '全屏显示'">
            <i :class="isMapFullScreen ? 'el-icon-close' : 'el-icon-full-screen'"></i>
            <span class="fulltext">{{ isMapFullScreen ? '恢复页面' : '全屏显示' }}</span>
            <span class="shorttext">{{ isMapFullScreen ? '还原' : '全屏' }}</span>
          </a>
        </li>
      </ul>
      <div id="mind_tab-content">
        <div class="mind-tab-panel" v-show="switchShow.showEditMenu">
          <edit-menu></edit-menu>
        </div>
        <div class="mind-tab-panel" v-show="switchShow.showViewMenu">
          <view-menu></view-menu>
        </div>
      </div>
      
      <!-- 历史记录弹窗 -->
      <el-dialog title="历史保存记录" :visible.sync="historyDialogVisible" width="60%" :close-on-click-modal="false">
        <el-table v-loading="historyLoading" :data="paginatedHistoryList">
          <el-table-column label="版本ID" align="center" prop="id" width="80" />
          <el-table-column label="版本描述" align="center" prop="description" :show-overflow-tooltip="true" />
          <el-table-column label="创建人" align="center" prop="createBy" width="100" />
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-view"
                @click="openInNewPage(scope.row)"
              >预览</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-refresh-right"
                @click="restoreToVersion(scope.row)"
              >恢复到此版本</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <pagination
            v-show="totalHistory>0"
            :total="totalHistory"
            :page.sync="historyQuery.pageNum"
            :limit.sync="historyQuery.pageSize"
            @pagination="handleHistoryPagination"
          />
        </div>
        
        <!-- 隐藏的链接容器 -->
        <div class="hidden-links-container" style="display: none;"></div>
      </el-dialog>
    </header>
  </div>
</template>

<script>
  import editMenu from './menu/edit/editMenu'
  import viewMenu from './menu/view/viewMenu'
  import { mapGetters } from 'vuex'
  import { createHistory, getMindmapHistoryList } from '@/api/testcase/history'
  import { parseTime } from '@/utils/ruoyi'
  import { saveMindmapData } from '@/api/testcase/mindmap'
  
  export default {
    name: 'headerVue',
    data() {
      return {
        switchShow: {
          showEditMenu: true,
          showViewMenu: false
        },
        countdownSeconds: 300,
        autoSaveTimer: null,
        countdownTimer: null,
        historyDialogVisible: false,
        historyLoading: false,
        historyList: [],
        historyQuery: {
          pageNum: 1,
          pageSize: 10
        },
        totalHistory: 0,
        paginatedHistoryList: [],
        localViewMode: 'view',
        modeButtonTitle: '切换到编辑模式',
        modeButtonText: '编辑模式',
        isHistoryView: false,
        _isModeSwitching: false,
        isFullScreen: false,
        _browserFullscreenExit: false,
        isMapFullScreen: false
      }
    },
    computed: {
      ...mapGetters(['getViewMode']),
      viewMode() {
        return this.getViewMode
      },
      getNodeEditMode() {
        return this.$store.getters.getNodeEditMode;
      }
    },
    components: {
      editMenu,
      viewMenu
    },
    created() {
      const { version } = this.$route.query;
      this.isHistoryView = !!version;
      
      this.localViewMode = 'view';
      
      this.$store.commit('SET_VIEW_MODE', this.localViewMode);
      
      this.updateModeButton();
    },
    mounted() {
      this.updateModeButton();
      window.addEventListener("resize", this.handleResize);
      this.handleResize();
      
      // 确保没有遗留的退出全屏按钮
      const exitButtons = document.querySelectorAll('.custom-exit-fullscreen-btn');
      exitButtons.forEach(btn => {
        btn.remove();
      });
      
      if (this.localViewMode !== 'view') {
        this.startAutoSaveCountdown();
      }
    },
    watch: {
      localViewMode(newVal) {
        if (newVal !== 'view') {
          this.startAutoSaveCountdown();
        } else {
          if (this.countdownTimer) {
            clearInterval(this.countdownTimer);
          }
          if (this.autoSaveTimer) {
            clearTimeout(this.autoSaveTimer);
          }
        }
        
        this.$store.commit('SET_VIEW_MODE', newVal);
        
        this.$root.$emit('toggle-edit-mode', newVal);
      }
    },
    beforeDestroy() {
      // 确保清除所有计时器
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
        this.countdownTimer = null;
      }
      
      if (this.autoSaveTimer) {
        clearTimeout(this.autoSaveTimer);
        this.autoSaveTimer = null;
      }
      
      // 移除resize事件监听
      window.removeEventListener("resize", this.handleResize);
      
      // 确保移除全屏状态相关元素
      const headerElement = document.querySelector('.container header');
      if (headerElement && this.isFullScreen) {
        headerElement.style.display = '';
      }
      
      // 移除可能存在的退出按钮
      const exitButtons = document.querySelectorAll('.custom-exit-fullscreen-btn');
      exitButtons.forEach(btn => {
        btn.remove();
      });
      
      // 清理脑图全屏模式
      if (this.isMapFullScreen) {
        // 退出浏览器全屏
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.mozCancelFullScreen) {
          document.mozCancelFullScreen();
        } else if (document.webkitCancelFullScreen) {
          document.webkitCancelFullScreen();
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen();
        }
        
        document.body.classList.remove('map-fullscreen-mode');
        document.body.classList.remove('fullscreen');
        
        // 移除全屏状态监听
        document.removeEventListener('fullscreenchange', this.handleMapFullscreenChange);
        document.removeEventListener('webkitfullscreenchange', this.handleMapFullscreenChange);
        document.removeEventListener('mozfullscreenchange', this.handleMapFullscreenChange);
        document.removeEventListener('MSFullscreenChange', this.handleMapFullscreenChange);
      }
    },
    methods: {
      showMenu: function (e) {
        for (var variable in this.switchShow) {
          if (this.switchShow.hasOwnProperty(variable)) {
            this.switchShow[variable] = false
          }
        }
        this['switchShow'][e.target.className.replace('btn-', '')] = true
      },
      startAutoSaveCountdown() {
        // 重置倒计时
        this.countdownSeconds = 300;
        
        // 清除现有的计时器
        if (this.countdownTimer) {
          clearInterval(this.countdownTimer);
          this.countdownTimer = null;
        }
        
        if (this.autoSaveTimer) {
          clearTimeout(this.autoSaveTimer);
          this.autoSaveTimer = null;
        }
        
        // 设置UI倒计时更新器
        this.countdownTimer = setInterval(() => {
          this.countdownSeconds -= 1;
          
          // 当倒计时结束时
          if (this.countdownSeconds <= 0) {
            // 保存脑图
            this.saveMindmap();
            this.createHistory('自动保存版本');
            
            // 保存完成后，重新开始倒计时
            this.countdownSeconds = 300;
          }
        }, 1000);
        
        // 设置自动保存的备份保障 - 确保即使UI计时器失效也能触发保存
        // 设置为比倒计时略长一点的时间，避免重复保存
        this.autoSaveTimer = setTimeout(() => {
          // 检查倒计时是否已经很接近0，如果是，说明常规计时器即将触发保存
          // 只有当倒计时还有超过10秒时才执行备份保存
          if (this.countdownSeconds > 10) {
            this.saveMindmap();
            this.createHistory('自动保存版本');
          }
          
          // 无论是否保存，都重新启动倒计时
          this.startAutoSaveCountdown();
        }, 305000); // 设置为比倒计时长5秒，作为一个安全网
      },
      manualSaveMindmap() {
        this.saveMindmap();
        this.createHistory('手动保存版本');
      },
      saveMindmap() {
        // 获取脑图数据
        const mindmapData = this.$store.getters.getMindmapData;
        const mindmapId = this.$route.query.id;
        
        if (!mindmapId) {
          this.$modal.msgWarning('请先创建脑图');
          return;
        }
        
        // 将对象转换为JSON字符串
        const contentStr = typeof mindmapData === 'string' ? mindmapData : JSON.stringify(mindmapData);
        
        // 构造保存参数
        const params = {
          id: mindmapId,
          content: contentStr
        };
        
        // 直接调用API函数
        saveMindmapData(params).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("保存成功");
          } else {
            this.$modal.msgError("保存失败：" + response.msg);
          }
        }).catch(error => {
          this.$modal.msgError("保存失败: " + (error.message || "未知错误"));
        });
        
        this.startAutoSaveCountdown();
      },
      createHistory(description) {// 保存
      // // 获取脑图数据
        const mindmapData = this.$store.getters.getMindmapData;
        const mindmapId = this.$route.query.id;
        
        if (!mindmapId) {
          this.$modal.msgWarning('请先创建脑图');
          return;
        }
        // 将对象转换为JSON字符串
        const contentStr = typeof mindmapData === 'string' ? mindmapData : JSON.stringify(mindmapData);

        createHistory(mindmapId, contentStr, description).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("保存历史成功");
          } else {
            this.$modal.msgError("保存历史失败：" + response.msg);
          }
        }).catch(error => {
          this.$modal.msgError("保存历史失败: " + (error.message || "未知错误"));
        });
      },
      backToList() {
        const mindmapData = this.$store.getters.getMindmapData;
        const mindmapId = this.$route.query.id;
        const folderId = this.$route.query.folderId;
        
        // 如果是查看模式，无需保存，直接返回
        if (this.localViewMode === 'view') {
          this.$router.push({ 
            path: '/testcase/list',
            query: { folderId: folderId }
          });
          return;
        }
        
        // 保存数据
        const contentStr = typeof mindmapData === 'string' ? mindmapData : JSON.stringify(mindmapData);
        
        const params = {
          id: mindmapId,
          content: contentStr
        };
        
        saveMindmapData(params).then(() => {
          // 无论成功失败都返回列表
          this.$router.push({ 
            path: '/testcase/list',
            query: { folderId: folderId }
          });
        }).catch(() => {
          // 发生错误也返回列表
          this.$router.push({ 
            path: '/testcase/list',
            query: { folderId: folderId }
          });
        });
      },
      viewHistory() {
        const mindmapId = this.$route.query.id;
        if (!mindmapId) {
          this.$modal.msgError('请先保存脑图后再查看历史记录');
          return;
        }
        
        this.historyDialogVisible = true;
        this.loadHistoryData(mindmapId);
      },
      loadHistoryData(mindmapId) {
        this.historyLoading = true;
        getMindmapHistoryList(mindmapId).then(response => {
          if (response.code === 200) {
            this.historyList = response.rows;
            this.totalHistory = this.historyList.length;
            this.updatePaginatedList();
          } else {
            this.$modal.msgError('获取历史记录失败：' + response.msg);
          }
          this.historyLoading = false;
        }).catch(error => {
          console.error('获取历史记录异常：', error);
          this.$modal.msgError('获取历史记录异常：' + error.message);
          this.historyLoading = false;
        });
      },
      updatePaginatedList() {
        const start = (this.historyQuery.pageNum - 1) * this.historyQuery.pageSize;
        const end = this.historyQuery.pageNum * this.historyQuery.pageSize;
        this.paginatedHistoryList = this.historyList.slice(start, end);
      },
      handleHistoryPagination(val) {
        this.historyQuery.pageNum = val.page;
        this.historyQuery.pageSize = val.limit;
        this.updatePaginatedList();
      },
      openInNewPage(record) {
        const mindmapId = this.$route.query.id;
        const editorUrl = `/testcase/editor?id=${mindmapId}&mode=view&version=${record.id}`;
        
        const container = document.querySelector('.hidden-links-container');
        if (container) {
          container.innerHTML = '';
          
          const a = document.createElement('a');
          a.href = editorUrl;
          a.target = '_blank';
          a.rel = 'noopener noreferrer';
          a.textContent = '在新页面打开';
          
          container.appendChild(a);
          
          a.click();
        } else {
          window.open(editorUrl, '_blank');
        }
      },
      toggleEditMode() {
        const currentMode = this.localViewMode;
        const newMode = currentMode === 'edit' ? 'view' : 'edit';
        
        // 防止递归状态更新，使用节流处理
        if (this._isModeSwitching) {
          return;
        }
        
        // 设置状态切换标志
        this._isModeSwitching = true;
        
        // 历史版本检查
        if (this.isHistoryView && newMode === 'edit') {
          this.$modal.msgWarning('历史版本为只读模式，不可编辑');
          this._isModeSwitching = false;
          return;
        }
        
        // 更新本地模式
        this.localViewMode = newMode;
        
        // 使用异步更新全局状态，防止递归渲染
        this.$nextTick(() => {
          // 更新全局状态 - 先后顺序很重要，避免同时触发多个响应式更新
          this.$store.commit('SET_VIEW_MODE', newMode);
          
          // 确保视图模式更新后再更新节点编辑模式
          this.$nextTick(() => {
            // 设置节点编辑模式
            this.$store.commit('SET_NODE_EDIT_MODE', newMode === 'edit');
            
            // 触发全局事件，确保所有组件同步更新
            this.$root.$emit('toggle-edit-mode', newMode);
            
            // 更新按钮样式
            this.updateModeButton();
            
            // 清除状态切换标志
            this._isModeSwitching = false;
          });
        });
      },
      updateModeButton() {
        this.modeButtonTitle = this.localViewMode === 'view' ? '切换到编辑模式' : '切换到查看模式';
        this.modeButtonText = this.localViewMode === 'view' ? '进入编辑模式' : '退出编辑模式';
        
        // 不再改变按钮颜色，始终保持红色
        this.$nextTick(() => {
          const btnMode = document.querySelector('.btn-mode');
          if (btnMode) {
            // 移除蓝色样式，确保始终使用默认的红色样式
            btnMode.classList.remove('exit-edit-mode');
          }
        });
      },
      handleResize() {
        // Implementation of handleResize method
      },
      restoreToVersion(record) {
        this.$confirm('确认要恢复到此版本吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const mindmapId = this.$route.query.id;
          // 获取历史记录内容
          this.historyLoading = true;
          
          // 获取历史版本ID和内容
          const historyId = record.id;
          const historyContent = record.content;
          
          try {
            // 解析历史内容
            const parsedContent = JSON.parse(historyContent);
            
            // 更新当前页面的mindmap数据
            this.$store.commit('SET_MINDMAP_DATA', parsedContent);
            
            // 使用force-render-mindmap事件强制重新渲染脑图
            this.$root.$emit('force-render-mindmap', parsedContent);
            
            // 如果当前是编辑模式，确保节点编辑状态也被恢复
            if (this.localViewMode === 'edit') {
              // 设置为编辑模式
              this.$store.commit('SET_NODE_EDIT_MODE', true);
              // 确保编辑器正确加载编辑状态
              this.$nextTick(() => {
                this.$root.$emit('toggle-edit-mode', 'edit');
                this.$root.$emit('edit-mode-enabled');
              });
            }
            
            // 直接调用saveMindmap接口保存
            this.saveMindmap();
            // 关闭历史记录对话框
            this.historyDialogVisible = false;
            
            this.$modal.msgSuccess("恢复成功，脑图已更新");
          } catch (error) {
            this.$modal.msgError("恢复失败：" + (error.message || "数据格式错误"));
            console.error("恢复失败:", error);
          } finally {
            this.historyLoading = false;
          }
        }).catch(() => {
          // 用户取消操作
        });
      },
      toggleFullScreen() {
        // 修改为仅隐藏header区域的全屏模式
        this.isFullScreen = !this.isFullScreen;
        
        // 触发全屏状态变化事件
        this.$root.$emit('fullscreen-changed', this.isFullScreen);
        
        // 直接操作DOM元素隐藏header
        const headerElement = document.querySelector('.container header');
        
        if (headerElement) {
          if (this.isFullScreen) {
            // 直接使用style修改而不是class
            headerElement.style.display = 'none';
            
            // 添加退出全屏按钮
            this.updateExitButton();
          } else {
            headerElement.style.display = '';
            
            // 移除所有退出全屏按钮
            const exitButtons = document.querySelectorAll('.custom-exit-fullscreen-btn');
            exitButtons.forEach(btn => {
              btn.remove();
            });
          }
        } else {
          // 如果没找到头部元素
        }
      },
      updateExitButton() {
        // 移除可能已存在的退出按钮
        const existingButtons = document.querySelectorAll('.custom-exit-fullscreen-btn');
        existingButtons.forEach(btn => {
          btn.remove();
        });
        
        // 如果是全屏模式，添加退出按钮
        if (this.isFullScreen) {
          const button = document.createElement('button');
          button.className = 'custom-exit-fullscreen-btn';
          button.innerHTML = '<i class="el-icon-view"></i> 恢复头部栏';
          
          // 使用箭头函数保持this上下文
          button.addEventListener('click', () => {
            this.toggleFullScreen();
          });
          
          // 将按钮添加到body
          document.body.appendChild(button);
        }
      },
      fullScreenChangeHandler() {
        // 此方法仅处理浏览器API引起的变化
        const fullscreenElement = 
          document.fullscreenElement ||
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement;
        
        // 只处理浏览器全屏状态变化的情况
        const headerElement = document.querySelector('.container header');
        
        if (fullscreenElement && !this.isFullScreen) {
          this.isFullScreen = true;
          
          // 隐藏header
          if (headerElement) {
            headerElement.style.display = 'none';
          }
          
          // 添加退出按钮
          this.updateExitButton();
          
        } else if (!fullscreenElement && this.isFullScreen) {
          this.isFullScreen = false;
          
          // 显示header
          if (headerElement) {
            headerElement.style.display = '';
          }
          
          // 移除退出按钮
          const exitBtn = document.querySelector('.custom-exit-fullscreen-btn');
          if (exitBtn) {
            exitBtn.remove();
          }
        }
      },
      toggleMapFullScreen() {
        this.isMapFullScreen = !this.isMapFullScreen;
        
        // 触发全屏状态变化事件
        this.$root.$emit('map-fullscreen-changed', this.isMapFullScreen);
        
        if (this.isMapFullScreen) {
          // 如果当前在隐藏头部的全屏模式，先还原头部
          if (this.isFullScreen) {
            this.isFullScreen = false;
            const headerElement = document.querySelector('.container header');
            if (headerElement) {
              headerElement.style.display = '';
            }
            
            // 移除退出全屏按钮
            const exitButtons = document.querySelectorAll('.custom-exit-fullscreen-btn');
            exitButtons.forEach(btn => {
              btn.remove();
            });
          }
          
          // 使用浏览器原生全屏API
          const docElement = document.documentElement;
          if (docElement.requestFullscreen) {
            docElement.requestFullscreen();
          } else if (docElement.mozRequestFullScreen) {
            docElement.mozRequestFullScreen();
          } else if (docElement.webkitRequestFullscreen) {
            docElement.webkitRequestFullscreen();
          } else if (docElement.msRequestFullscreen) {
            docElement.msRequestFullscreen();
          }
          
          // 添加脑图全屏样式
          document.body.classList.add('map-fullscreen-mode');
          document.body.classList.add('fullscreen');
          
          // 添加全屏状态监听
          document.addEventListener('fullscreenchange', this.handleMapFullscreenChange);
          document.addEventListener('webkitfullscreenchange', this.handleMapFullscreenChange);
          document.addEventListener('mozfullscreenchange', this.handleMapFullscreenChange);
          document.addEventListener('MSFullscreenChange', this.handleMapFullscreenChange);
          
          // 更改按钮状态和文本
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .fulltext').forEach(el => {
            el.textContent = '退出全屏';
          });
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .shorttext').forEach(el => {
            el.textContent = '退出';
          });
        } else {
          // 退出浏览器全屏
          if (document.exitFullscreen) {
            document.exitFullscreen();
          } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
          } else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
          } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
          }
          
          // 移除脑图全屏样式
          document.body.classList.remove('map-fullscreen-mode');
          document.body.classList.remove('fullscreen');
          
          // 移除全屏状态监听
          document.removeEventListener('fullscreenchange', this.handleMapFullscreenChange);
          document.removeEventListener('webkitfullscreenchange', this.handleMapFullscreenChange);
          document.removeEventListener('mozfullscreenchange', this.handleMapFullscreenChange);
          document.removeEventListener('MSFullscreenChange', this.handleMapFullscreenChange);
          
          // 还原按钮状态和文本
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .fulltext').forEach(el => {
            el.textContent = '全屏显示';
          });
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .shorttext').forEach(el => {
            el.textContent = '全屏';
          });
        }
      },
      handleMapFullscreenChange() {
        // 检测浏览器全屏状态
        const isFullscreenActive = !!(
          document.fullscreenElement || 
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement
        );
        
        // 如果浏览器退出全屏但我们的状态仍然是全屏，则同步状态
        if (!isFullscreenActive && this.isMapFullScreen) {
          this.isMapFullScreen = false;
          document.body.classList.remove('map-fullscreen-mode');
          document.body.classList.remove('fullscreen');
          
          // 还原按钮状态和文本
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .fulltext').forEach(el => {
            el.textContent = '全屏显示';
          });
          document.querySelectorAll('.fullscreen-item .btn-fullscreen-map .shorttext').forEach(el => {
            el.textContent = '全屏';
          });
        }
      }
    }
  }
</script>

<style lang="scss">
  @import "../../style/header.scss";
  
  .auto-save-item {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 140px;
    padding: 0 15px;
    flex-grow: 0;
    flex-shrink: 0;
    
    .countdown-text {
      font-size: 14px;
      color: #2ecc71;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      font-weight: 500;
    }
  }
  
  @media screen and (max-width: 768px) {
    .auto-save-item {
      min-width: 100px;
      padding: 0 8px;
      
      .countdown-text {
        font-size: 12px;
      }
    }
  }
  
  @media screen and (max-width: 480px) {
    #mind_tab {
      display: flex;
      flex-wrap: wrap;
      
      li {
        flex: 0 0 auto;
      }
      
      .auto-save-item {
        order: 1;
      }
    }
  }
  
  /* 统一按钮样式 */
  .btn-back, .btn-history, .btn-fullscreen, .btn-fullscreen-map {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f2f2f2 !important; /* 使用!important确保覆盖其他样式 */
    color: #606266 !important;
    padding: 4px 12px;
    border-radius: 4px;
    transition: all 0.3s;
    text-align: center;
    border: 1px solid #dcdfe6;
    
    i {
      margin-right: 5px;
      font-size: 16px;
    }
    
    &:hover {
      background-color: #e6e6e6 !important;
      color: #303133 !important;
    }
  }
  
  /* 特别指定 #mind_tab li .btn-back 的样式，覆盖 header.scss 中的定义 */
  #mind_tab li .btn-back {
    background-color: #f2f2f2 !important;
    color: #606266 !important;
    
    &:hover {
      background-color: #e6e6e6 !important;
      color: #303133 !important;
    }
  }
  
  .history-item {
    flex: 0 0 auto !important;
    min-width: 140px;
    overflow: visible !important;
    margin: 0 5px !important;
    box-sizing: content-box;
  }
  
  .btn-history {
    display: block;
    width: 100%;
    white-space: nowrap;
    overflow: visible !important;
    box-sizing: border-box;
  }
  
  /* 模式切换按钮样式 */
  .mode-switcher {
    flex: 0 0 auto !important;
    min-width: 120px;
    overflow: visible !important;
    margin: 0 5px !important;
    box-sizing: content-box;
  }
  
  .btn-mode {
    color: #fff;
    background-color: #F56C6C; /* 默认红色（编辑模式按钮） */
    padding: 4px 12px;
    border-radius: 4px;
    transition: all 0.3s;
    display: block;
    width: 100%;
    text-align: center;
    white-space: nowrap;
    overflow: visible !important;
    box-sizing: border-box;
  }
  
  /* 退出编辑模式按钮使用蓝色 */
  .exit-edit-mode {
    background-color: #409EFF;
  }
  
  .btn-mode:hover {
    color: #fff;
    filter: brightness(1.1);
  }
  
  #mind_tab {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }
  
  #mind_tab li {
    float: none;
    flex: 0 0 auto;
    margin: 2px 3px;
  }
  
  .pagination-container {
    margin-top: 15px;
    padding: 10px 0;
    text-align: right;
  }
  
  @media screen and (max-width: 768px) {    
    .history-item {
      width: auto;
      min-width: 150px;
      margin-top: 5px !important;
    }
    
    .btn-back, .btn-history, .btn-mode, .btn-fullscreen, .btn-fullscreen-map {
      font-size: 12px;
      padding: 3px 8px;
    }
    
    .mode-switcher {
      min-width: 100px;
      margin-top: 5px !important;
    }
  }
  
  // 全屏按钮样式
  .fullscreen-item {
    flex: 0 0 auto !important;
    min-width: 120px;
    overflow: visible !important;
    margin: 0 5px !important;
    box-sizing: content-box;
    
    .btn-fullscreen, .btn-fullscreen-map {
      .shorttext {
        display: none;
      }
    }
  }
  
  @media screen and (max-width: 768px) {    
    .history-item {
      width: auto;
      min-width: 150px;
      margin-top: 5px !important;
    }
    
    .mode-switcher {
      min-width: 100px;
      margin-top: 5px !important;
    }
    
    .fullscreen-item {
      min-width: 100px;
      margin-top: 5px !important;
      
      .btn-fullscreen, .btn-fullscreen-map {
        .fulltext {
          display: none;
        }
        
        .shorttext {
          display: inline;
        }
      }
    }
  }
  
  @media screen and (max-width: 480px) {
    .fullscreen-item {
      min-width: 60px;
      
      .btn-fullscreen i, .btn-fullscreen-map i {
        margin-right: 2px;
      }
    }
  }
</style>

<style lang="scss">
/* 动态创建的退出全屏按钮样式 */
.custom-exit-fullscreen-btn {
  position: fixed;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  background: rgba(64, 158, 255, 0.8);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  
  i {
    margin-right: 5px;
  }
  
  &:hover {
    background: #409eff;
  }
}

/* 脑图全屏模式样式 */
.map-fullscreen-mode {
  /* 隐藏所有非必要元素 */
  .navbar, 
  .sidebar-container,
  .tags-view-container,
  .app-main > div > header,
  #mind_tab-content,
  .container > nav,
  .app-main .fixed-header {
    display: none !important;
  }
  
  /* 调整脑图容器 */
  .app-main {
    padding: 0 !important;
    margin: 0 !important;
  }
  
  /* 让脑图占据整个视口 */
  .testcase {
    margin: 0 !important;
    padding: 0 !important;
    border-radius: 0 !important;
    border: none !important;
    
    .main-editor {
      height: 100vh !important;
      max-height: none !important;
    }
    
    .editor-panel {
      height: 100% !important;
    }
  }
  
  /* 确保所有菜单容器在全屏模式下撑满宽度 */
  #mind_tab-content {
    display: block !important;
    width: 100% !important;
    max-width: 100% !important;
    overflow: visible !important;
  }
  
  .mind-tab-panel {
    width: 100% !important;
    max-width: 100% !important;
    overflow: visible !important;
  }
  
  .menu-container {
    width: 100% !important;
    max-width: 100% !important;
    display: flex !important;
    flex-wrap: wrap !important;
    justify-content: flex-start !important;
    overflow: visible !important;
  }
  
  /* 修复菜单容器内部元素的布局 */
  .menu-container > div {
    width: auto !important;
    min-width: auto !important;
    flex: 0 0 auto !important;
    display: flex !important;
    justify-content: flex-start !important;
    padding: 0 10px !important;
    border-right: none !important;
  }
  
  /* 菜单按钮和选项适应宽度 */
  .menu-btn, 
  .el-dropdown, 
  .el-select,
  .dropdown-toggle {
    width: auto !important;
    min-width: auto !important;
    margin: 0 8px !important;
  }
  
  /* 确保按钮文本和图标正确显示 */
  .menu-btn span,
  .menu-btn i,
  .label {
    display: inline-block !important;
    white-space: nowrap !important;
  }
  
  /* 确保在全屏模式下脑图撑满整个屏幕 */
  &.fullscreen {
    /* 全局容器适应全屏 */
    .container, 
    .app-main,
    .main-container,
    .testcase,
    .main-editor,
    .editor-panel {
      width: 100vw !important;
      height: 100vh !important;
      max-width: 100vw !important;
      max-height: 100vh !important;
      overflow: hidden !important;
    }
    
    /* 让菜单面板也参与全屏计算 */
    #mind_tab-content {
      display: block !important;
      width: 100vw !important;
      max-width: 100vw !important;
      overflow: visible !important;
    }
    
    .mind-tab-panel {
      width: 100vw !important;
      max-width: 100vw !important;
      overflow: visible !important;
    }
    
    /* 确保菜单容器正确布局 */
    .menu-container {
      width: 100% !important;
      max-width: 100% !important;
      display: flex !important;
      flex-wrap: wrap !important;
      justify-content: flex-start !important;
      overflow: visible !important;
    }
    
    /* 特别处理font-group等特殊菜单组 */
    .font-group,
    .style-group,
    .arrange-group {
      display: flex !important;
      flex-wrap: wrap !important;
      width: auto !important;
      justify-content: flex-start !important;
    }
    
    /* 修复expand-group在全屏模式下的宽度问题 */
    .expand-group,
    .selection-group {
      width: 60px !important;
      min-width: 60px !important;
      max-width: 60px !important;
      flex: 0 0 60px !important;
    }
    
    /* 为其他菜单组件设置固定宽度 */
    .edit-del-group,
    .move-group {
      width: 70px !important;
      min-width: 70px !important;
      max-width: 70px !important;
      flex: 0 0 70px !important;
    }
    
    .sequence-group,
    .progress-group {
      width: 150px !important;
      min-width: 150px !important;
      max-width: 150px !important;
      flex: 0 0 150px !important;
      overflow: visible !important;
      position: relative !important;
      padding: 5px !important;
      box-sizing: border-box !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      
      /* 确保ul和li元素可见 */
      ul {
        display: grid !important;
        grid-template-columns: repeat(5, 20px) !important;
        grid-template-rows: repeat(2, 20px) !important;
        grid-gap: 2px !important;
        width: 120px !important;
        padding: 0 !important;
        margin: 0 auto !important;
        list-style: none !important;
        visibility: visible !important;
        z-index: 10 !important;
        position: relative !important;
      }
      
      li {
        display: inline-block !important;
        width: 20px !important;
        height: 20px !important;
        margin: 0 !important;
        visibility: visible !important;
        opacity: 1 !important;
        z-index: 11 !important;
        cursor: pointer !important;
      }
    }
    
    /* 确保序列和进度图标正确显示 */
    .sequence-group li {
      background-image: url("~@/assets/minder/iconpriority.png") !important;
    }
    
    .progress-group li {
      background-image: url("~@/assets/minder/iconprogress.png") !important;
    }
    
    /* 序列图标定位 */
    @for $i from 0 through 9 {
      .sequence-group .sequence-#{$i} {
        background-position: 0 -20px * (-1 + $i) !important;
      }
    }
    
    /* 进度图标定位 */
    @for $i from 0 through 9 {
      .progress-group .progress-#{$i} {
        background-position: 0 -20px * (-1 + $i) !important;
      }
    }
    
    /* 特别处理insert-group组件 */
    .insert-group {
      width: 110px !important;
      min-width: 110px !important;
      max-width: 110px !important;
      flex: 0 0 110px !important;
    }
    
    /* 确保下拉菜单正确显示 */
    .el-scrollbar, 
    .el-select-dropdown, 
    .el-dropdown-menu {
      z-index: 3000 !important;
    }
    
    /* 强制子菜单显示 */
    .el-submenu__title, 
    .el-menu-item {
      width: auto !important;
      min-width: auto !important;
    }
    
    /* 确保下拉菜单正确展开 */
    .el-select-dropdown__wrap,
    .el-scrollbar__wrap {
      max-width: 100vw !important;
      margin-bottom: 0 !important;
    }
  }
}

/* 全屏模式下的顶级组件样式 - 由于CSS作用域问题,需要从body开始选择 */
body.map-fullscreen-mode #app .main-container {
  margin-left: 0 !important;
  width: 100vw !important;
}

body.map-fullscreen-mode #app {
  height: 100vh !important;
  width: 100vw !important;
  overflow: hidden !important;
}

body.map-fullscreen-mode #app .container {
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  overflow: hidden !important;
}

/* 全屏模式下的菜单容器强制样式 */
body.map-fullscreen-mode #app .container header #mind_tab-content {
  display: block !important;
}

body.map-fullscreen-mode #app .container header .mind-tab-panel {
  height: auto !important;
  width: 100% !important;
}

body.map-fullscreen-mode #app .container header .menu-container {
  width: 100% !important;
  display: flex !important;
  flex-wrap: wrap !important;
}

body.map-fullscreen-mode #app .container header .menu-container > div {
  width: auto !important;
  min-width: auto !important;
  flex: 0 0 auto !important;
  display: flex !important;
  justify-content: flex-start !important;
  padding: 0 10px !important;
  border-right: none !important;
}

/* 修复全屏模式下特定组件的宽度问题 */
body.map-fullscreen-mode #app .container header .menu-container .expand-group,
body.map-fullscreen-mode #app .container header .menu-container .selection-group,
body.map-fullscreen-mode #app .container header .menu-container .edit-del-group,
body.map-fullscreen-mode #app .container header .menu-container .move-group,
body.map-fullscreen-mode #app .container header .menu-container .sequence-group,
body.map-fullscreen-mode #app .container header .menu-container .progress-group,
body.map-fullscreen-mode #app .container header .menu-container .insert-group {
  width: auto !important;
  min-width: auto !important;
  max-width: 150px !important;
  flex: 0 0 auto !important;
}

/* 针对各个组件的特定宽度 */
body.map-fullscreen-mode #app .container header .menu-container .expand-group,
body.map-fullscreen-mode #app .container header .menu-container .selection-group {
  max-width: 60px !important;
  flex: 0 0 60px !important;
}

body.map-fullscreen-mode #app .container header .menu-container .edit-del-group,
body.map-fullscreen-mode #app .container header .menu-container .move-group {
  max-width: 70px !important;
  flex: 0 0 70px !important;
}

body.map-fullscreen-mode #app .container header .menu-container .sequence-group,
body.map-fullscreen-mode #app .container header .menu-container .progress-group {
  width: 150px !important;
  min-width: 150px !important;
  max-width: 150px !important;
  flex: 0 0 150px !important;
  overflow: visible !important;
  position: relative !important;
  padding: 5px !important;
  box-sizing: border-box !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  
  /* 确保ul和li元素可见 */
  ul {
    display: grid !important;
    grid-template-columns: repeat(5, 20px) !important;
    grid-template-rows: repeat(2, 20px) !important;
    grid-gap: 2px !important;
    width: 120px !important;
    padding: 0 !important;
    margin: 0 auto !important;
    list-style: none !important;
    visibility: visible !important;
    z-index: 10 !important;
    position: relative !important;
  }
  
  li {
    display: inline-block !important;
    width: 20px !important;
    height: 20px !important;
    margin: 0 !important;
    visibility: visible !important;
    opacity: 1 !important;
    z-index: 11 !important;
    cursor: pointer !important;
  }
}

/* 特别处理insert-group组件 */
body.map-fullscreen-mode #app .container header .menu-container .insert-group {
  width: 110px !important;
  min-width: 110px !important;
  max-width: 110px !important;
  flex: 0 0 110px !important;
}

/* 确保序列和进度图标正确显示 */
body.map-fullscreen-mode #app .container header .menu-container .sequence-group li {
  background-image: url("~@/assets/minder/iconpriority.png") !important;
}

body.map-fullscreen-mode #app .container header .menu-container .progress-group li {
  background-image: url("~@/assets/minder/iconprogress.png") !important;
}

/* 序列图标定位 */
@for $i from 0 through 9 {
  body.map-fullscreen-mode #app .container header .menu-container .sequence-group .sequence-#{$i} {
    background-position: 0 -20px * (-1 + $i) !important;
  }
}

/* 进度图标定位 */
@for $i from 0 through 9 {
  body.map-fullscreen-mode #app .container header .menu-container .progress-group .progress-#{$i} {
    background-position: 0 -20px * (-1 + $i) !important;
  }
}

/* 脑图全屏退出按钮 */
.map-exit-fullscreen-btn {
  position: fixed;
  top: 10px;
  right: 10px;
  z-index: 9999;
  background: rgba(64, 158, 255, 0.8);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  
  i {
    margin-right: 5px;
  }
  
  &:hover {
    background: #409eff;
  }
}
</style>
