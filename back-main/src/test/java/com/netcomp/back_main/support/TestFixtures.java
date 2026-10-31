package com.netcomp.back_main.support;

import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderItem;
import com.netcomp.back_main.orders.order.domain.OrderStatus;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.domain.CartItem;
import com.netcomp.back_main.users.user.domain.Role;
import com.netcomp.back_main.users.user.domain.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user() {
        User user = new User(new Role("ROLE_CUSTOMER", "Cliente"), "Ana Torres", "ana@example.com", "hash", true);
        user.setId(UUID.randomUUID());
        return user;
    }

    public static Product product(String name, String price, int stock) {
        Category category = new Category();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setSlug(name.toLowerCase().replace(' ', '-'));
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(true);
        product.setCategory(category);
        return product;
    }

    public static Cart cart(User user, Product product, int quantity) {
        Cart cart = new Cart(user);
        cart.setUserId(user.getId());
        CartItem item = new CartItem(cart, product, quantity);
        item.setId(UUID.randomUUID());
        item.setCreatedAt(Instant.now());
        cart.getItems().add(item);
        return cart;
    }

    public static OrderStatus status(OrderStatusCode code) {
        String name = switch (code) {
            case PENDING -> "Pendiente";
            case PAID -> "Pagado";
            case PROCESSING -> "En Proceso";
            case SHIPPED -> "Enviado";
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
        };
        return new OrderStatus(code.name(), name);
    }

    public static Order order(User user, OrderStatusCode code, Product product, int quantity) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        Order order = new Order(user, status(code), subtotal, "Av. Arequipa 123, Lima");
        order.setId(UUID.randomUUID());
        order.setCreatedAt(Instant.parse("2026-10-15T15:30:00Z"));
        OrderItem item = new OrderItem(order, product, product.getName(), quantity, product.getPrice(), subtotal);
        item.setId(UUID.randomUUID());
        order.getItems().add(item);
        return order;
    }
}
