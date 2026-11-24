package com.example.campussystem.service;

import com.example.campussystem.dto.OrderRequest;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.entity.Order;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.OrderRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务类
 */
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private CacheService cacheService;

    /**
     * 创建订单
     */
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        // 验证商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 验证商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException("商品不可购买");
        }

        // 验证买家不能购买自己的商品
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 验证买家是否存在
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException("买家不存在"));

        // 【安全修复】使用商品的实际价格，而不是请求中的金额
        // 防止买家篡改价格进行恶意购买
        BigDecimal actualAmount = product.getPrice();
        
        // 验证价格有效性
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格无效");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setProductId(request.getProductId());
        order.setAmount(actualAmount); // 使用商品实际价格
        order.setRemark(request.getRemark());
        order.setStatus(1); // 待确认
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);

        Order savedOrder = orderRepository.save(order);

        // 更新商品状态为已预订
        product.setStatus(2);
        productRepository.save(product);
        
        // 转换为响应对象
        OrderResponse response = convertToResponse(savedOrder);
        
        // 【关键】发送新订单通知给卖家
        try {
            webSocketService.sendNewOrderNotification(product.getUserId(), response);
        } catch (Exception e) {
            // 通知失败不影响订单创建，只记录日志
            System.err.println("发送新订单通知失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限：只有买家或卖家可以查看订单详情
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    /**
     * 根据订单号获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNo(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限：只有买家或卖家可以取消订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态：只有待确认或进行中的订单可以取消
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        // 恢复商品状态
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null) {
            product.setStatus(1); // 可售
            productRepository.save(product);
        }
        
        // 【关键】清除买家和卖家的统计缓存
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
        
        // 【关键】发送订单取消通知给买家和卖家
        try {
            OrderResponse response = convertToResponse(updatedOrder);
            webSocketService.sendOrderUpdateNotification(order.getBuyerId(), response);
            webSocketService.sendOrderUpdateNotification(order.getSellerId(), response);
        } catch (Exception e) {
            System.err.println("发送订单取消通知失败: " + e.getMessage());
        }
    }

    /**
     * 确认订单（买家确认）
     */
    public void confirmOrderByBuyer(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() == 4) {
            throw new BusinessException("订单已取消，无法确认");
        }

        // 买家确认
        order.setBuyerConfirm(1);
        
        // 检查是否双方都已确认
        if (order.getSellerConfirm() == 1) {
            order.setStatus(3); // 已完成
            order.setCompleteTime(LocalDateTime.now());
            
            // 更新商品状态为已售出
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null) {
                product.setStatus(3); // 已售出
                productRepository.save(product);
            }
        } else {
            order.setStatus(2); // 进行中
        }

        Order updatedOrder = orderRepository.save(order);
        
        // 【关键】清除买家和卖家的统计缓存
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
        
        // 【关键】发送订单状态更新通知
        try {
            OrderResponse response = convertToResponse(updatedOrder);
            // 通知买家和卖家
            webSocketService.sendOrderUpdateNotification(order.getBuyerId(), response);
            webSocketService.sendOrderUpdateNotification(order.getSellerId(), response);
        } catch (Exception e) {
            System.err.println("发送订单更新通知失败: " + e.getMessage());
        }
    }

    /**
     * 确认订单（卖家确认）
     */
    public void confirmOrderBySeller(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() == 4) {
            throw new BusinessException("订单已取消，无法确认");
        }

        // 卖家确认
        order.setSellerConfirm(1);
        
        // 检查是否双方都已确认
        if (order.getBuyerConfirm() == 1) {
            order.setStatus(3); // 已完成
            order.setCompleteTime(LocalDateTime.now());
            
            // 更新商品状态为已售出
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null) {
                product.setStatus(3); // 已售出
                productRepository.save(product);
            }
        } else {
            order.setStatus(2); // 进行中
        }

        Order updatedOrder = orderRepository.save(order);
        
        // 【关键】清除买家和卖家的统计缓存
        clearUserStatsCache(order.getBuyerId());
        clearUserStatsCache(order.getSellerId());
        
        // 【关键】发送订单状态更新通知
        try {
            OrderResponse response = convertToResponse(updatedOrder);
            // 通知买家和卖家
            webSocketService.sendOrderUpdateNotification(order.getBuyerId(), response);
            webSocketService.sendOrderUpdateNotification(order.getSellerId(), response);
        } catch (Exception e) {
            System.err.println("发送订单更新通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的买家订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreateTimeDesc(buyerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户的卖家订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerIdOrderByCreateTimeDesc(sellerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户的所有订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findUserOrders(userId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 根据状态获取用户订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrdersByStatus(Long userId, Integer status) {
        List<Order> orders = orderRepository.findUserOrdersByStatus(userId, status);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 分页获取用户订单列表
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrdersPage(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderRepository.findUserOrders(userId, pageable);
        return orderPage.map(this::convertToResponse);
    }

    /**
     * 获取买家待确认订单
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerPendingOrders(Long buyerId) {
        List<Order> orders = orderRepository.findBuyerPendingConfirmOrders(buyerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取卖家待确认订单
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerPendingOrders(Long sellerId) {
        List<Order> orders = orderRepository.findSellerPendingConfirmOrders(sellerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 统计用户订单数量
     */
    @Transactional(readOnly = true)
    public long countUserOrders(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return orderRepository.countByBuyerId(userId);
            case "seller":
                return orderRepository.countBySellerId(userId);
            default:
                return orderRepository.countByBuyerId(userId) + orderRepository.countBySellerId(userId);
        }
    }

    /**
     * 计算用户交易金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateUserTotalAmount(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return orderRepository.calculateBuyerTotalAmount(userId);
            case "seller":
                return orderRepository.calculateSellerTotalAmount(userId);
            default:
                BigDecimal buyerAmount = orderRepository.calculateBuyerTotalAmount(userId);
                BigDecimal sellerAmount = orderRepository.calculateSellerTotalAmount(userId);
                return buyerAmount.add(sellerAmount);
        }
    }

    /**
     * 获取用户订单统计
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserOrderStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        
        // 统计各种状态的订单数量
        stats.put("pending", orderRepository.countUserOrdersByStatus(userId, 1)); // 待确认
        stats.put("processing", orderRepository.countUserOrdersByStatus(userId, 2)); // 进行中
        stats.put("completed", orderRepository.countUserOrdersByStatus(userId, 3)); // 已完成
        stats.put("cancelled", orderRepository.countUserOrdersByStatus(userId, 4)); // 已取消
        
        // 统计买家和卖家订单数量
        stats.put("asBuyer", orderRepository.countByBuyerId(userId));
        stats.put("asSeller", orderRepository.countBySellerId(userId));
        
        // 总订单数
        stats.put("total", stats.get("asBuyer") + stats.get("asSeller"));
        
        return stats;
    }

    /**
     * 获取用户总交易金额
     */
    @Transactional(readOnly = true)
    public Double getUserTotalTradeAmount(Long userId) {
        try {
            // 只计算已完成订单的金额
            BigDecimal buyerAmount = orderRepository.calculateCompletedBuyerAmount(userId);
            BigDecimal sellerAmount = orderRepository.calculateCompletedSellerAmount(userId);
            
            if (buyerAmount == null) buyerAmount = BigDecimal.ZERO;
            if (sellerAmount == null) sellerAmount = BigDecimal.ZERO;
            
            return buyerAmount.add(sellerAmount).doubleValue();
        } catch (Exception e) {
            System.err.println("计算用户交易金额失败: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 10000));
        return "ORD" + timestamp + String.format("%04d", Integer.parseInt(random));
    }

    /**
     * 转换为响应DTO
     */
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setProductId(order.getProductId());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());
        response.setStatusText(getStatusText(order.getStatus()));
        response.setBuyerConfirm(order.getBuyerConfirm());
        response.setSellerConfirm(order.getSellerConfirm());
        response.setRemark(order.getRemark());
        response.setCancelReason(order.getCancelReason());
        response.setCancelTime(order.getCancelTime());
        response.setCompleteTime(order.getCompleteTime());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());

        // 设置买家信息
        if (order.getBuyer() != null) {
            response.setBuyerUsername(order.getBuyer().getUsername());
            response.setBuyerNickname(order.getBuyer().getRealName());
            response.setBuyerPhone(order.getBuyer().getPhone());
            response.setBuyerStudentId(order.getBuyer().getStudentId());
        } else {
            // 如果关联对象为空，手动查询
            userRepository.findById(order.getBuyerId()).ifPresent(buyer -> {
                response.setBuyerUsername(buyer.getUsername());
                response.setBuyerNickname(buyer.getRealName());
                response.setBuyerPhone(buyer.getPhone());
                response.setBuyerStudentId(buyer.getStudentId());
            });
        }

        // 设置卖家信息
        if (order.getSeller() != null) {
            response.setSellerUsername(order.getSeller().getUsername());
            response.setSellerNickname(order.getSeller().getRealName());
            response.setSellerPhone(order.getSeller().getPhone());
            response.setSellerStudentId(order.getSeller().getStudentId());
        } else {
            userRepository.findById(order.getSellerId()).ifPresent(seller -> {
                response.setSellerUsername(seller.getUsername());
                response.setSellerNickname(seller.getRealName());
                response.setSellerPhone(seller.getPhone());
                response.setSellerStudentId(seller.getStudentId());
            });
        }

        // 设置商品信息
        if (order.getProduct() != null) {
            response.setProductTitle(order.getProduct().getTitle());
            response.setProductImage(extractFirstImage(order.getProduct().getImages()));
            response.setProductPrice(order.getProduct().getPrice());
            response.setProductDescription(order.getProduct().getDescription());
            // 设置商品分类
            if (order.getProduct().getCategory() != null) {
                response.setProductCategory(order.getProduct().getCategory().getName());
            }
        } else {
            productRepository.findById(order.getProductId()).ifPresent(product -> {
                response.setProductTitle(product.getTitle());
                response.setProductImage(extractFirstImage(product.getImages()));
                response.setProductPrice(product.getPrice());
                response.setProductDescription(product.getDescription());
                // 设置商品分类
                if (product.getCategory() != null) {
                    response.setProductCategory(product.getCategory().getName());
                }
            });
        }

        return response;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待确认";
            case 2:
                return "进行中";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 从JSON格式的图片数组中提取第一张图片
     */
    private String extractFirstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试解析JSON数组
            if (imagesJson.trim().startsWith("[")) {
                // 简单的JSON数组解析
                String content = imagesJson.trim()
                        .substring(1, imagesJson.trim().length() - 1) // 去掉[]
                        .trim();
                
                if (content.isEmpty()) {
                    return null;
                }
                
                // 提取第一个元素
                String firstImage = content.split(",")[0].trim();
                // 去掉引号
                firstImage = firstImage.replaceAll("^\"|\"$", "");
                return firstImage.isEmpty() ? null : firstImage;
            } else {
                // 如果不是JSON数组格式，直接返回
                return imagesJson;
            }
        } catch (Exception e) {
            // 解析失败，返回原始字符串
            System.err.println("解析商品图片失败: " + e.getMessage());
            return imagesJson;
        }
    }
    
    /**
     * 清除用户的统计缓存
     */
    private void clearUserStatsCache(Long userId) {
        try {
            String cacheKey = "user_trade_stats:" + userId;
            cacheService.delete(cacheKey);
            System.out.println("已清除用户 " + userId + " 的统计缓存");
        } catch (Exception e) {
            System.err.println("清除用户统计缓存失败: " + e.getMessage());
        }
    }
}