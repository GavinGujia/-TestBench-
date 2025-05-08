<template lang="">
<div class="font-group">
    <el-select v-model="fontFamilyDefaultValue" placeholder="字体" class="font-family-select" :disabled="disabledFont" @change="execCommandFontFamily">
        <el-option v-for="item in fontFamilys" :label="item.name" :value="item.value" :key="item.id" :style="{'font-family':item.value}">
        </el-option>
    </el-select>
    <el-select v-model="fontSizeDefaultValue" placeholder="字号" class="font-size-select" :disabled="disabledFontSize" @change="execCommandFontSize">
        <el-option v-for="item in fontSizes" :label="item.label" :value="item.value" :key="item.id" :style="{'font-size':item.value+'px','height':2*item.value+'px', 'line-height':2*item.value+'px','padding':0}">
        </el-option>
    </el-select>
    <span class="font-bold menu-btn tab-icons" @click="execCommandFontStyle('bold')" :class="{'selected':boldSelected}" :disabled="disabledBold"></span>
    <span class="font-italic menu-btn tab-icons" @click="execCommandFontStyle('italic')" :class="{'selected':italicSelected}" :disabled="disabledItalic"></span>
</div>
</template>

<script>
import {
    mapGetters
} from "vuex";
export default {
    data() {
        return {
            fontFamilys: [
                {
                    id: 1,
                    value: "宋体,SimSun",
                    name: "宋体",
                },
                {
                    id: 2,
                    value: "微软雅黑,Microsoft YaHei",
                    name: "微软雅黑",
                },
                {
                    id: 3,
                    value: "楷体,楷体_GB2312,SimKai",
                    name: "楷体",
                },
                {
                    id: 4,
                    value: "黑体, SimHei",
                    name: "黑体",
                },
                {
                    id: 5,
                    value: "隶书, SimLi",
                    name: "隶书",
                },
                {
                    id: 6,
                    value: "andale mono",
                    name: "Andale Mono",
                },
                {
                    id: 7,
                    value: "arial,helvetica,sans-serif",
                    name: "Arial",
                },
                {
                    id: 8,
                    value: "arial black,avant garde",
                    name: "arialBlack",
                },
                {
                    id: 9,
                    value: "comic sans ms",
                    name: "comic Sans Ms",
                },
                {
                    id: 10,
                    value: "impact,chicago",
                    name: "Impact",
                },
                {
                    id: 11,
                    value: "times new roman",
                    name: "times New Roman",
                },
                {
                    id: 12,
                    value: "sans-serif",
                    name: "Sans-Serif",
                },
            ],

            fontSizes: [
                {
                    id: 1,
                    value: 10,
                    label: 10,
                },
                {
                    id: 2,
                    value: 12,
                    label: 12,
                },
                {
                    id: 3,
                    value: 16,
                    label: 16,
                },
                {
                    id: 4,
                    value: 18,
                    label: 18,
                },
                {
                    id: 5,
                    value: 24,
                    label: 24,
                },
                {
                    id: 6,
                    value: 32,
                    label: 32,
                },
                {
                    id: 7,
                    value: 48,
                    label: 48,
                },
            ],
            fontFamilyDefaultValue: "字体",
            fontSizeDefaultValue: "字号",
            // 命令状态缓存
            commandStates: {
                fontfamily: -1,
                fontsize: -1,
                bold: -1,
                italic: -1
            },
            // 命令值缓存
            commandValues: {
                fontfamily: null,
                fontsize: null
            }
        };
    },

    computed: {
        ...mapGetters({
            minder: "getMinder",
            viewMode: "getViewMode",
            nodeEditMode: "getNodeEditMode"
        }),
        
        // 计算属性使用缓存值，避免递归调用
        disabledFont() {
            // 如果不是编辑模式，直接禁用
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return true;
            }
            
            // 更新字体系列值
            this.fontFamilyDefaultValue = this.commandValues.fontfamily || "字体";
            
            // 使用缓存的状态值
            return this.commandStates.fontfamily === -1;
        },
        disabledFontSize() {
            // 如果不是编辑模式，直接禁用
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return true;
            }
            
            // 更新字体大小值
            this.fontSizeDefaultValue = this.commandValues.fontsize || "字号";
            
            // 使用缓存的状态值
            return this.commandStates.fontsize === -1;
        },
        disabledBold() {
            // 如果不是编辑模式，直接禁用
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return true;
            }
            
            // 使用缓存的状态值
            return this.commandStates.bold === -1;
        },
        disabledItalic() {
            // 如果不是编辑模式，直接禁用
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return true;
            }
            
            // 使用缓存的状态值
            return this.commandStates.italic === -1;
        },
        boldSelected() {
            // 如果不是编辑模式，直接返回false
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return false;
            }
            
            // 使用缓存的状态值
            return this.commandStates.bold === 1;
        },
        italicSelected() {
            // 如果不是编辑模式，直接返回false
            if (this.viewMode !== 'edit' || !this.nodeEditMode) {
                return false;
            }
            
            // 使用缓存的状态值
            return this.commandStates.italic === 1;
        }
    },
    watch: {
        // 监听视图模式变化
        viewMode: {
            immediate: true,
            handler(newVal) {
                this.updateCommandStates();
            }
        },
        // 监听minder对象变化
        minder: {
            immediate: true,
            handler(newVal, oldVal) {
                if (newVal !== oldVal) {
                    this.updateCommandStates();
                    this.setupMinderEvents();
                }
            }
        },
        // 监听节点编辑模式变化
        nodeEditMode(newVal, oldVal) {
            if (newVal !== oldVal) {
                this.updateCommandStates();
            }
        }
    },
    mounted() {
        this.setupMinderEvents();
        this.updateCommandStates();
    },
    beforeDestroy() {
        this.removeMinderEvents();
    },
    methods: {
        // 设置minder事件监听
        setupMinderEvents() {
            if (!this.minder || typeof this.minder.on !== 'function') {
                return;
            }
            
            // 选择节点变化时更新命令状态
            this.minder.on('selectionchange', this.updateCommandStates);
            this.minder.on('interactchange', this.updateCommandStates);
        },
        
        // 清理minder事件监听
        removeMinderEvents() {
            if (!this.minder || typeof this.minder.off !== 'function') {
                return;
            }
            
            this.minder.off('selectionchange', this.updateCommandStates);
            this.minder.off('interactchange', this.updateCommandStates);
        },
        
        // 更新命令状态和值缓存
        updateCommandStates() {
            if (!this.minder || !this.minder.queryCommandState || !this.minder.queryCommandValue) {
                // minder不可用时，所有命令都禁用
                this.commandStates = {
                    fontfamily: -1,
                    fontsize: -1,
                    bold: -1,
                    italic: -1
                };
                this.commandValues = {
                    fontfamily: null,
                    fontsize: null
                };
                return;
            }
            
            try {
                // 安全地查询命令状态
                const queryState = (cmd) => {
                    try {
                        return this.minder.queryCommandState(cmd);
                    } catch (e) {
                        console.error(`查询${cmd}命令状态出错:`, e);
                        return -1;
                    }
                };
                
                // 安全地查询命令值
                const queryValue = (cmd) => {
                    try {
                        return this.minder.queryCommandValue(cmd);
                    } catch (e) {
                        console.error(`查询${cmd}命令值出错:`, e);
                        return null;
                    }
                };
                
                // 更新状态缓存
                this.commandStates = {
                    fontfamily: queryState('fontfamily'),
                    fontsize: queryState('fontsize'),
                    bold: queryState('bold'),
                    italic: queryState('italic')
                };
                
                // 更新值缓存
                this.commandValues = {
                    fontfamily: queryValue('fontfamily'),
                    fontsize: queryValue('fontsize')
                };
                
                // 更新默认值
                this.fontFamilyDefaultValue = this.commandValues.fontfamily || "字体";
                this.fontSizeDefaultValue = this.commandValues.fontsize || "字号";
            } catch (e) {
                console.error('更新命令状态缓存出错:', e);
                // 出错时默认禁用所有命令
                this.commandStates = {
                    fontfamily: -1,
                    fontsize: -1,
                    bold: -1,
                    italic: -1
                };
            }
        },
        
        execCommandFontFamily(value) {
            if (value == "字体" || this.disabledFont) {
                return;
            }
            
            if (!this.minder || typeof this.minder.execCommand !== 'function') {
                return;
            }
            
            try {
                this.minder.execCommand("fontfamily", value);
                // 命令执行后更新状态
                this.$nextTick(() => this.updateCommandStates());
            } catch (e) {
                console.error("执行fontfamily命令出错:", e);
            }
        },
        
        execCommandFontSize(value) {
            if (typeof value !== "number" || this.disabledFontSize) {
                return;
            }
            
            if (!this.minder || typeof this.minder.execCommand !== 'function') {
                return;
            }
            
            try {
                this.minder.execCommand("fontsize", value);
                // 命令执行后更新状态
                this.$nextTick(() => this.updateCommandStates());
            } catch (e) {
                console.error("执行fontsize命令出错:", e);
            }
        },
        
        execCommandFontStyle(style) {
            if (!this.minder || typeof this.minder.execCommand !== 'function') {
                return;
            }
            
            try {
                switch (style) {
                    case "bold":
                        if (this.disabledBold) return;
                        this.minder.execCommand("bold");
                        break;
                    case "italic":
                        if (this.disabledItalic) return;
                        this.minder.execCommand("italic");
                        break;
                }
                // 命令执行后更新状态
                this.$nextTick(() => this.updateCommandStates());
            } catch (e) {
                console.error(`执行${style}命令出错:`, e);
            }
        },
    },
};
</script>
