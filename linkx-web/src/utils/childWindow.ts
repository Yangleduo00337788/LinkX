/** Electron 子窗口；浏览器内用固定尺寸弹层降级。 */

export type ChildWindowType = 'create-group' | 'add-friend' | 'add-group-members'

export interface OpenChildWindowPayload {
  type: ChildWindowType
  groupId?: string | number
}

export function isElectronChildWindowApi(): boolean {
  return typeof window !== 'undefined' && !!(window as Window & { electronAPI?: { openChildWindow?: unknown } }).electronAPI?.openChildWindow
}

export async function openLinkxChildWindow(payload: OpenChildWindowPayload): Promise<boolean> {
  const api = (window as Window & { electronAPI?: { openChildWindow?: (p: OpenChildWindowPayload) => Promise<boolean> } }).electronAPI
  if (api?.openChildWindow) {
    return Boolean(await api.openChildWindow(payload))
  }
  return false
}

export async function closeLinkxChildWindow(): Promise<void> {
  const api = (window as Window & { electronAPI?: { closeChildWindow?: () => Promise<boolean> } }).electronAPI
  if (api?.closeChildWindow) {
    await api.closeChildWindow()
  }
}