package com.netcomp.back_main.orders.order.api.controller;

import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.orders.order.api.request.AdminOrderSearchRequest;
import com.netcomp.back_main.orders.order.api.request.SalesSummaryRequest;
import com.netcomp.back_main.orders.order.api.request.UpdateOrderStatusRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.api.response.OrderStatusResponse;
import com.netcomp.back_main.orders.order.api.response.SalesSummaryResponse;
import com.netcomp.back_main.orders.order.service.AdminOrderService;
import com.netcomp.back_main.orders.order.service.SalesReportService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final AdminOrderService orderService;
    private final SalesReportService salesReportService;

    public AdminOrderController(AdminOrderService orderService, SalesReportService salesReportService) {
        this.orderService = orderService;
        this.salesReportService = salesReportService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> list(@Valid @ModelAttribute AdminOrderSearchRequest request) {
        return ResponseEntity.ok(orderService.getOrders(request));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<OrderStatusResponse>> statuses() {
        return ResponseEntity.ok(orderService.getStatuses());
    }

    @GetMapping("/summary")
    public ResponseEntity<SalesSummaryResponse> summary(@Valid @ModelAttribute SalesSummaryRequest request) {
        return ResponseEntity.ok(orderService.getSummary(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@Valid @ModelAttribute AdminOrderSearchRequest request) {
        byte[] report = salesReportService.exportOrders(request);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(salesReportService.buildFileName())
                .build();

        return ResponseEntity.ok()
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(report.length)
                .body(report);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> retrieve(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request));
    }
}
