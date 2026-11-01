package com.netcomp.back_main.orders.order.api.controller;

import com.netcomp.back_main.common.api.request.RequestHeaders;
import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.orders.order.api.request.CreateGuestOrderRequest;
import com.netcomp.back_main.orders.order.api.request.CreateOrderRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class PublicOrderController {

    private final OrderService orderService;

    public PublicOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userId, request));
    }

    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> createGuest(@Valid @RequestBody CreateGuestOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createGuestOrder(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> list(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(name = "per_page", required = false) Integer perPage
    ) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, perPage));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> retrieve(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderService.getUserOrder(userId, orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderService.cancelUserOrder(userId, orderId));
    }
}
