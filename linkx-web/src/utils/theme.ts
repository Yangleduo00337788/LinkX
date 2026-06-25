/**
 * theme 工具模块，封装当前前端场景下的通用方法。
 */
import { ref, watch } from 'vue'  // 行注：引入 ref, watch 能力

export type ThemeMode = 'light' | 'dark' | 'system'  // 行注：导出当前能力

const THEME_KEY = 'linkx-theme'  // 行注：初始化 THEME_KEY 变量
const themeMode = ref<ThemeMode>((localStorage.getItem(THEME_KEY) as ThemeMode) || 'light')  // 行注：初始化 themeMode 变量
const mediaQuery = typeof window !== 'undefined' && window.matchMedia  // 行注：初始化 mediaQuery 变量
  ? window.matchMedia('(prefers-color-scheme: dark)')  // 行注：调用 matchMedia 方法
  : null  // 行注：补充当前表达式
let initialized = false  // 行注：初始化 initialized 方法

function getSystemTheme(): 'light' | 'dark' {  // 行注：定义 getSystemTheme 方法
  if (mediaQuery) {  // 行注：判断当前条件是否成立
    return mediaQuery.matches ? 'dark' : 'light'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return 'light'  // 行注：返回当前结果
}  // 行注：结束当前代码块

function applyTheme(mode: ThemeMode) {  // 行注：定义 applyTheme 方法
  const actual = mode === 'system' ? getSystemTheme() : mode  // 行注：初始化 actual 变量
  document.documentElement.setAttribute('data-theme', actual)  // 行注：调用 setAttribute 方法
}  // 行注：结束当前代码块

export function useTheme() {  // 行注：导出当前能力
  if (!initialized) {  // 行注：判断当前条件是否成立
    initialized = true  // 行注：更新 initialized 值
    applyTheme(themeMode.value)  // 行注：调用 applyTheme 方法

    watch(themeMode, (val) => {  // 行注：监听状态变化
      localStorage.setItem(THEME_KEY, val)  // 行注：写入本地缓存
      applyTheme(val)  // 行注：调用 applyTheme 方法
    })  // 行注：结束当前调用配置

    mediaQuery?.addEventListener('change', () => {  // 行注：注册变更事件监听
      if (themeMode.value === 'system') {  // 行注：判断当前条件是否成立
        applyTheme(themeMode.value)  // 行注：调用 applyTheme 方法
      }  // 行注：结束当前代码块
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function setMode(val: ThemeMode) {  // 行注：定义 setMode 方法
    themeMode.value = val  // 行注：更新 themeMode 状态
  }  // 行注：结束当前代码块

  return { mode: themeMode, setMode }  // 行注：返回当前结果
}  // 行注：结束当前代码块
