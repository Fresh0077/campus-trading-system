package com.example.campussystem.service;

import com.example.campussystem.dto.AdminLoginRequest;
import com.example.campussystem.dto.AdminResponse;
import com.example.campussystem.dto.DashboardStatsResponse;
import com.example.campussystem.entity.Admin;
import com.example.campussystem.entity.User;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.Order;
import com.example.campussystem.entity.Category;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.AdminRepository;
import com.example.campussystem.repository.UserRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.OrderRepository;
import com.example.campussystem.repository.CategoryRepository;
import com.example.campussystem.util.JwtUtil;
import com.example.campussystem.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员认证和管理服务
 */
@Service
@Transactional
public class AdminAuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    public Map<String, Object> login(AdminLoginRequest request, HttpServletRequest httpRequest) {
        // 查找管理员
        Admin admin = adminRepository.findByUsernameAndStatus(request.getUsername(), 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(getClientIpAddress(httpRequest));
        admin.setLoginCount(admin.getLoginCount() + 1);
        adminRepository.save(admin);

        // 生成JWT令牌
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", new AdminResponse(admin));
        result.put("permissions", getAllPermissions()); // 管理员拥有所有权限

        return result;
    }

    /**
     * 获取管理员信息
     */
    @Transactional(readOnly = true)
    public AdminResponse getAdminInfo(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("管理员不存在"));
        return new AdminResponse(admin);
    }

    /**
     * 获取仪表盘统计数据
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // 基础统计
        stats.setTotalUsers(userRepository.count());
        stats.setTotalProducts(productRepository.count());
        stats.setTotalOrders(orderRepository.count());

        // 今日统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.setTodayNewUsers(userRepository.countByCreateTimeAfter(todayStart));
        stats.setTodayNewProducts(productRepository.countByCreateTimeAfter(todayStart));
        stats.setTodayNewOrders(orderRepository.countByCreateTimeAfter(todayStart));
        
        // 今日交易金额
        BigDecimal todayAmount = orderRepository.sumAmountByCreateTimeAfterAndStatus(todayStart, 3);
        stats.setTodayTransactionAmount(todayAmount != null ? todayAmount : BigDecimal.ZERO);

        // 状态统计
        stats.setActiveUsers(userRepository.countByStatus(1));
        stats.setOnlineUsers(userRepository.countByOnline(true));
        stats.setAvailableProducts(productRepository.countByStatus(1));
        stats.setSoldProducts(productRepository.countByStatus(3));
        stats.setPendingOrders(orderRepository.countByStatus(1));
        stats.setCompletedOrders(orderRepository.countByStatus(3));

        // 分类统计
        stats.setCategoryStats(getCategoryStatsList());


        // 用户活跃度统计（最近7天）
        stats.setUserActivityStats(getUserActivityStatsList());

        // 交易统计（最近7天）
        stats.setTransactionStats(getTransactionStatsList());

        // 热门商品
        stats.setPopularProducts(getPopularProductsList());

        // 最新用户
        stats.setRecentUsers(getRecentUsersList());

        return stats;
    }

    /**
     * 创建管理员账户
     */
    public AdminResponse createAdmin(Admin admin) {
        // 检查用户名是否已存在
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new BusinessException("管理员用户名已存在");
        }

        // 检查邮箱是否已存在
        if (admin.getEmail() != null && adminRepository.existsByEmail(admin.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 加密密码
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        // 设置默认值
        if (admin.getRole() == null) {
            admin.setRole("ADMIN");
        }
        if (admin.getStatus() == null) {
            admin.setStatus(1);
        }
        if (admin.getLoginCount() == null) {
            admin.setLoginCount(0);
        }

        Admin savedAdmin = adminRepository.save(admin);
        return new AdminResponse(savedAdmin);
    }

    /**
     * 获取管理员列表
     */
    @Transactional(readOnly = true)
    public Page<AdminResponse> getAdminList(int page, int size, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<Admin> admins;
        if (keyword != null && !keyword.trim().isEmpty()) {
            admins = adminRepository.findByKeywordAndStatus(keyword, status, pageable);
        } else if (status != null) {
            admins = adminRepository.findByStatus(status, pageable);
        } else {
            admins = adminRepository.findAll(pageable);
        }

        return admins.map(AdminResponse::new);
    }

    /**
     * 更新管理员状态
     */
    public void updateAdminStatus(Long adminId, Integer status) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        admin.setStatus(status);
        adminRepository.save(admin);
    }

    /**
     * 删除管理员
     */
    public void deleteAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        // 检查是否是最后一个管理员
        long adminCount = adminRepository.countActiveAdmins();
        if (adminCount <= 1) {
            throw new BusinessException("不能删除最后一个管理员");
        }

        adminRepository.delete(admin);
    }

    // 私有辅助方法

    private String getClientIpAddress(HttpServletRequest request) {
        try {
            return IpUtils.getClientIpAddress(request);
        } catch (Exception e) {
            return "unknown";
        }
    }

    private List<String> getAllPermissions() {
        // 管理员拥有所有权限
        return Arrays.asList(
            "user:view", "user:edit", "user:delete",
            "product:view", "product:edit", "product:delete",
            "order:view", "order:edit", "order:delete",
            "category:view", "category:edit", "category:delete",
            "admin:view", "admin:edit", "admin:delete",
            "system:view", "system:manage", "system:logs",
            "statistics:view", "cache:manage"
        );
    }

    private List<DashboardStatsResponse.CategoryStats> getCategoryStatsList() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> {
            Long productCount = productRepository.countByCategoryId(category.getId());
            Long orderCount = orderRepository.countByCategoryId(category.getId());
            BigDecimal totalAmount = orderRepository.sumAmountByCategoryIdAndStatus(category.getId(), 3);

            return new DashboardStatsResponse.CategoryStats(
                category.getName(),
                productCount,
                orderCount,
                totalAmount != null ? totalAmount : BigDecimal.ZERO
            );
        }).collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.DailyStats> getUserActivityStatsList() {
        List<DashboardStatsResponse.DailyStats> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);
            
            Long count = userRepository.countByCreateTimeBetween(startOfDay, endOfDay);
            stats.add(new DashboardStatsResponse.DailyStats(
                date.format(formatter),
                count,
                BigDecimal.ZERO
            ));
        }
        
        return stats;
    }

    private List<DashboardStatsResponse.DailyStats> getTransactionStatsList() {
        List<DashboardStatsResponse.DailyStats> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);
            
            Long count = orderRepository.countByCreateTimeBetweenAndStatus(startOfDay, endOfDay, 3);
            BigDecimal amount = orderRepository.sumAmountByCreateTimeBetweenAndStatus(startOfDay, endOfDay, 3);
            
            stats.add(new DashboardStatsResponse.DailyStats(
                date.format(formatter),
                count,
                amount != null ? amount : BigDecimal.ZERO
            ));
        }
        
        return stats;
    }

    private List<DashboardStatsResponse.PopularProduct> getPopularProductsList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "viewCount"));
        Page<Product> products = productRepository.findByStatus(1, pageable);
        
        return products.getContent().stream().map(product -> {
            DashboardStatsResponse.PopularProduct popularProduct = new DashboardStatsResponse.PopularProduct();
            popularProduct.setId(product.getId());
            popularProduct.setTitle(product.getTitle());
            popularProduct.setPrice(product.getPrice());
            popularProduct.setViewCount(product.getViewCount());
            
            // 获取分类名称
            if (product.getCategory() != null) {
                popularProduct.setCategoryName(product.getCategory().getName());
            }
            
            return popularProduct;
        }).collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.RecentUser> getRecentUsersList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> users = userRepository.findAll(pageable);
        
        return users.getContent().stream().map(user -> {
            DashboardStatsResponse.RecentUser recentUser = new DashboardStatsResponse.RecentUser();
            recentUser.setId(user.getId());
            recentUser.setUsername(user.getUsername());
            recentUser.setRealName(user.getRealName());
            recentUser.setStudentId(user.getStudentId());
            recentUser.setCreateTime(user.getCreateTime());
            return recentUser;
        }).collect(Collectors.toList());
    }
}
