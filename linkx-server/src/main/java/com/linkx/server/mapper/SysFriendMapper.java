package com.linkx.server.mapper;  // 行注：声明当前文件所在包 com.linkx.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 行注：引入 BaseMapper 类型
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import org.apache.ibatis.annotations.Mapper;  // 行注：引入 Mapper 类型

/** {@link com.linkx.server.entity.SysFriend} Mapper。 */
@Mapper  // 行注：应用 @Mapper 注解
// 行注：定义 SysFriendMapper 接口
public interface SysFriendMapper extends BaseMapper<SysFriend> {
}  // 行注：结束当前代码块
