package com.example.campussystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 基于 STOMP 的 WebSocket 推送服务。
 * 说明：仅用于系统通知推送，不再处理私聊消息。
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    // 系统通知广播目的地（前端应订阅 /topic/notifications）
    public static final String SYSTEM_NOTIFICATIONS_TOPIC = "/topic/notifications";

    // 新订单通知队列（前端应订阅 /user/queue/new-order）
    public static final String USER_NEW_ORDER_QUEUE = "/queue/new-order";

    // 订单状态更新通知队列（前端应订阅 /user/queue/order-update）
    public static final String USER_ORDER_UPDATE_QUEUE = "/queue/order-update";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 向所有用户广播系统通知。
     * 前端应通过 STOMP 订阅 /topic/notifications 接收。
     *
     * @param notification 通知内容
     */
    public void broadcastSystemNotification(SystemNotification notification) {
        if (notification == null) {
            logger.warn("broadcastSystemNotification skipped: notification is null");
            return;
        }
        try {
            logger.info("广播系统通知: {}", notification.getTitle());
            messagingTemplate.convertAndSend(SYSTEM_NOTIFICATIONS_TOPIC, notification.toMap());
        } catch (Exception e) {
            logger.error("广播系统通知失败", e);
        }
    }

    /**
     * 向指定用户发送系统通知。
     *
     * @param userId 用户ID
     * @param notification 通知内容
     */
    public void sendSystemNotification(Long userId, SystemNotification notification) {
        if (userId == null || notification == null) {
            logger.warn("sendSystemNotification skipped: userId or notification is null");
            return;
        }
        try {
            logger.info("发送系统通知给用户 {}, 标题: {}", userId, notification.getTitle());
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notification", notification.toMap());
        } catch (Exception e) {
            logger.error("发送系统通知失败, userId={}", userId, e);
        }
    }
    
    /**
     * 向卖家发送"新订单"通知。
     * 前端应通过 STOMP 订阅 /user/queue/new-order 接收。
     *
     * @param sellerId 卖家用户ID
     * @param payload 订单信息
     */
    public void sendNewOrderNotification(Long sellerId, Object payload) {
        if (sellerId == null) {
            logger.warn("sendNewOrderNotification skipped: sellerId is null");
            return;
        }
        try {
            logger.info("发送新订单通知给卖家, sellerId={}", sellerId);
            messagingTemplate.convertAndSendToUser(String.valueOf(sellerId), USER_NEW_ORDER_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送新订单通知失败, sellerId={}", sellerId, e);
        }
    }
    
    /**
     * 向用户发送"订单状态更新"通知。
     * 前端应通过 STOMP 订阅 /user/queue/order-update 接收。
     *
     * @param userId 用户ID（买家或卖家）
     * @param payload 订单更新信息
     */
    public void sendOrderUpdateNotification(Long userId, Object payload) {
        if (userId == null) {
            logger.warn("sendOrderUpdateNotification skipped: userId is null");
            return;
        }
        try {
            logger.info("发送订单更新通知, userId={}", userId);
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), USER_ORDER_UPDATE_QUEUE, payload);
        } catch (Exception e) {
            logger.error("发送订单更新通知失败, userId={}", userId, e);
        }
    }

    /**
     * 系统通知数据类
     */
    public static class SystemNotification {
        private String id;
        private String type;
        private String title;
        private String content;
        private String level; // info, success, warning, error
        private String category; // order, product, user, system
        private Long relatedId; // 关联的ID
        private String actionUrl;
        private String actionText;
        private LocalDateTime timestamp;

        public SystemNotification() {
            this.id = UUID.randomUUID().toString();
            this.timestamp = LocalDateTime.now();
        }

        public SystemNotification(String type, String title, String content) {
            this();
            this.type = type;
            this.title = title;
            this.content = content;
            this.level = "info";
        }

        public SystemNotification(String type, String title, String content, String level) {
            this(type, title, content);
            this.level = level;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Long getRelatedId() { return relatedId; }
        public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

        public String getActionText() { return actionText; }
        public void setActionText(String actionText) { this.actionText = actionText; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        /**
         * 转换为Map格式，用于WebSocket传输
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("type", type);
            map.put("title", title);
            map.put("content", content);
            map.put("level", level);
            map.put("category", category);
            map.put("relatedId", relatedId);
            map.put("actionUrl", actionUrl);
            map.put("actionText", actionText);
            map.put("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return map;
        }
    }
}