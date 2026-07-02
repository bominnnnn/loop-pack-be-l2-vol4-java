package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public record OrderInfo(
    Long orderId,
    Long userId,
    Long couponId,
    Long originalPrice,
    Long discountAmount,
    Long totalPrice,
    OrderStatus status,
    List<OrderItemInfo> items
) {
    public record OrderItemInfo(
        Long orderItemId,
        Long productId,
        String productName,
        String brandName,
        Long price,
        int quantity
    ) {
        public static OrderItemInfo from(OrderItem item) {
            return new OrderItemInfo(
                item.getId(),
                item.getProductId(),
                item.getSnapshot().getProductName(),
                item.getSnapshot().getBrandName(),
                item.getSnapshot().getPrice(),
                item.getQuantity()
            );
        }
    }

    public static OrderInfo from(Order order) {
        return new OrderInfo(
            order.getId(),
            order.getUserId(),
            order.getCouponId(),
            order.getOriginalPrice(),
            order.getDiscountAmount(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getItems().stream().map(OrderItemInfo::from).toList()
        );
    }
}
