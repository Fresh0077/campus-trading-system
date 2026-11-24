package com.example.campussystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 订单请求DTO
 */
public class OrderRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 【已废弃】前端传递的金额字段，仅用于前端显示
     * 后端不再使用此字段，而是直接从商品表查询实际价格
     * 这是为了防止恶意用户篡改价格进行低价购买
     */
    @Deprecated
    private BigDecimal amount;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public OrderRequest() {}

    public OrderRequest(Long productId, BigDecimal amount, String remark) {
        this.productId = productId;
        this.amount = amount;
        this.remark = remark;
    }

    // Getter和Setter方法
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "productId=" + productId +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                '}';
    }
}