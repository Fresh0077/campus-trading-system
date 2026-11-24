package com.example.campussystem.dto;

import com.example.campussystem.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类响应DTO
 */
@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String icon;
    private String description;
    private List<CategoryResponse> children;

    /**
     * 从Category实体转换为CategoryResponse（包含子分类）
     */
    public static CategoryResponse from(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setParentId(category.getParentId());
        response.setSortOrder(category.getSortOrder());
        response.setStatus(category.getStatus());
        response.setCreateTime(category.getCreateTime());
        response.setUpdateTime(category.getUpdateTime());
        response.setIcon(category.getIcon());
        response.setDescription(category.getDescription());

        // 如果有子分类，递归设置
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> children = new ArrayList<>();
            for (Category child : category.getChildren()) {
                children.add(from(child));
            }
            response.setChildren(children);
        }

        return response;
    }

    /**
     * 从Category列表转换为CategoryResponse列表（不包含子分类）
     */
    public static List<CategoryResponse> fromListWithoutChildren(List<Category> categories) {
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            CategoryResponse response = new CategoryResponse();
            response.setId(category.getId());
            response.setName(category.getName());
            response.setParentId(category.getParentId());
            response.setSortOrder(category.getSortOrder());
            response.setStatus(category.getStatus());
            response.setCreateTime(category.getCreateTime());
            response.setUpdateTime(category.getUpdateTime());
            response.setIcon(category.getIcon());
            response.setDescription(category.getDescription());
            responses.add(response);
        }
        return responses;
    }

    /**
     * 从Category列表转换为CategoryResponse列表（包含子分类）
     */
    public static List<CategoryResponse> fromList(List<Category> categories) {
        List<CategoryResponse> responses = new ArrayList<>();
        for (Category category : categories) {
            responses.add(from(category));
        }
        return responses;
    }
}
