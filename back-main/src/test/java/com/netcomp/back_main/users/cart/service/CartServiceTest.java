package com.netcomp.back_main.users.cart.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import com.netcomp.back_main.support.TestFixtures;
import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import com.netcomp.back_main.users.cart.api.request.MergeCartRequest;
import com.netcomp.back_main.users.cart.api.request.UpdateCartItemRequest;
import com.netcomp.back_main.users.cart.api.response.CartResponse;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.repository.CartRepository;
import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntityManager entityManager;

    private CartService cartService;
    private User user;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productRepository, entityManager);
        user = TestFixtures.user();
        lenient().when(entityManager.find(User.class, user.getId())).thenReturn(user);
        lenient().when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setUserId(user.getId());
            return cart;
        });
    }

    @Test
    void returnsEmptyCartWhenUserHasNoCart() {
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(user.getId());

        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
        assertFalse(response.checkoutAvailable());
    }

    @Test
    void createsCartAndAddsProduct() {
        Product product = TestFixtures.product("Monitor LG", "899.90", 5);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.empty());
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartResponse response = cartService.addItem(user.getId(), new AddToCartRequest(product.getId(), 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.itemCount());
        assertEquals(new BigDecimal("1799.80"), response.total());
        assertTrue(response.checkoutAvailable());
    }

    @Test
    void incrementsQuantityWhenProductIsAlreadyInCart() {
        Product product = TestFixtures.product("Mouse Logitech", "120.00", 10);
        Cart cart = TestFixtures.cart(user, product, 3);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartResponse response = cartService.addItem(user.getId(), new AddToCartRequest(product.getId(), 2));

        assertEquals(1, response.items().size());
        assertEquals(5, response.items().get(0).quantity());
    }

    @Test
    void rejectsAddingMoreUnitsThanAvailableStock() {
        Product product = TestFixtures.product("RTX 5070", "2999.00", 2);
        Cart cart = TestFixtures.cart(user, product, 2);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cartService.addItem(user.getId(), new AddToCartRequest(product.getId(), 1)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Solo hay 2 unidades"));
        verify(cartRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsInactiveProducts() {
        Product product = TestFixtures.product("Teclado", "150.00", 4);
        product.setActive(false);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.empty());
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cartService.addItem(user.getId(), new AddToCartRequest(product.getId(), 1)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void rejectsUnknownUsers() {
        UUID unknownUserId = UUID.randomUUID();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cartService.getCart(unknownUserId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updatesQuantityWithinStock() {
        Product product = TestFixtures.product("Audífonos", "250.00", 6);
        Cart cart = TestFixtures.cart(user, product, 1);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.updateItem(user.getId(), product.getId(), new UpdateCartItemRequest(6));

        assertEquals(6, response.items().get(0).quantity());
        assertEquals(new BigDecimal("1500.00"), response.total());
    }

    @Test
    void removingMissingProductFails() {
        Product product = TestFixtures.product("Audífonos", "250.00", 6);
        Cart cart = TestFixtures.cart(user, product, 1);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cartService.removeItem(user.getId(), UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void removesProductFromCart() {
        Product product = TestFixtures.product("Audífonos", "250.00", 6);
        Cart cart = TestFixtures.cart(user, product, 1);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeItem(user.getId(), product.getId());

        assertTrue(response.items().isEmpty());
    }

    @Test
    void mergeCapsQuantitiesAtStockAndSkipsUnavailableProducts() {
        Product available = TestFixtures.product("SSD 1TB", "300.00", 3);
        Product soldOut = TestFixtures.product("Fuente 750W", "400.00", 0);
        UUID missingProductId = UUID.randomUUID();
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.empty());
        when(productRepository.findById(available.getId())).thenReturn(Optional.of(available));
        when(productRepository.findById(soldOut.getId())).thenReturn(Optional.of(soldOut));
        when(productRepository.findById(missingProductId)).thenReturn(Optional.empty());

        CartResponse response = cartService.mergeItems(user.getId(), new MergeCartRequest(List.of(
                new AddToCartRequest(available.getId(), 2),
                new AddToCartRequest(available.getId(), 4),
                new AddToCartRequest(soldOut.getId(), 1),
                new AddToCartRequest(missingProductId, 1)
        )));

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
    }

    @Test
    void flagsItemsThatExceedCurrentStock() {
        Product product = TestFixtures.product("Laptop", "4500.00", 1);
        Cart cart = TestFixtures.cart(user, product, 3);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(user.getId());

        assertFalse(response.items().get(0).available());
        assertFalse(response.checkoutAvailable());
    }
}
