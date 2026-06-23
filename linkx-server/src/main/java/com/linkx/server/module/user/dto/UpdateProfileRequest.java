package com.linkx.server.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 更新本人昵称、头像、性别等（字段可选）。 */
@Data
public class UpdateProfileRequest {
    @Size(min = 1, max = 50, message = "昵称长度1-50位")
    private String nickname;

    private String avatar;

    private Integer gender;
}
