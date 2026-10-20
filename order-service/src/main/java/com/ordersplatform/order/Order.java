package com.ordersplatform.order;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(String id, String customerEmail, String item, int quantity, BigDecimal price, Instant createdAt) {}
