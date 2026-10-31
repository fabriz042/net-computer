package com.netcomp.back_main.users.cart.api.controller;

import com.netcomp.back_main.common.api.request.RequestHeaders;
import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import com.netcomp.back_main.users.cart.api.request.MergeCartRequest;
import com.netcomp.back_main.users.cart.api.request.UpdateCartItemRequest;
import com.netcomp.back_main.users.cart.api.response.CartResponse;
import com.netcomp.back_main.users.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class PublicCartController {

    private final CartService cartService;

    public PublicCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> retrieve(@RequestHeader(RequestHeaders.USER_ID) UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear(@RequestHeader(RequestHeaders.USER_ID) UUID userId) {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> merge(
            @RequestHeader(RequestHeaders.USER_ID) UUID userId,
            @Valid @RequestBody MergeCartRequest request
    ) {
        return ResponseEntity.ok(cartService.mergeItems(userId, request));
    }
}
