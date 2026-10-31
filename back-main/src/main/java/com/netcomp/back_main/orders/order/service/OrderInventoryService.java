package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.catalog.inventory.domain.MovementType;
import com.netcomp.back_main.catalog.inventory.domain.StockMovement;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class OrderInventoryService {

    // --- Constants ---
    private static final String SALE_REASON = "Venta - pedido #";
    private static final String CANCELLATION_REASON = "Cancelación - pedido #";

    // --- Dependencies ---
    private final EntityManager entityManager;

    // --- Constructor ---
    public OrderInventoryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // --- Public operations ---
    public Product lockProduct(Product product) {
        entityManager.refresh(product, LockModeType.PESSIMISTIC_WRITE);
        return product;
    }

    public void registerSale(Order order) {
        for (OrderItem item : sortedItems(order)) {
            Product product = item.getProduct();
            int stockBefore = product.getStock();
            int stockAfter = stockBefore - item.getQuantity();
            product.setStock(stockAfter);
            entityManager.persist(new StockMovement(
                    product,
                    MovementType.EXIT,
                    item.getQuantity(),
                    stockBefore,
                    stockAfter,
                    SALE_REASON + OrderResponse.toCode(order.getId()),
                    order.getId().toString()
            ));
        }
    }

    public void restoreStock(Order order) {
        for (OrderItem item : sortedItems(order)) {
            Product product = lockProduct(item.getProduct());
            int stockBefore = product.getStock();
            int stockAfter = stockBefore + item.getQuantity();
            product.setStock(stockAfter);
            entityManager.persist(new StockMovement(
                    product,
                    MovementType.ENTRY,
                    item.getQuantity(),
                    stockBefore,
                    stockAfter,
                    CANCELLATION_REASON + OrderResponse.toCode(order.getId()),
                    order.getId().toString()
            ));
        }
    }

    // --- Private helpers ---
    private List<OrderItem> sortedItems(Order order) {
        return order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
    }
}
