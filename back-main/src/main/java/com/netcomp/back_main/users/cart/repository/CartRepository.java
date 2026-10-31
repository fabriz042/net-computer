package com.netcomp.back_main.users.cart.repository;

import com.netcomp.back_main.users.cart.domain.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT cart FROM Cart cart WHERE cart.userId = :userId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT new com.netcomp.back_main.users.cart.repository.CartSummaryView(
                cart.userId,
                customer.name,
                customer.email,
                COUNT(item.id),
                SUM(item.quantity),
                SUM(item.quantity * product.price),
                cart.createdAt,
                MAX(item.createdAt)
            )
            FROM Cart cart
            JOIN cart.user customer
            JOIN cart.items item
            JOIN item.product product
            WHERE :search = ''
                OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(customer.email) LIKE LOWER(CONCAT('%', :search, '%'))
            GROUP BY cart.userId, customer.name, customer.email, cart.createdAt
            ORDER BY MAX(item.createdAt) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT cart.userId)
            FROM Cart cart
            JOIN cart.user customer
            JOIN cart.items item
            WHERE :search = ''
                OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(customer.email) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<CartSummaryView> searchActiveCarts(@Param("search") String search, Pageable pageable);
}
