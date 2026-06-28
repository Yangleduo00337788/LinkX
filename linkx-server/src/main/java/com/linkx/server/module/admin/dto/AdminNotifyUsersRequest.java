package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminNotifyUsersRequest {

    @NotEmpty
    @Size(max = 500)
    private List<Long> userIds;

    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    @Size(max = 4000)
    private String content;
}