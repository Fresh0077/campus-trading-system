package com.example.campussystem.repository;

import com.example.campussystem.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 管理员数据访问层
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * 根据用户名查找管理员
     */
    Optional<Admin> findByUsername(String username);

    /**
     * 根据用户名和状态查找管理员
     */
    Optional<Admin> findByUsernameAndStatus(String username, Integer status);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找管理员列表
     */
    Page<Admin> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据角色查找管理员列表
     */
    Page<Admin> findByRole(String role, Pageable pageable);

    /**
     * 根据用户名或真实姓名模糊查询
     */
    @Query("SELECT a FROM Admin a WHERE " +
           "(a.username LIKE %:keyword% OR a.realName LIKE %:keyword%) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Admin> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                      @Param("status") Integer status, 
                                      Pageable pageable);

    /**
     * 统计启用的管理员数量
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status = 1")
    long countActiveAdmins();

    /**
     * 根据用户名模糊查询和状态查找管理员列表（按创建时间倒序）
     */
    List<Admin> findByUsernameContainingIgnoreCaseAndStatusOrderByCreateTimeDesc(String username, Integer status);

    /**
     * 根据用户名模糊查询管理员列表（按创建时间倒序）
     */
    List<Admin> findByUsernameContainingIgnoreCaseOrderByCreateTimeDesc(String username);

    /**
     * 根据状态查找管理员列表（按创建时间倒序）
     */
    List<Admin> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * 查找所有管理员列表（按创建时间倒序）
     */
    List<Admin> findAllByOrderByCreateTimeDesc();

}
