package com.example.campussystem.service;

import com.example.campussystem.entity.Category;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类服务
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取所有激活的分类
     */
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByStatus(1);
    }

    /**
     * 获取分类树结构
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAllActiveOrdered();
        return buildCategoryTree(allCategories);
    }

    /**
     * 获取顶级分类
     */
    @Transactional(readOnly = true)
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIdIsNullAndStatus(1);
    }

    /**
     * 根据父ID获取子分类
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryRepository.findByParentIdAndStatus(parentId, 1);
    }

    /**
     * 根据ID获取分类
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
    }

    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(String name, Long parentId, Integer sortOrder) {
        // 检查分类名称是否已存在
        List<Category> existingCategories = categoryRepository.findByNameContainingAndStatus(name, 1);
        if (!existingCategories.isEmpty()) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        category.setStatus(1);

        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long id, String name, Long parentId, Integer sortOrder, Integer status) {
        Category category = getCategoryById(id);

        // 检查分类名称是否与其他分类重复
        List<Category> existingCategories = categoryRepository.findByNameContainingAndStatus(name, 1);
        if (!existingCategories.isEmpty() && !existingCategories.get(0).getId().equals(id)) {
            throw new BusinessException("分类名称已存在");
        }

        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        if (status != null) {
            category.setStatus(status);
        }

        return categoryRepository.save(category);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // 检查是否有子分类
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("请先删除子分类");
        }

        // 检查分类下是否有商品（这里简化处理，实际应该检查商品表）
        // TODO: 检查商品表中是否有使用此分类的商品

        categoryRepository.delete(category);
    }

    /**
     * 切换分类状态
     */
    @Transactional
    public Category toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        return categoryRepository.save(category);
    }

    /**
     * 批量删除分类
     */
    @Transactional
    public void deleteCategoriesBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteCategory(id);
        }
    }

    /**
     * 初始化基础分类数据
     */
    @Transactional
    public void initializeBasicCategories() {
        if (categoryRepository.count() == 0) {
            // 创建基础分类
            createCategory("电子产品", null, 1);
            createCategory("书籍教材", null, 2);
            createCategory("生活用品", null, 3);
            createCategory("服饰鞋包", null, 4);
            createCategory("运动健身", null, 5);
            createCategory("其他", null, 6);
        }
    }

    /**
     * 构建分类树结构
     */
    private List<Category> buildCategoryTree(List<Category> allCategories) {
        List<Category> rootCategories = new ArrayList<>();

        for (Category category : allCategories) {
            if (category.getParentId() == null) {
                rootCategories.add(category);
                category.setChildren(getChildCategories(category.getId(), allCategories));
            }
        }

        return rootCategories;
    }

    /**
     * 获取子分类
     */
    private List<Category> getChildCategories(Long parentId, List<Category> allCategories) {
        List<Category> children = new ArrayList<>();

        for (Category category : allCategories) {
            if (parentId.equals(category.getParentId())) {
                children.add(category);
                category.setChildren(getChildCategories(category.getId(), allCategories));
            }
        }

        return children;
    }
}
