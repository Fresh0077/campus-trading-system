package com.example.campussystem.controller;

import com.example.campussystem.dto.AIChatRequest;
import com.example.campussystem.dto.AIChatResponse;
import com.example.campussystem.service.AIAssistantService;
import com.example.campussystem.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI智能客服控制器
 */
@RestController
@RequestMapping("/ai-assistant")
@Tag(name = "AI智能客服", description = "AI智能客服相关接口")
public class AIAssistantController {
    
    @Autowired
    private AIAssistantService aiAssistantService;
    
    /**
     * 发送消息给AI助手
     */
    @PostMapping("/chat")
    @Operation(summary = "与AI助手聊天", description = "发送消息给AI智能客服助手")
    public Result<AIChatResponse> chat(
            @Valid @RequestBody AIChatRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            AIChatResponse response = aiAssistantService.chat(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("AI助手服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 清除对话历史
     */
    @DeleteMapping("/conversation/{sessionId}")
    @Operation(summary = "清除对话历史", description = "清除指定会话的对话历史")
    public Result<Void> clearConversation(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            aiAssistantService.clearConversationHistory(sessionId);
            return Result.success("对话历史已清除", null);
        } catch (Exception e) {
            return Result.error("清除对话历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取AI助手信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取AI助手信息", description = "获取AI智能客服助手的基本信息和能力")
    public Result<Map<String, Object>> getAssistantInfo() {
        try {
            Map<String, Object> info = aiAssistantService.getAssistantInfo();
            return Result.success(info);
        } catch (Exception e) {
            return Result.error("获取助手信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "AI助手健康检查", description = "检查AI助手服务是否正常")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "ok",
            "service", "AI Assistant",
            "timestamp", System.currentTimeMillis()
        );
        return Result.success(health);
    }
}
