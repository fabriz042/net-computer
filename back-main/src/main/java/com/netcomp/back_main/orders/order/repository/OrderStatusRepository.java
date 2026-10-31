package com.netcomp.back_main.orders.order.repository;

import com.netcomp.back_main.orders.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, String> {
}
