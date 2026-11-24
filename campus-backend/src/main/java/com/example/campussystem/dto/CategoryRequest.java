package com.example.campussystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类请求DTO
 */
@Data
public class CategoryRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 分类图标
     */
    @Size(max = 100, message = "图标路径长度不能超过100个字符")
    private String icon;

    /**
     * 分类描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}
