package com.example.campussystem.repository;

import com.example.campussystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 商品分类仓库接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据状态查找分类
     */
    List<Category> findByStatus(Integer status);

    /**
     * 根据父ID查找子分类
     */
    List<Category> findByParentId(Long parentId);

    /**
     * 根据父ID和状态查找子分类
     */
    List<Category> findByParentIdAndStatus(Long parentId, Integer status);

    /**
     * 查找所有顶级分类（parentId为null）
     */
    List<Category> findByParentIdIsNull();

    /**
     * 根据状态查找顶级分类
     */
    List<Category> findByParentIdIsNullAndStatus(Integer status);

    /**
     * 查找所有启用状态的分类
     */
    List<Category> findByStatusOrderBySortOrderAsc(Integer status);

    /**
     * 获取分类下的商品数量统计
     */
    @Query("SELECT new map(c.id as categoryId, c.name as categoryName, COUNT(p.id) as productCount) " +
           "FROM Category c LEFT JOIN Product p ON c.id = p.categoryId AND p.status = 1 " +
           "WHERE c.status = 1 " +
           "GROUP BY c.id, c.name " +
           "ORDER BY c.sortOrder ASC")
    List<Map<String, Object>> getCategoryProductCounts();

    /**
     * 根据名称模糊查询
     */
    List<Category> findByNameContainingAndStatus(String name, Integer status);

    /**
     * 获取树形结构的分类数据
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1 ORDER BY c.sortOrder ASC, c.createTime ASC")
    List<Category> findAllActiveOrdered();
}
