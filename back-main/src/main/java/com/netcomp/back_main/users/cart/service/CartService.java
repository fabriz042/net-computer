package com.netcomp.back_main.users.cart.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import com.netcomp.back_main.users.cart.api.request.MergeCartRequest;
import com.netcomp.back_main.users.cart.api.request.UpdateCartItemRequest;
import com.netcomp.back_main.users.cart.api.response.CartResponse;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.domain.CartItem;
import com.netcomp.back_main.users.cart.repository.CartRepository;
import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CartService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);
    private static final int MAX_QUANTITY_PER_PRODUCT = 99;

    // --- Dependencies ---
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    // --- Constructor ---
    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            EntityManager entityManager
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    // --- Public operations ---
    public CartResponse getCart(UUID userId) {
        requireActiveUser(userId);
        return cartRepository.findWithItemsByUserId(userId)
                .map(CartResponse::from)
                .orElseGet(() -> CartResponse.empty(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = requirePurchasableProduct(request.productId());
        Optional<CartItem> existingItem = findItem(cart, product.getId());
        int currentQuantity = existingItem.map(CartItem::getQuantity).orElse(0);
        int requestedQuantity = currentQuantity + request.quantity();

        ensureStock(product, requestedQuantity);

        existingItem.ifPresentOrElse(
                item -> item.setQuantity(requestedQuantity),
                () -> cart.getItems().add(new CartItem(cart, product, requestedQuantity))
        );

        LOGGER.info("Cart item added. userId={}, productId={}, quantity={}",
                userId, product.getId(), requestedQuantity);
        return saveAndBuildResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(UUID userId, UUID productId, UpdateCartItemRequest request) {
        Cart cart = requireCart(userId);
        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El producto no está en tu carrito."
                ));

        ensureStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());

        return saveAndBuildResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID productId) {
        Cart cart = requireCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El producto no está en tu carrito."
            );
        }

        return saveAndBuildResponse(cart);
    }

    @Transactional
    public CartResponse clearCart(UUID userId) {
        requireActiveUser(userId);
        Optional<Cart> cart = cartRepository.findWithItemsByUserId(userId);

        if (cart.isEmpty()) {
            return CartResponse.empty(userId);
        }

        cart.get().getItems().clear();
        return saveAndBuildResponse(cart.get());
    }

    @Transactional
    public CartResponse mergeItems(UUID userId, MergeCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Map<UUID, Integer> requestedQuantities = new LinkedHashMap<>();

        for (AddToCartRequest item : request.items()) {
            requestedQuantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        requestedQuantities.forEach((productId, quantity) -> productRepository.findById(productId)
                .filter(product -> Boolean.TRUE.equals(product.getActive()) && product.getStock() > 0)
                .ifPresent(product -> mergeProduct(cart, product, quantity)));

        LOGGER.info("Guest cart merged. userId={}, products={}", userId, requestedQuantities.size());
        return saveAndBuildResponse(cart);
    }

    // --- Private helpers ---
    private void mergeProduct(Cart cart, Product product, int quantity) {
        Optional<CartItem> existingItem = findItem(cart, product.getId());
        int mergedQuantity = Math.min(
                existingItem.map(CartItem::getQuantity).orElse(0) + quantity,
                Math.min(product.getStock(), MAX_QUANTITY_PER_PRODUCT)
        );

        existingItem.ifPresentOrElse(
                item -> item.setQuantity(mergedQuantity),
                () -> cart.getItems().add(new CartItem(cart, product, mergedQuantity))
        );
    }

    private CartResponse saveAndBuildResponse(Cart cart) {
        Cart savedCart = cartRepository.saveAndFlush(cart);
        return CartResponse.from(savedCart);
    }

    private Cart getOrCreateCart(UUID userId) {
        User user = requireActiveUser(userId);
        return cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private Cart requireCart(UUID userId) {
        requireActiveUser(userId);
        return cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tu carrito está vacío."
                ));
    }

    private Optional<CartItem> findItem(Cart cart, UUID productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    private User requireActiveUser(UUID userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe.");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario está inactivo.");
        }
        return user;
    }

    private Product requirePurchasableProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El producto no existe."
                ));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto ya no está disponible."
            );
        }
        return product;
    }

    private void ensureStock(Product product, int requestedQuantity) {
        if (requestedQuantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad máxima por producto es " + MAX_QUANTITY_PER_PRODUCT + "."
            );
        }
        if (product.getStock() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto " + product.getName() + " está agotado."
            );
        }
        if (requestedQuantity > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo hay " + product.getStock() + " unidades disponibles de " + product.getName() + "."
            );
        }
    }
}
