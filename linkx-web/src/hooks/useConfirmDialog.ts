/**
 * 统一封装确认弹窗的打开、取消和确认逻辑。
 */
import { reactive } from 'vue'  // 行注：引入 reactive 能力

export type ConfirmType = 'primary' | 'danger'  // 行注：导出当前能力

export interface ConfirmDialogOptions {  // 行注：导出当前能力
  title: string  // 行注：设置 title 配置项
  subtitle?: string  // 行注：补充当前表达式
  description: string  // 行注：设置 description 配置项
  cancelText?: string  // 行注：补充当前表达式
  confirmText?: string  // 行注：补充当前表达式
  confirmType?: ConfirmType  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export function useConfirmDialog() {  // 行注：导出当前能力
  const confirmDialog = reactive({  // 行注：开始解构当前返回值
    visible: false,  // 行注：设置 visible 配置项
    title: '',  // 行注：设置 title 配置项
    subtitle: '',  // 行注：设置 subtitle 配置项
    description: '',  // 行注：设置 description 配置项
    cancelText: '取消',  // 行注：设置 cancelText 配置项
    confirmText: '确认',  // 行注：设置 confirmText 配置项
    confirmType: 'primary' as ConfirmType  // 行注：设置 confirmType 配置项
  })  // 行注：结束当前调用配置

  let confirmDialogResolver: ((value: boolean) => void) | null = null  // 行注：初始化 confirmDialogResolver 变量

  function resolveConfirmDialog(confirmed: boolean) {  // 行注：定义 resolveConfirmDialog 方法
    const resolver = confirmDialogResolver  // 行注：初始化 resolver 方法
    confirmDialogResolver = null  // 行注：更新 confirmDialogResolver 值
    confirmDialog.visible = false  // 行注：更新 confirmDialog.visible 值
    resolver?.(confirmed)  // 行注：执行当前调用逻辑
  }  // 行注：结束当前代码块

  function cancelConfirmDialog() {  // 行注：定义 cancelConfirmDialog 方法
    resolveConfirmDialog(false)  // 行注：调用 resolveConfirmDialog 方法
  }  // 行注：结束当前代码块

  function confirmConfirmDialog() {  // 行注：定义 confirmConfirmDialog 方法
    resolveConfirmDialog(true)  // 行注：调用 resolveConfirmDialog 方法
  }  // 行注：结束当前代码块

  function openConfirmDialog(options: ConfirmDialogOptions) {  // 行注：定义 openConfirmDialog 方法
    confirmDialog.title = options.title  // 行注：更新 confirmDialog.title 值
    confirmDialog.subtitle = options.subtitle || ''  // 行注：更新 confirmDialog.subtitle 值
    confirmDialog.description = options.description  // 行注：更新 confirmDialog.description 值
    confirmDialog.cancelText = options.cancelText || '取消'  // 行注：更新 confirmDialog.cancelText 值
    confirmDialog.confirmText = options.confirmText || '确认'  // 行注：更新 confirmDialog.confirmText 值
    confirmDialog.confirmType = options.confirmType || 'primary'  // 行注：更新 confirmDialog.confirmType 值
    confirmDialog.visible = true  // 行注：更新 confirmDialog.visible 值
    return new Promise<boolean>((resolve) => {  // 行注：返回当前结果
      confirmDialogResolver = resolve  // 行注：更新 confirmDialogResolver 值
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    confirmDialog,  // 行注：补充 confirmDialog 配置项
    openConfirmDialog,  // 行注：补充当前配置项
    cancelConfirmDialog,  // 行注：补充当前配置项
    confirmConfirmDialog  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
