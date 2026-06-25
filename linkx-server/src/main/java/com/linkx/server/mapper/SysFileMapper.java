package com.linkx.server.mapper;  // 行注：声明当前文件所在包 com.linkx.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // 行注：引入 BaseMapper 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import org.apache.ibatis.annotations.Mapper;  // 行注：引入 Mapper 类型

/** {@link com.linkx.server.entity.SysFile} Mapper。 */
@Mapper  // 行注：应用 @Mapper 注解
// 行注：定义 SysFileMapper 接口
public interface SysFileMapper extends BaseMapper<SysFile> {
}  // 行注：结束当前代码块
