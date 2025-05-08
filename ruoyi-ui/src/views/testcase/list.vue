<template>
  <div class="app-container">
    <div class="app-container-inner">
      <!-- 左侧文件列表导航 -->
      <div class="file-nav">
        <div class="file-nav-header">
          <div class="file-nav-header-inner">
            <el-input
              placeholder="搜索类别"
              v-model="searchFileName"
              size="small"
              suffix-icon="el-icon-search"
              clearable
            ></el-input>
            <el-button
              type="text"
              icon="el-icon-plus"
              size="mini"
              @click="handleAddRootFolder"
              class="add-folder-btn"
            ></el-button>
          </div>
        </div>
        <div class="file-nav-content">
          <el-tree
            :data="fileList"
            :props="defaultProps"
            node-key="id"
            :expand-on-click-node="false"
            default-expand-all
            @node-click="handleNodeClick"
            :filter-node-method="filterNode"
            ref="fileTree"
            highlight-current
          >
            <span class="custom-tree-node" slot-scope="{ node, data }">
              <span class="node-label">
                <i :class="[data.children && data.children.length > 0 ? 'el-icon-folder' : 'el-icon-document', 'node-icon']"></i>
                {{ node.label }}
              </span>
              <span class="action-buttons">
                <el-dropdown trigger="click" @command="handleFileCommand($event, data)">
                  <span class="el-dropdown-link">
                    <i class="el-icon-more"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="addSibling">
                      <i class="el-icon-plus menu-icon"></i>添加同级文件夹
                    </el-dropdown-item>
                    <el-dropdown-item command="add">
                      <i class="el-icon-folder-add menu-icon"></i>添加子文件夹
                    </el-dropdown-item>
                    <el-dropdown-item command="rename">
                      <i class="el-icon-edit menu-icon"></i>重命名
                    </el-dropdown-item>
                    <el-dropdown-item command="delete">
                      <i class="el-icon-delete menu-icon"></i>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </span>
            </span>
          </el-tree>
        </div>
      </div>

      <!-- 右侧主内容区 -->
      <div class="main-content">
        <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用例编号" prop="id">
            <el-input
              v-model="queryParams.id"
              placeholder="请输入用例编号"
              clearable
              size="small"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入用例名称"
              clearable
              size="small"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="dateRange"
              size="small"
              style="width: 240px"
              value-format="yyyy-MM-dd"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
            >新建用例</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete"
            >删除</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="mindmapList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="用例编号" align="center" prop="id" width="120" />
          <el-table-column label="用例名称" align="center" prop="name" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              <el-link type="primary" :underline="false" @click="handleEdit(scope.row)">{{ scope.row.name }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="关联需求" align="center" prop="requirement" :show-overflow-tooltip="true" />
          <el-table-column label="最近更新人" align="center" prop="updateBy" width="120" />
          <el-table-column label="创建人" align="center" prop="createBy" width="120" />
          <el-table-column label="负责人" align="center" prop="owner" width="120" />
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button
                size="mini" 
                type="text"
                icon="el-icon-view"
                @click="handleView(scope.row)"
              >查看</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </div>

    <!-- 文件夹操作对话框 -->
    <el-dialog :title="folderDialogTitle" :visible.sync="folderDialogVisible" width="40%" append-to-body>
      <el-form ref="folderForm" :model="folderForm" :rules="folderRules" label-width="100px">
        <el-form-item label="文件夹名称" prop="name">
          <el-input v-model="folderForm.name" placeholder="请输入文件夹名称" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="folderDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitFolderForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 新建测试用例对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="40%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用例名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入用例名称" />
        </el-form-item>
        <el-form-item label="关联需求" prop="requirement">
          <el-input v-model="form.requirement" placeholder="请输入关联需求链接" />
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="form.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="用例文件夹" prop="folderId">
          <el-input v-model="currentFolderName" placeholder="请先在左侧选择文件夹" readonly />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMindmap, delMindmap, addMindmap } from "@/api/testcase/mindmap";
import { getFolderTree, addFolder, updateFolder, deleteFolder, getMindmapListByFolder } from "@/api/testcase/folder";

export default {
  name: "Mindmap",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 脑图表格数据
      mindmapList: [],
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        id: undefined,
        name: undefined,
        folderId: undefined // 确保此字段存在
      },
      // 文件列表搜索
      searchFileName: "",
      // 文件列表数据
      fileList: [],
      // 树形配置选项
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 文件夹操作对话框
      folderDialogVisible: false,
      folderDialogTitle: "",
      folderOperationType: "", // add, rename
      currentNode: null,
      folderForm: {
        name: "",
        parentId: null
      },
      folderRules: {
        name: [
          { required: true, message: "文件夹名称不能为空", trigger: "blur" }
        ]
      },
      // 表单参数
      form: {
        name: undefined,
        requirement: undefined,
        owner: undefined,
        folderId: undefined
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "用例名称不能为空", trigger: "blur" }
        ]
      },
      // 是否显示弹出层
      open: false,
      // 弹出层标题
      title: "",
    };
  },
  computed: {
    currentFolderName() {
      if (!this.form.folderId) {
        return "";
      }
      
      const findFolderName = (nodes, id) => {
        for (let node of nodes) {
          if (node.id == id) {
            return node.label;
          }
          if (node.children && node.children.length > 0) {
            const name = findFolderName(node.children, id);
            if (name) return name;
          }
        }
        return null;
      };
      
      const folderName = findFolderName(this.fileList, this.form.folderId);
      return folderName || "未知文件夹";
    }
  },
  watch: {
    searchFileName(val) {
      this.$refs.fileTree.filter(val);
    }
  },
  created() {
    // 从路由参数中获取文件夹ID
    if (this.$route.query.folderId) {
      this.queryParams.folderId = parseInt(this.$route.query.folderId);
    }
    this.getFolderTree();
    // 获取文件夹树后再获取列表
  },
  methods: {
    /** 查询脑图列表 */
    getList() {
      this.loading = true;
      if (this.queryParams.folderId) {
        // 如果选中了文件夹，从该文件夹获取列表
        getMindmapListByFolder(this.queryParams.folderId, this.queryParams).then(response => {
          if (response.code === 200) {
            this.mindmapList = response.rows || [];
            this.total = response.total || 0;
          } else {
            console.error(`获取文件夹脑图列表失败: ${response.msg}`);
            this.$message.error("获取脑图列表失败: " + (response.msg || '未知错误'));
            this.mindmapList = [];
            this.total = 0;
          }
        this.loading = false;
        }).catch(error => {
          console.error(`获取文件夹脑图列表出错: ${error.message}`);
          this.$message.error("获取脑图列表出错: " + error.message);
          this.mindmapList = [];
          this.total = 0;
          this.loading = false;
        });
      } else {
        // 否则获取所有列表
        listMindmap(this.queryParams).then(response => {
          if (response.code === 200) {
            this.mindmapList = response.rows || [];
            this.total = response.total || 0;
          } else {
            console.error(`获取脑图列表失败: ${response.msg}`);
            this.$message.error("获取脑图列表失败: " + (response.msg || '未知错误'));
            this.mindmapList = [];
            this.total = 0;
          }
          this.loading = false;
        }).catch(error => {
          console.error(`获取脑图列表出错: ${error.message}`);
          this.$message.error("获取脑图列表出错: " + error.message);
          this.mindmapList = [];
          this.total = 0;
          this.loading = false;
        });
      }
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        name: undefined,
        requirement: undefined,
        owner: undefined,
        folderId: this.queryParams.folderId || undefined
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      // 如果当前选中了文件夹，设置表单中的文件夹ID
      if (this.queryParams.folderId) {
        this.form.folderId = this.queryParams.folderId;
      }
      this.open = true;
      this.title = "新建测试用例";
    },
    /** 修改按钮操作 */
    handleEdit(row) {
      // 导航到编辑页面时传递当前选中的文件夹ID
      this.$router.push({ 
        path: `/testcase/editor`, 
        query: { 
          id: row.id, 
          folderId: this.queryParams.folderId,
        } 
      });
    },
    /** 查看按钮操作 */
    handleView(row) {
      // 导航到编辑页面时传递当前选中的文件夹ID
      this.$router.push({ 
        path: `/testcase/editor`, 
        query: { 
          id: row.id, 
          mode: 'view',
          folderId: this.queryParams.folderId 
        } 
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      if (!ids || (Array.isArray(ids) && ids.length === 0)) {
        this.$modal.msgError("请先选择要删除的数据");
        return;
      }
      
      this.$modal.confirm('是否确认删除脑图编号为"' + ids + '"的数据项?').then(() => {
        const loadingInstance = this.$loading({
          lock: true,
          text: '删除中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        return delMindmap(ids).then(response => {
          loadingInstance.close();
          if (response.code === 200) {
        this.getList();
        this.$modal.msgSuccess("删除成功");
          } else {
            this.$modal.msgError("删除失败: " + (response.msg || '未知错误'));
          }
        }).catch(error => {
          loadingInstance.close();
          console.error("删除脑图出错:", error);
          this.$modal.msgError("删除失败: " + error.message);
        });
      }).catch(() => {});
    },
    /** 过滤文件节点 */
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    /** 点击文件节点 */
    handleNodeClick(data) {
      // 设置当前选中的文件夹ID
      this.queryParams.folderId = data.id;
      // 重置分页
      this.queryParams.pageNum = 1;
      // 获取该文件夹下的脑图列表
      this.getList();
    },
    /** 处理文件操作 */
    handleFileCommand(command, data) {
      this.currentNode = data;
      if (command === 'add') {
        this.folderDialogTitle = "添加子文件夹";
        this.folderOperationType = "add";
        this.folderForm = {
          name: "",
          parentId: data.id
        };
        this.folderDialogVisible = true;
      } else if (command === 'addSibling') {
        this.folderDialogTitle = "添加同级文件夹";
        this.folderOperationType = "addSibling";
        // 查找父节点ID
        let parentId = this.findParentId(this.fileList, data.id);
        this.folderForm = {
          name: "",
          parentId: parentId || 0 // 如果没有父节点，则添加到根级
        };
        this.folderDialogVisible = true;
      } else if (command === 'rename') {
        this.folderDialogTitle = "重命名文件夹";
        this.folderOperationType = "rename";
        this.folderForm = {
          name: data.label.replace(/\(\d+\)$/, "").trim(),
          id: data.id
        };
        this.folderDialogVisible = true;
      } else if (command === 'delete') {
        this.handleDeleteFolder(data);
      }
    },
    /** 添加根级文件夹 */
    handleAddRootFolder() {
      this.folderDialogTitle = "添加文件夹";
      this.folderOperationType = "addRoot";
      this.folderForm = {
        name: "",
        parentId: 0 // 根级文件夹
      };
      this.folderDialogVisible = true;
    },
    /** 查找节点的父节点ID */
    findParentId(nodes, nodeId, parentId = null) {
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i].id === nodeId) {
          return parentId;
        }
        if (nodes[i].children && nodes[i].children.length > 0) {
          const result = this.findParentId(nodes[i].children, nodeId, nodes[i].id);
          if (result !== null) {
            return result;
          }
        }
      }
      return null;
    },
    /** 提交文件夹表单 */
    submitFolderForm() {
      this.$refs.folderForm.validate(valid => {
        if (valid) {
          const loadingInstance = this.$loading({
            lock: true,
            text: '处理中...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          if (this.folderOperationType === 'add') {
            // 添加子文件夹
            addFolder({
              name: this.folderForm.name,
              parentId: this.folderForm.parentId
            }).then(response => {
              loadingInstance.close();
              if (response.code === 200) {
                this.getFolderTree(); // 刷新树
                this.$modal.msgSuccess("添加成功");
                this.folderDialogVisible = false;
              } else {
                this.$modal.msgError("添加失败: " + (response.msg || '未知错误'));
              }
            }).catch(error => {
              loadingInstance.close();
              console.error("添加文件夹出错:", error);
              this.$modal.msgError("添加失败: " + error.message);
            });
          } else if (this.folderOperationType === 'addSibling' || this.folderOperationType === 'addRoot') {
            // 添加同级或根级文件夹
            addFolder({
              name: this.folderForm.name,
              parentId: this.folderForm.parentId
            }).then(response => {
              loadingInstance.close();
              if (response.code === 200) {
                this.getFolderTree(); // 刷新树
                this.$modal.msgSuccess("添加成功");
                this.folderDialogVisible = false;
              } else {
                this.$modal.msgError("添加失败: " + (response.msg || '未知错误'));
              }
            }).catch(error => {
              loadingInstance.close();
              console.error("添加文件夹出错:", error);
              this.$modal.msgError("添加失败: " + error.message);
            });
          } else if (this.folderOperationType === 'rename') {
            // 重命名文件夹
            updateFolder({
              id: this.folderForm.id,
              name: this.folderForm.name
            }).then(response => {
              loadingInstance.close();
              if (response.code === 200) {
                this.getFolderTree(); // 刷新树
                this.$modal.msgSuccess("重命名成功");
                this.folderDialogVisible = false;
              } else {
                this.$modal.msgError("重命名失败: " + (response.msg || '未知错误'));
              }
            }).catch(error => {
              loadingInstance.close();
              console.error("重命名文件夹出错:", error);
              this.$modal.msgError("重命名失败: " + error.message);
            });
          }
        }
      });
    },
    /** 删除文件夹 */
    handleDeleteFolder(data) {
      this.$modal.confirm('是否确认删除文件夹 "' + data.label + '"? 删除后将无法恢复。').then(() => {
        const loadingInstance = this.$loading({
          lock: true,
          text: '删除中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        deleteFolder(data.id).then(response => {
          loadingInstance.close();
          if (response.code === 200) {
            this.getFolderTree(); // 刷新树
            this.$modal.msgSuccess("删除成功");
            // 如果当前正在查看被删除的文件夹，则清除选择并查询所有
            if (this.queryParams.folderId === data.id) {
              this.queryParams.folderId = undefined;
              this.getList();
            }
          } else {
            this.$modal.msgError("删除失败: " + (response.msg || '未知错误'));
          }
        }).catch(error => {
          loadingInstance.close();
          console.error("删除文件夹出错:", error);
          this.$modal.msgError("删除失败: " + error.message);
        });
      }).catch(() => {});
    },
    // 获取文件夹树
    getFolderTree() {
      getFolderTree().then(response => {
        if (response.code === 200 && response.data) {
          // 转换后端数据格式为前端树组件格式
          this.fileList = this.convertToTreeFormat(response.data);
          // 在nextTick中选中默认节点，确保DOM已更新
          this.$nextTick(() => {
            this.selectDefaultNode();
            // 获取默认文件夹下的列表
            this.getList();
          });
            } else {
          console.error(`获取文件夹树失败: ${response.msg || '未知错误'}`);
          this.$message.error("获取文件夹树失败: " + (response.msg || '未知错误'));
          // 获取全部列表
          this.getList();
        }
      }).catch(error => {
        console.error(`获取文件夹树出错: ${error.message}`);
        this.$message.error("获取文件夹树出错: " + error.message);
        // 获取全部列表
        this.getList();
      });
    },
    
    // 将后端返回的数据转换为前端树组件需要的格式
    convertToTreeFormat(data) {
      if (!data || !Array.isArray(data)) {
        return [];
      }
      
      return data.map(item => {
        const treeNode = {
          id: item.id,
          label: item.name,
          parentId: item.parentId,
          children: item.children && item.children.length > 0 ? this.convertToTreeFormat(item.children) : []
        };
        
        return treeNode;
      });
    },
    // 选择默认节点的方法
    selectDefaultNode() {
      // 如果没有文件夹ID则不选中任何节点
      if (!this.queryParams.folderId) {
        // 获取全部列表
        this.getList();
        return;
      }
      
      if (this.$refs.fileTree) {
        // 设置当前选中节点
        this.$refs.fileTree.setCurrentKey(this.queryParams.folderId);
        
        // 查找节点数据
        const findNode = (nodes, id) => {
          for (let node of nodes) {
            if (node.id === id) {
              return node;
            }
            if (node.children && node.children.length > 0) {
              const found = findNode(node.children, id);
              if (found) return found;
            }
          }
          return null;
        };
        
        const nodeData = findNode(this.fileList, this.queryParams.folderId);
        if (nodeData) {
          this.handleNodeClick(nodeData);
          
          // 滚动节点到视图中
          this.$nextTick(() => {
            // 获取树节点DOM元素
            const nodeEl = this.$refs.fileTree.$el.querySelector(`div[data-key="${this.queryParams.folderId}"]`);
            if (nodeEl) {
              // 添加一个临时的闪烁动画类
              nodeEl.classList.add('folder-highlight-pulse');
              // 滚动到该元素
              nodeEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
              // 3秒后移除动画类
              setTimeout(() => {
                nodeEl.classList.remove('folder-highlight-pulse');
              }, 3000);
            }
          });
        } else {
          console.warn(`未找到ID为${this.queryParams.folderId}的文件夹节点`);
          this.queryParams.folderId = undefined;
          this.getList(); // 获取全部列表
        }
      }
    },
    // 修改submitForm方法
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.folderId) {
            this.$message.warning("请先在左侧选择一个文件夹");
            return;
          }
          
          const loading = this.$loading({
            lock: true,
            text: '保存中...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          // 调用添加接口
          addMindmap(this.form).then(response => {
            loading.close();
            if (response.code === 200) {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              
              // 提取ID，有多种可能的返回格式
              let id;
              if (response.id !== undefined) {
                id = response.id;
              } else if (response.data !== undefined) {
                // 数据可能直接是ID，也可能是对象
                id = typeof response.data === 'object' ? response.data.id : response.data;
              }
              
              if (id) {
                // 延迟一点再跳转，确保其他操作完成
                setTimeout(() => {
                  // 跳转到编辑页面
                  this.$router.push({ 
                    path: `/testcase/editor`, 
                    query: { 
                      id: id,
                      folderId: this.form.folderId 
                    }
                  }).catch(err => {
                    // 如果是重复导航错误，可以忽略
                    if (err.name !== 'NavigationDuplicated') {
                      console.error(`页面跳转失败: ${err.message}`);
                      this.$message.error("页面跳转失败，请手动进入编辑页面");
                    }
                  });
                }, 100);
              } else {
                console.warn("未能从响应中提取ID，刷新列表");
                this.getList(); // 如果没有获取到ID，则刷新列表
              }
            } else {
              this.$modal.msgError(response.msg || "操作失败");
            }
          }).catch(error => {
            loading.close();
            console.error(`保存测试用例出错: ${error.message}`);
            this.$modal.msgError("保存失败: " + error.message);
          });
        }
      });
    },
  }
};
</script>

<style>
/* 全局样式确保优先级 */
.file-nav-content .el-tree-node {
  margin-bottom: 4px !important;
  padding-bottom: 4px !important;
}

.file-nav-content .el-tree-node__content {
  height: 32px !important; /* 保持高度不变 */
  border-radius: 3px !important;
}

/* 直接子节点和间接子节点保持一致的间距 */
.file-nav-content .el-tree-node {
  margin-bottom: 4px !important;
  padding-bottom: 0 !important;
}

/* 统一所有层级的间距 */
.file-nav-content .el-tree-node__children {
  padding-top: 4px !important; /* 顶部添加间距 */
  padding-left: 16px !important; /* 保持缩进 */
}

/* 确保节点内容区域的间距一致 */
.file-nav-content .el-tree-node__content {
  padding: 0 40px 0 6px !important; /* 右侧加宽以容纳操作按钮 */
}

/* 调整自定义树节点的内部间距 */
.file-nav-content .custom-tree-node {
  padding: 4px 0 4px 0 !important; /* 调整内部间距 */
  width: 100% !important;
  position: relative !important;
}

/* 确保文件夹图标与文字之间有适当间距 */
.file-nav-content .folder-icon {
  margin-right: 8px !important;
  font-size: 16px !important;
  color: #67C23A !important; /* 绿色 */
}

/* 打开的文件夹图标使用深一点的颜色 */
.el-icon-folder-opened.folder-icon {
  color: #529B2E !important; /* 深绿色 */
}

/* 增加子节点容器的内边距 */
::v-deep .el-tree-node__children {
  padding-top: 4px !important;
  padding-left: 16px !important;
}

/* 节点图标基础样式 */
.node-icon {
  margin-right: 8px !important;
  font-size: 16px !important;
}

/* 文件夹图标样式 */
.el-icon-folder.node-icon {
  color: #2C7D32 !important; /* 深绿色 */
}

/* 文件图标样式 */
.el-icon-document.node-icon {
  color: #909399 !important; /* 灰色 */
}
</style>

<style scoped>
.app-container-inner {
  display: flex;
  height: calc(100vh - 120px);
}

.file-nav {
  width: 260px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
}

.file-nav-header {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.file-nav-header-inner {
  display: flex;
  align-items: center;
}

.file-nav-header-inner .el-input {
  flex: 1;
}

.add-folder-btn {
  margin-left: 5px;
}

/* 搜索框样式 */
::v-deep .el-input__suffix {
  right: 5px;
  cursor: pointer;
}

.file-nav-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 #f9f9f9;
}

.main-content {
  flex: 1;
  padding: 0 15px;
  overflow-y: auto;
}

/* 自定义树节点样式 */
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-buttons {
  margin-left: auto;
  position: absolute;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

::v-deep .el-tree-node__content:hover .action-buttons {
  opacity: 1;
}

/* 表单自适应样式 */
::v-deep .el-form-item {
  margin-bottom: 22px;
}

::v-deep .el-input {
  width: 100%;
}

::v-deep .el-dialog {
  display: flex;
  flex-direction: column;
  margin: 0 !important;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-height: calc(100% - 30px);
  max-width: 800px;
}

::v-deep .el-dialog .el-dialog__body {
  overflow: auto;
  padding: 20px 25px;
}

/* 响应式调整 */
/* 平板和小屏幕桌面 */
@media screen and (max-width: 1200px) {
  .file-nav {
    width: 220px;
  }
  
  ::v-deep .el-form-item {
    margin-bottom: 18px;
  }
}

/* 手机端 */
@media screen and (max-width: 768px) {
  /* 对话框样式 */
  ::v-deep .el-dialog {
    width: 90% !important;
  }
  
  .app-container-inner {
    flex-direction: column;
    height: auto;
  }
  
  .file-nav {
    width: 100%;
    max-height: 300px;
    border-right: none;
    border-bottom: 1px solid #e6e6e6;
  }
  
  .main-content {
    padding: 15px 0;
  }
  
  .file-nav-content {
    max-height: 250px;
  }
  
  /* 表单项在小屏幕上堆叠排列 */
  ::v-deep .el-form-item {
    display: block;
    margin-right: 0;
  }
  
  /* 调整表格显示 */
  ::v-deep .el-table {
    display: block;
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  /* 确保操作列在移动端可见 */
  ::v-deep .el-table .el-table__fixed-right {
    position: sticky;
    right: 0;
    z-index: 1;
    background-color: white;
    box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  }
  
  /* 调整搜索表单布局 */
  ::v-deep .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
  margin-bottom: 10px;
}

  /* 调整按钮布局 */
  .mb8 .el-col {
    margin-bottom: 8px;
  }
}

/* 调整树本身的内边距 */
::v-deep .el-tree {
  padding: 8px !important;
}

/* 确保每个节点之间有距离 */
::v-deep .el-tree-node {
  margin-bottom: 4px !important;
}

::v-deep .el-tree-node__content {
  padding: 0 40px 0 6px !important; /* 右侧加宽以容纳操作按钮 */
  height: 38px !important;
  border-radius: 4px !important;
}

/* 增加子节点容器的内边距 */
::v-deep .el-tree-node__children {
  padding-top: 4px !important;
  padding-left: 16px !important;
}

/* 节点图标基础样式 */
.node-icon {
  margin-right: 8px !important;
  font-size: 16px !important;
}

/* 文件夹图标样式 */
.el-icon-folder.node-icon {
  color: #2C7D32 !important; /* 深绿色 */
}

/* 文件图标样式 */
.el-icon-document.node-icon {
  color: #909399 !important; /* 灰色 */
}

/* 增加选中节点的样式 */
::v-deep .el-tree-node.is-current > .el-tree-node__content {
  background-color: #e6f7ff !important;
  border-left: 3px solid #409EFF !important;
  font-weight: bold !important;
  color: #409EFF !important;
}

/* 确保选中节点的图标也跟着变色 */
::v-deep .el-tree-node.is-current > .el-tree-node__content .node-icon {
  color: #409EFF !important;
}

/* 修改选中节点动画效果 */
::v-deep .el-tree-node__content {
  transition: all 0.3s ease;
}

/* 添加选中文件夹的闪烁动画 */
@keyframes folderHighlightPulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

.folder-highlight-pulse {
  animation: folderHighlightPulse 1.5s infinite;
  position: relative;
  z-index: 1;
}

/* 给中间内容区域添加滚动条样式 */
.file-nav-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 #f9f9f9;
}

.file-nav-content::-webkit-scrollbar {
  width: 6px;
}

.file-nav-content::-webkit-scrollbar-track {
  background: #f9f9f9;
}

.file-nav-content::-webkit-scrollbar-thumb {
  background-color: #d9d9d9;
  border-radius: 3px;
}
</style> 