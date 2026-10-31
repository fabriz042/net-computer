package com.netcomp.back_main.orders.order.repository;

import com.netcomp.back_main.orders.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items", "user", "status"})
    @Query("SELECT customerOrder FROM Order customerOrder WHERE customerOrder.id = :id")
    Optional<Order> findDetailedById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"items", "user", "status"})
    @Query("SELECT customerOrder FROM Order customerOrder WHERE customerOrder.id = :id AND customerOrder.user.id = :userId")
    Optional<Order> findDetailedByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user", "status"})
    @Query(value = """
            SELECT customerOrder
            FROM Order customerOrder
            WHERE customerOrder.user.id = :userId
            ORDER BY customerOrder.createdAt DESC
            """,
            countQuery = "SELECT COUNT(customerOrder) FROM Order customerOrder WHERE customerOrder.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "status"})
    @Query(value = """
            SELECT customerOrder
            FROM Order customerOrder
            JOIN customerOrder.user customer
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
                AND (:allStatuses = TRUE OR customerOrder.status.code = :statusCode)
                AND (
                    :search = ''
                    OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(customer.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(customerOrder.id AS String)) LIKE LOWER(CONCAT(:search, '%'))
                )
            ORDER BY
                CASE WHEN :sortOrder = 'asc' THEN customerOrder.createdAt END ASC,
                CASE WHEN :sortOrder = 'desc' THEN customerOrder.createdAt END DESC
            """,
            countQuery = """
            SELECT COUNT(customerOrder)
            FROM Order customerOrder
            JOIN customerOrder.user customer
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
                AND (:allStatuses = TRUE OR customerOrder.status.code = :statusCode)
                AND (
                    :search = ''
                    OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(customer.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(customerOrder.id AS String)) LIKE LOWER(CONCAT(:search, '%'))
                )
            """)
    Page<Order> searchOrders(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("allStatuses") boolean allStatuses,
            @Param("statusCode") String statusCode,
            @Param("search") String search,
            @Param("sortOrder") String sortOrder,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT customerOrder
            FROM Order customerOrder
            JOIN FETCH customerOrder.user customer
            JOIN FETCH customerOrder.status
            LEFT JOIN FETCH customerOrder.items
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
                AND (:allStatuses = TRUE OR customerOrder.status.code = :statusCode)
                AND (
                    :search = ''
                    OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(customer.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CAST(customerOrder.id AS String)) LIKE LOWER(CONCAT(:search, '%'))
                )
            ORDER BY customerOrder.createdAt DESC
            """)
    List<Order> findOrdersForReport(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("allStatuses") boolean allStatuses,
            @Param("statusCode") String statusCode,
            @Param("search") String search
    );

    @Query("""
            SELECT new com.netcomp.back_main.orders.order.repository.OrderStatusTotalView(
                status.code,
                status.name,
                COUNT(customerOrder.id),
                SUM(customerOrder.total)
            )
            FROM Order customerOrder
            JOIN customerOrder.status status
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
            GROUP BY status.code, status.name
            """)
    List<OrderStatusTotalView> sumTotalsByStatus(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT new com.netcomp.back_main.orders.order.repository.TopProductView(
                item.product.id,
                item.productName,
                SUM(item.quantity),
                SUM(item.subtotal)
            )
            FROM OrderItem item
            JOIN item.order customerOrder
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
                AND customerOrder.status.code IN :statusCodes
            GROUP BY item.product.id, item.productName
            ORDER BY SUM(item.quantity) DESC, SUM(item.subtotal) DESC
            """)
    List<TopProductView> findTopProducts(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statusCodes") Collection<String> statusCodes,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(item.quantity), 0)
            FROM OrderItem item
            JOIN item.order customerOrder
            WHERE customerOrder.createdAt >= :from
                AND customerOrder.createdAt < :to
                AND customerOrder.status.code IN :statusCodes
            """)
    Long sumUnitsSold(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statusCodes") Collection<String> statusCodes
    );

    @Query(value = """
            SELECT CAST(DATE_TRUNC('day', created_at AT TIME ZONE :zone) AS DATE) AS sale_date,
                   COUNT(*) AS order_count,
                   SUM(total) AS revenue
            FROM orders
            WHERE created_at >= :from
                AND created_at < :to
                AND status_code IN (:statusCodes)
            GROUP BY sale_date
            ORDER BY sale_date
            """, nativeQuery = true)
    List<Object[]> sumDailySales(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statusCodes") Collection<String> statusCodes,
            @Param("zone") String zone
    );
}
