package com.example.campussystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 父分类ID，为null表示顶级分类
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序值，值越小越靠前
     */
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    /**
     * 状态：1-启用，0-禁用
     */
    @Column(nullable = false, columnDefinition = "int default 1")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 分类图标
     */
    @Column(length = 100)
    private String icon;

    /**
     * 分类描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 子分类（仅用于树形结构，不映射到数据库）
     */
    @Transient
    private List<Category> children;
}
