package com.ordersplatform.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    // In-memory store keeps the demo simple. Swap for RDS later if you want.
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final OrderEventPublisher publisher;

    public OrderController(OrderEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        if (req.item() == null || req.item().isBlank() || req.quantity() < 1
                || req.price() == null || req.price().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "item, quantity >= 1 and price > 0 are required"));
        }
        Order order = new Order(UUID.randomUUID().toString(), req.customerEmail(), req.item(),
                req.quantity(), req.price(), Instant.now());
        orders.put(order.id(), order);
        publisher.publishOrderCreated(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public Collection<Order> list() {
        return orders.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable String id) {
        Order o = orders.get(id);
        return o == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(o);
    }
}
