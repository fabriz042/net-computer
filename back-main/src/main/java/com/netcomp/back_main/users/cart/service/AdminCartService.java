package com.netcomp.back_main.users.cart.service;

import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.users.cart.api.request.AdminCartSearchRequest;
import com.netcomp.back_main.users.cart.api.response.AdminCartResponse;
import com.netcomp.back_main.users.cart.api.response.CartResponse;
import com.netcomp.back_main.users.cart.repository.CartRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminCartService {

    // --- Dependencies ---
    private final CartRepository cartRepository;

    // --- Constructor ---
    public AdminCartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // --- Public operations ---
    public PageResponse<AdminCartResponse> getActiveCarts(AdminCartSearchRequest request) {
        Pageable pageable = PageRequest.of(request.resolvedPage() - 1, request.resolvedPerPage());
        String search = StringUtils.trimToEmpty(request.search());
        return PageResponse.from(cartRepository.searchActiveCarts(search, pageable), AdminCartResponse::from);
    }

    public CartResponse getCart(UUID userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .map(CartResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario no tiene un carrito registrado."
                ));
    }
}
