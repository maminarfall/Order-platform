package com.ordersplatform.order;

import java.math.BigDecimal;

public record CreateOrderRequest(String customerEmail, String item, int quantity, BigDecimal price) {}
