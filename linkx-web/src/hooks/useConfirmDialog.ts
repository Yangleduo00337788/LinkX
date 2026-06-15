import { reactive } from 'vue'

export type ConfirmType = 'primary' | 'danger'

export interface ConfirmDialogOptions {
  title: string
  subtitle?: string
  description: string
  cancelText?: string
  confirmText?: string
  confirmType?: ConfirmType
}

export function useConfirmDialog() {
  const confirmDialog = reactive({
    visible: false,
    title: '',
    subtitle: '',
    description: '',
    cancelText: '取消',
    confirmText: '确认',
    confirmType: 'primary' as ConfirmType
  })

  let confirmDialogResolver: ((value: boolean) => void) | null = null

  function resolveConfirmDialog(confirmed: boolean) {
    const resolver = confirmDialogResolver
    confirmDialogResolver = null
    confirmDialog.visible = false
    resolver?.(confirmed)
  }

  function cancelConfirmDialog() {
    resolveConfirmDialog(false)
  }

  function confirmConfirmDialog() {
    resolveConfirmDialog(true)
  }

  function openConfirmDialog(options: ConfirmDialogOptions) {
    confirmDialog.title = options.title
    confirmDialog.subtitle = options.subtitle || ''
    confirmDialog.description = options.description
    confirmDialog.cancelText = options.cancelText || '取消'
    confirmDialog.confirmText = options.confirmText || '确认'
    confirmDialog.confirmType = options.confirmType || 'primary'
    confirmDialog.visible = true
    return new Promise<boolean>((resolve) => {
      confirmDialogResolver = resolve
    })
  }

  return {
    confirmDialog,
    openConfirmDialog,
    cancelConfirmDialog,
    confirmConfirmDialog
  }
}
