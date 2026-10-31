package com.netcomp.back_main.orders.order.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesSummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        long completedOrders,
        BigDecimal averageTicket,
        long unitsSold,
        long pendingOrders,
        BigDecimal pendingAmount,
        long cancelledOrders,
        List<StatusTotal> statusBreakdown,
        List<DailySale> dailySales,
        List<TopProduct> topProducts
) {
    public record StatusTotal(String code, String name, long orderCount, BigDecimal total) {
    }

    public record DailySale(LocalDate date, long orderCount, BigDecimal revenue) {
    }

    public record TopProduct(UUID productId, String productName, long unitsSold, BigDecimal revenue) {
    }
}
