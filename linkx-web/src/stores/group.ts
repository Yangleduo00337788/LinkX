/**
 * 维护群组相关的共享状态、缓存和界面辅助数据。
 */
import { defineStore } from 'pinia'  // 行注：引入 defineStore 能力
import { reactive, ref } from 'vue'  // 行注：引入 reactive, ref 能力
import type { GroupDetail } from '../types/chat'  // 行注：引入 type { GroupDetail } 模块
import { resetGroupProfileDraftState } from '../utils/group-draft'  // 行注：引入 resetGroupProfileDraftState 能力

export const useGroupStore = defineStore('group', () => {  // 行注：导出当前能力
  const groupDetail = ref<GroupDetail | null>(null)  // 行注：初始化 groupDetail 变量
  const showGroupDrawer = ref(false)  // 行注：初始化 showGroupDrawer 响应式状态
  const noticeDraft = ref('')  // 行注：初始化 noticeDraft 响应式状态
  const groupProfileDraft = reactive({  // 行注：开始解构当前返回值
    groupName: '',  // 行注：设置 groupName 配置项
    avatarPreview: '',  // 行注：设置 avatarPreview 配置项
    avatarFile: null as File | null  // 行注：设置 avatarFile 配置项
  })  // 行注：结束当前调用配置

  function resetGroupDetailState() {  // 行注：定义 resetGroupDetailState 方法
    groupDetail.value = null  // 行注：更新 groupDetail 状态
    showGroupDrawer.value = false  // 行注：更新 showGroupDrawer 状态
    noticeDraft.value = ''  // 行注：更新 noticeDraft 状态
    resetGroupProfileDraftState(groupProfileDraft)  // 行注：调用 resetGroupProfileDraftState 方法
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    groupDetail,  // 行注：补充 groupDetail 配置项
    showGroupDrawer,  // 行注：补充当前配置项
    noticeDraft,  // 行注：补充当前配置项
    groupProfileDraft,  // 行注：补充当前配置项
    resetGroupDetailState  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置
