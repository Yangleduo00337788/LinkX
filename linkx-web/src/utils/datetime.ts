/**
 * datetime 工具模块，封装当前前端场景下的通用方法。
 */
const BACKEND_LOCAL_DATE_TIME_PATTERN = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/  // 行注：初始化 BACKEND_LOCAL_DATE_TIME_PATTERN 变量

function normalizeDateTimeInput(value: string) {  // 行注：定义 normalizeDateTimeInput 方法
  const trimmed = value.trim()  // 行注：初始化 trimmed 变量
  if (BACKEND_LOCAL_DATE_TIME_PATTERN.test(trimmed) && !trimmed.includes('T')) {  // 行注：判断当前条件是否成立
    return trimmed.replace(' ', 'T')  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return trimmed  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function parseDateTime(value?: string | null) {  // 行注：导出当前能力
  if (!value) {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const normalized = normalizeDateTimeInput(value)  // 行注：初始化 normalized 变量
  const date = new Date(normalized)  // 行注：初始化 date 变量
  if (Number.isNaN(date.getTime())) {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return date  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getDateTimeTimestamp(value?: string | null) {  // 行注：导出当前能力
  return parseDateTime(value)?.getTime() ?? 0  // 行注：返回当前结果
}  // 行注：结束当前代码块
