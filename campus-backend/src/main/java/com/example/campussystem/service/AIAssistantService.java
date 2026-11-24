package com.example.campussystem.service;

import com.example.campussystem.client.DeepSeekClient;
import com.example.campussystem.dto.AIChatRequest;
import com.example.campussystem.dto.AIChatResponse;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.Order;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI智能客服服务
 */
@Service
public class AIAssistantService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);
    private static final String CONVERSATION_HISTORY_PREFIX = "ai:conversation:";
    private static final int MAX_HISTORY_SIZE = 10; // 保留最近10轮对话
    private static final long HISTORY_EXPIRE_HOURS = 2; // 对话历史保留2小时
    
    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired(required = false)
    private ProductRepository productRepository;
    
    @Autowired(required = false)
    private OrderRepository orderRepository;
    
    @Value("${deepseek.assistant.system-prompt}")
    private String systemPrompt;
    
    @Value("${deepseek.assistant.name:AI助手}")
    private String assistantName;
    
    /**
     * 处理用户聊天请求
     */
    public AIChatResponse chat(AIChatRequest request, Long userId) {
        try {
            // 生成或获取会话ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = generateSessionId(userId);
            }
            
            // 构建增强的系统提示词
            String enhancedPrompt = buildEnhancedPrompt(request, userId);
            
            // 获取对话历史
            List<Map<String, String>> conversationHistory = getConversationHistory(sessionId);
            
            // 调用DeepSeek API
            String reply = deepSeekClient.chat(request.getMessage(), enhancedPrompt, conversationHistory);
            
            // 保存对话历史
            saveConversationHistory(sessionId, request.getMessage(), reply);
            
            // 构建响应
            return AIChatResponse.builder()
                    .reply(reply)
                    .sessionId(sessionId)
                    .timestamp(System.currentTimeMillis())
                    .success(true)
                    .build();
                    
        } catch (IOException e) {
            logger.error("AI聊天失败: {}", e.getMessage(), e);
            return AIChatResponse.builder()
                    .reply("抱歉，我现在遇到了一些技术问题，请稍后再试或联系人工客服。")
                    .sessionId(request.getSessionId())
                    .timestamp(System.currentTimeMillis())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("处理AI请求时发生异常: {}", e.getMessage(), e);
            return AIChatResponse.builder()
                    .reply("抱歉，处理您的请求时出现了错误，请稍后再试。")
                    .sessionId(request.getSessionId())
                    .timestamp(System.currentTimeMillis())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 构建增强的系统提示词（包含上下文信息）
     */
    private String buildEnhancedPrompt(AIChatRequest request, Long userId) {
        StringBuilder promptBuilder = new StringBuilder(systemPrompt);
        promptBuilder.append("\n\n当前用户ID: ").append(userId);
        
        // 如果有商品ID，添加商品信息
        if (request.getProductId() != null && productRepository != null) {
            try {
                Optional<Product> productOpt = productRepository.findById(request.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    promptBuilder.append("\n\n用户正在咨询的商品信息：");
                    promptBuilder.append("\n- 商品名称: ").append(product.getTitle());
                    promptBuilder.append("\n- 价格: ¥").append(product.getPrice());
                    promptBuilder.append("\n- 描述: ").append(product.getDescription());
                    promptBuilder.append("\n- 状态: ").append(getProductStatusText(product.getStatus()));
                }
            } catch (Exception e) {
                logger.warn("获取商品信息失败: {}", e.getMessage());
            }
        }
        
        // 如果有订单ID，添加订单信息
        if (request.getOrderId() != null && orderRepository != null) {
            try {
                Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    promptBuilder.append("\n\n用户正在咨询的订单信息：");
                    promptBuilder.append("\n- 订单号: ").append(order.getOrderNo());
                    promptBuilder.append("\n- 金额: ¥").append(order.getAmount());
                    promptBuilder.append("\n- 状态: ").append(getOrderStatusText(order.getStatus()));
                }
            } catch (Exception e) {
                logger.warn("获取订单信息失败: {}", e.getMessage());
            }
        }
        
        return promptBuilder.toString();
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId(Long userId) {
        return "session_" + userId + "_" + System.currentTimeMillis();
    }
    
    /**
     * 获取对话历史
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getConversationHistory(String sessionId) {
        try {
            String key = CONVERSATION_HISTORY_PREFIX + sessionId;
            Object historyObj = redisTemplate.opsForValue().get(key);
            if (historyObj instanceof List) {
                return (List<Map<String, String>>) historyObj;
            }
        } catch (Exception e) {
            logger.warn("获取对话历史失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }
    
    /**
     * 保存对话历史
     */
    private void saveConversationHistory(String sessionId, String userMessage, String assistantReply) {
        try {
            String key = CONVERSATION_HISTORY_PREFIX + sessionId;
            List<Map<String, String>> history = getConversationHistory(sessionId);
            
            // 添加用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            history.add(userMsg);
            
            // 添加助手回复
            Map<String, String> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", assistantReply);
            history.add(assistantMsg);
            
            // 限制历史记录大小
            while (history.size() > MAX_HISTORY_SIZE * 2) {
                history.remove(0);
            }
            
            // 保存到Redis
            redisTemplate.opsForValue().set(key, history, HISTORY_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("保存对话历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 清除对话历史
     */
    public void clearConversationHistory(String sessionId) {
        try {
            String key = CONVERSATION_HISTORY_PREFIX + sessionId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("清除对话历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取商品状态文本
     */
    private String getProductStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已下架";
            case 1: return "在售";
            case 2: return "已预订";
            case 3: return "已售出";
            default: return "未知";
        }
    }
    
    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "进行中";
            case 2: return "已取消";
            case 3: return "已完成";
            default: return "未知";
        }
    }
    
    /**
     * 获取助手信息
     */
    public Map<String, Object> getAssistantInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", assistantName);
        info.put("description", "我是您的智能客服助手，可以帮您解答关于平台使用、商品交易、订单处理等各类问题。");
        info.put("capabilities", Arrays.asList(
            "回答平台使用问题",
            "商品咨询和推荐",
            "订单状态查询",
            "交易流程指导",
            "问题投诉处理"
        ));
        return info;
    }
}
