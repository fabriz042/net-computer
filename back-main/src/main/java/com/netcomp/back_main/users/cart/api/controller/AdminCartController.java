package com.netcomp.back_main.users.cart.api.controller;

import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.users.cart.api.request.AdminCartSearchRequest;
import com.netcomp.back_main.users.cart.api.response.AdminCartResponse;
import com.netcomp.back_main.users.cart.api.response.CartResponse;
import com.netcomp.back_main.users.cart.service.AdminCartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/carts")
public class AdminCartController {

    private final AdminCartService cartService;

    public AdminCartController(AdminCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminCartResponse>> list(
            @Valid @ModelAttribute AdminCartSearchRequest request
    ) {
        return ResponseEntity.ok(cartService.getActiveCarts(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> retrieve(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }
}
