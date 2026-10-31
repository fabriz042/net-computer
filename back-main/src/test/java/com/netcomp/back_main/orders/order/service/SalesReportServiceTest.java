package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.orders.order.api.request.AdminOrderSearchRequest;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.support.TestFixtures;
import com.netcomp.back_main.users.user.domain.User;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private SalesReportService salesReportService;

    @BeforeEach
    void setUp() {
        salesReportService = new SalesReportService(orderRepository);
    }

    @Test
    void generatesWorkbookWithSummaryOrdersAndDetailSheets() throws IOException {
        User user = TestFixtures.user();
        Order paidOrder = TestFixtures.order(user, OrderStatusCode.PAID,
                TestFixtures.product("Monitor LG", "899.90", 5), 2);
        Order cancelledOrder = TestFixtures.order(user, OrderStatusCode.CANCELLED,
                TestFixtures.product("Mouse", "50.00", 5), 1);
        when(orderRepository.findOrdersForReport(any(), any(), eq(true), eq(""), eq("")))
                .thenReturn(List.of(paidOrder, cancelledOrder));

        byte[] report = salesReportService.exportOrders(request(null, null, null));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(report))) {
            assertEquals(3, workbook.getNumberOfSheets());
            assertEquals("Resumen", workbook.getSheetName(0));
            assertEquals("Ventas", workbook.getSheetName(1));
            assertEquals("Detalle", workbook.getSheetName(2));

            Sheet orders = workbook.getSheet("Ventas");
            assertEquals("N° pedido", orders.getRow(0).getCell(0).getStringCellValue());
            assertEquals(paidOrder.getId().toString().substring(0, 8).toUpperCase(),
                    orders.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Ana Torres", orders.getRow(1).getCell(2).getStringCellValue());
            assertEquals("Pagado", orders.getRow(1).getCell(4).getStringCellValue());
            assertEquals(1799.80, orders.getRow(1).getCell(7).getNumericCellValue(), 0.001);
            assertEquals("SUM(H2:H3)", orders.getRow(3).getCell(7).getCellFormula());

            Sheet detail = workbook.getSheet("Detalle");
            assertEquals(2, detail.getLastRowNum());
            assertEquals("Monitor LG", detail.getRow(1).getCell(3).getStringCellValue());

            Sheet summary = workbook.getSheet("Resumen");
            assertEquals("Todo el historial", findValue(summary, "Periodo"));
            assertEquals(1799.80, findNumber(summary, "Ingresos por ventas"), 0.001);
            assertEquals(1, findNumber(summary, "Ventas concretadas"), 0.001);
        }
    }

    @Test
    void generatesValidWorkbookWhenThereAreNoOrders() throws IOException {
        when(orderRepository.findOrdersForReport(any(), any(), eq(false), eq("PAID"), anyString()))
                .thenReturn(List.of());

        byte[] report = salesReportService.exportOrders(request("paid", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30)));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(report))) {
            assertEquals(0, workbook.getSheet("Ventas").getLastRowNum());
            assertEquals("01/10/2026 - 30/10/2026", findValue(workbook.getSheet("Resumen"), "Periodo"));
        }
    }

    @Test
    void rejectsInvertedDateRange() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> salesReportService.exportOrders(request(null, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 1))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void buildsExcelFileName() {
        String fileName = salesReportService.buildFileName();

        assertTrue(fileName.startsWith("reporte-ventas-"));
        assertTrue(fileName.endsWith(".xlsx"));
    }

    private static AdminOrderSearchRequest request(String status, LocalDate from, LocalDate to) {
        return new AdminOrderSearchRequest(null, null, null, null, status, from, to);
    }

    private static String findValue(Sheet sheet, String label) {
        return findRow(sheet, label).getCell(1).getStringCellValue();
    }

    private static double findNumber(Sheet sheet, String label) {
        return findRow(sheet, label).getCell(1).getNumericCellValue();
    }

    private static Row findRow(Sheet sheet, String label) {
        for (Row row : sheet) {
            if (row.getCell(0) != null && label.equals(row.getCell(0).getStringCellValue())) {
                return row;
            }
        }
        throw new AssertionError("Row not found: " + label);
    }
}
