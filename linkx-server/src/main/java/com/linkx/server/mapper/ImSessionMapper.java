package com.linkx.server.mapper;  // 行注：声明当前文件所在包 com.linkx.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 行注：引入 BaseMapper 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import org.apache.ibatis.annotations.Mapper;  // 行注：引入 Mapper 类型

/** {@link com.linkx.server.entity.ImSession} Mapper。 */
@Mapper  // 行注：应用 @Mapper 注解
// 行注：定义 ImSessionMapper 接口
public interface ImSessionMapper extends BaseMapper<ImSession> {
}  // 行注：结束当前代码块
