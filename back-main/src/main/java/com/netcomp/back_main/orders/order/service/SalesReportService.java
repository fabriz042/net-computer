package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.orders.order.api.request.AdminOrderSearchRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderItem;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.domain.SalesPeriod;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SalesReportService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesReportService.class);
    private static final int ROW_WINDOW_SIZE = 200;
    private static final int MAX_REPORT_ORDERS = 20_000;
    private static final String CURRENCY_FORMAT = "\"S/\" #,##0.00";
    private static final String DATE_TIME_FORMAT = "dd/mm/yyyy hh:mm";
    private static final DateTimeFormatter LABEL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter LABEL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] ORDER_HEADERS = {
            "N° pedido", "Fecha", "Cliente", "Correo", "Estado", "Unidades", "Dirección de envío", "Total (S/)"
    };
    private static final int[] ORDER_COLUMN_WIDTHS = {14, 18, 28, 32, 14, 11, 48, 16};
    private static final String[] DETAIL_HEADERS = {
            "N° pedido", "Fecha", "Estado", "Producto", "Cantidad", "Precio unitario (S/)", "Subtotal (S/)"
    };
    private static final int[] DETAIL_COLUMN_WIDTHS = {14, 18, 14, 48, 11, 20, 16};

    // --- Dependencies ---
    private final OrderRepository orderRepository;

    // --- Constructor ---
    public SalesReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // --- Public operations ---
    public byte[] exportOrders(AdminOrderSearchRequest request) {
        SalesPeriod period = resolvePeriod(request.from(), request.to());
        String statusCode = StringUtils.upperCase(StringUtils.trimToNull(request.status()));
        List<Order> orders = orderRepository.findOrdersForReport(
                period.start(),
                period.end(),
                statusCode == null,
                statusCode == null ? "" : statusCode,
                StringUtils.trimToEmpty(request.search())
        );

        if (orders.size() > MAX_REPORT_ORDERS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El reporte supera los " + MAX_REPORT_ORDERS + " pedidos. Reduce el rango de fechas."
            );
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_WINDOW_SIZE);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ReportStyles styles = new ReportStyles(workbook);
            writeSummarySheet(workbook, styles, orders, period, statusCode);
            writeOrdersSheet(workbook, styles, orders);
            writeDetailSheet(workbook, styles, orders);
            workbook.write(output);

            LOGGER.info("Sales report generated. orders={}, status={}, from={}, to={}",
                    orders.size(), statusCode, period.from(), period.to());
            return output.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("Failed to generate sales report.", exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el reporte de ventas.",
                    exception
            );
        }
    }

    public String buildFileName() {
        LocalDate today = LocalDate.now(SalesPeriod.STORE_ZONE);
        return "reporte-ventas-" + today + ".xlsx";
    }

    // --- Private helpers ---
    private void writeSummarySheet(
            Workbook workbook,
            ReportStyles styles,
            List<Order> orders,
            SalesPeriod period,
            String statusCode
    ) {
        Sheet sheet = workbook.createSheet("Resumen");
        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 22 * 256);
        sheet.setColumnWidth(2, 22 * 256);

        Row titleRow = sheet.createRow(0);
        createTextCell(titleRow, 0, "Reporte de ventas - Net Computer", styles.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowIndex = 2;
        rowIndex = writeLabelValue(sheet, rowIndex, "Periodo", describePeriod(period), styles);
        rowIndex = writeLabelValue(sheet, rowIndex, "Estado filtrado",
                statusCode == null ? "Todos" : describeStatus(orders, statusCode), styles);
        rowIndex = writeLabelValue(sheet, rowIndex, "Generado el",
                LocalDateTime.now(SalesPeriod.STORE_ZONE).format(LABEL_DATE_TIME_FORMAT), styles);
        rowIndex++;

        long saleOrders = orders.stream().filter(this::countsAsSale).count();
        BigDecimal revenue = orders.stream()
                .filter(this::countsAsSale)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long unitsSold = orders.stream()
                .filter(this::countsAsSale)
                .flatMap(order -> order.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        Row kpiHeader = sheet.createRow(rowIndex++);
        createTextCell(kpiHeader, 0, "Indicador", styles.header);
        createTextCell(kpiHeader, 1, "Valor", styles.header);
        rowIndex = writeNumberRow(sheet, rowIndex, "Pedidos en el reporte", orders.size(), styles);
        rowIndex = writeNumberRow(sheet, rowIndex, "Ventas concretadas", saleOrders, styles);
        rowIndex = writeCurrencyRow(sheet, rowIndex, "Ingresos por ventas", revenue, styles);
        rowIndex = writeCurrencyRow(sheet, rowIndex, "Ticket promedio", saleOrders == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(saleOrders), 2, RoundingMode.HALF_UP), styles);
        rowIndex = writeNumberRow(sheet, rowIndex, "Unidades vendidas", unitsSold, styles);
        rowIndex++;

        Row statusHeader = sheet.createRow(rowIndex++);
        createTextCell(statusHeader, 0, "Estado", styles.header);
        createTextCell(statusHeader, 1, "Pedidos", styles.header);
        createTextCell(statusHeader, 2, "Monto (S/)", styles.header);

        Map<String, StatusAccumulator> totalsByStatus = new LinkedHashMap<>();
        for (OrderStatusCode code : OrderStatusCode.values()) {
            totalsByStatus.put(code.name(), new StatusAccumulator());
        }
        for (Order order : orders) {
            StatusAccumulator accumulator = totalsByStatus.computeIfAbsent(
                    order.getStatus().getCode(), key -> new StatusAccumulator());
            accumulator.name = order.getStatus().getName();
            accumulator.count++;
            accumulator.total = accumulator.total.add(order.getTotal());
        }
        for (Map.Entry<String, StatusAccumulator> entry : totalsByStatus.entrySet()) {
            StatusAccumulator accumulator = entry.getValue();
            if (accumulator.count == 0) {
                continue;
            }
            Row row = sheet.createRow(rowIndex++);
            createTextCell(row, 0, accumulator.name, styles.text);
            createNumberCell(row, 1, accumulator.count, styles.integer);
            createCurrencyCell(row, 2, accumulator.total, styles.currency);
        }
    }

    private void writeOrdersSheet(Workbook workbook, ReportStyles styles, List<Order> orders) {
        Sheet sheet = workbook.createSheet("Ventas");
        writeHeader(sheet, styles, ORDER_HEADERS, ORDER_COLUMN_WIDTHS);

        int rowIndex = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowIndex++);
            createTextCell(row, 0, OrderResponse.toCode(order.getId()), styles.text);
            createDateCell(row, 1, order.getCreatedAt(), styles.dateTime);
            createTextCell(row, 2, order.getUser().getName(), styles.text);
            createTextCell(row, 3, order.getUser().getEmail(), styles.text);
            createTextCell(row, 4, order.getStatus().getName(), styles.text);
            createNumberCell(row, 5, order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(), styles.integer);
            createTextCell(row, 6, order.getShippingAddress(), styles.text);
            createCurrencyCell(row, 7, order.getTotal(), styles.currency);
        }

        if (!orders.isEmpty()) {
            Row totalRow = sheet.createRow(rowIndex);
            createTextCell(totalRow, 6, "Total", styles.totalLabel);
            Cell totalCell = totalRow.createCell(7);
            totalCell.setCellFormula("SUM(H2:H" + rowIndex + ")");
            totalCell.setCellStyle(styles.totalCurrency);
            sheet.setAutoFilter(new CellRangeAddress(0, rowIndex - 1, 0, ORDER_HEADERS.length - 1));
        }
    }

    private void writeDetailSheet(Workbook workbook, ReportStyles styles, List<Order> orders) {
        Sheet sheet = workbook.createSheet("Detalle");
        writeHeader(sheet, styles, DETAIL_HEADERS, DETAIL_COLUMN_WIDTHS);

        int rowIndex = 1;
        for (Order order : orders) {
            List<OrderItem> items = order.getItems().stream()
                    .sorted(Comparator.comparing(OrderItem::getProductName))
                    .toList();
            for (OrderItem item : items) {
                Row row = sheet.createRow(rowIndex++);
                createTextCell(row, 0, OrderResponse.toCode(order.getId()), styles.text);
                createDateCell(row, 1, order.getCreatedAt(), styles.dateTime);
                createTextCell(row, 2, order.getStatus().getName(), styles.text);
                createTextCell(row, 3, item.getProductName(), styles.text);
                createNumberCell(row, 4, item.getQuantity(), styles.integer);
                createCurrencyCell(row, 5, item.getUnitPrice(), styles.currency);
                createCurrencyCell(row, 6, item.getSubtotal(), styles.currency);
            }
        }

        if (rowIndex > 1) {
            sheet.setAutoFilter(new CellRangeAddress(0, rowIndex - 1, 0, DETAIL_HEADERS.length - 1));
        }
    }

    private void writeHeader(Sheet sheet, ReportStyles styles, String[] headers, int[] widths) {
        Row header = sheet.createRow(0);
        for (int index = 0; index < headers.length; index++) {
            createTextCell(header, index, headers[index], styles.header);
            sheet.setColumnWidth(index, widths[index] * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    private int writeLabelValue(Sheet sheet, int rowIndex, String label, String value, ReportStyles styles) {
        Row row = sheet.createRow(rowIndex);
        createTextCell(row, 0, label, styles.label);
        createTextCell(row, 1, value, styles.plain);
        return rowIndex + 1;
    }

    private int writeNumberRow(Sheet sheet, int rowIndex, String label, long value, ReportStyles styles) {
        Row row = sheet.createRow(rowIndex);
        createTextCell(row, 0, label, styles.text);
        createNumberCell(row, 1, value, styles.integer);
        return rowIndex + 1;
    }

    private int writeCurrencyRow(Sheet sheet, int rowIndex, String label, BigDecimal value, ReportStyles styles) {
        Row row = sheet.createRow(rowIndex);
        createTextCell(row, 0, label, styles.text);
        createCurrencyCell(row, 1, value, styles.currency);
        return rowIndex + 1;
    }

    private void createTextCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createNumberCell(Row row, int column, long value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCurrencyCell(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? 0 : value.doubleValue());
        cell.setCellStyle(style);
    }

    private void createDateCell(Row row, int column, Instant value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(LocalDateTime.ofInstant(value, SalesPeriod.STORE_ZONE));
        }
        cell.setCellStyle(style);
    }

    private boolean countsAsSale(Order order) {
        return OrderStatusCode.from(order.getStatus().getCode()).countsAsSale();
    }

    private String describePeriod(SalesPeriod period) {
        if (period.isOpen()) {
            return "Todo el historial";
        }
        String from = period.from() == null ? "inicio" : period.from().format(LABEL_DATE_FORMAT);
        String to = period.to() == null ? "hoy" : period.to().format(LABEL_DATE_FORMAT);
        return from + " - " + to;
    }

    private String describeStatus(List<Order> orders, String statusCode) {
        return orders.stream()
                .filter(order -> order.getStatus().getCode().equals(statusCode))
                .map(order -> order.getStatus().getName())
                .findFirst()
                .orElse(statusCode);
    }

    private SalesPeriod resolvePeriod(LocalDate from, LocalDate to) {
        try {
            return SalesPeriod.of(from, to);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }

    private static final class StatusAccumulator {
        private String name = "";
        private long count;
        private BigDecimal total = BigDecimal.ZERO;
    }

    private static final class ReportStyles {
        private final CellStyle title;
        private final CellStyle header;
        private final CellStyle label;
        private final CellStyle plain;
        private final CellStyle text;
        private final CellStyle integer;
        private final CellStyle currency;
        private final CellStyle dateTime;
        private final CellStyle totalLabel;
        private final CellStyle totalCurrency;

        private ReportStyles(Workbook workbook) {
            short currencyFormat = workbook.createDataFormat().getFormat(CURRENCY_FORMAT);
            short dateTimeFormat = workbook.createDataFormat().getFormat(DATE_TIME_FORMAT);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            title = workbook.createCellStyle();
            title.setFont(titleFont);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header = bordered(workbook);
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            label = workbook.createCellStyle();
            label.setFont(boldFont);

            plain = workbook.createCellStyle();

            text = bordered(workbook);

            integer = bordered(workbook);
            integer.setAlignment(HorizontalAlignment.CENTER);

            currency = bordered(workbook);
            currency.setDataFormat(currencyFormat);

            dateTime = bordered(workbook);
            dateTime.setDataFormat(dateTimeFormat);
            dateTime.setAlignment(HorizontalAlignment.CENTER);

            totalLabel = bordered(workbook);
            totalLabel.setFont(boldFont);
            totalLabel.setAlignment(HorizontalAlignment.RIGHT);

            totalCurrency = bordered(workbook);
            totalCurrency.setFont(boldFont);
            totalCurrency.setDataFormat(currencyFormat);
        }

        private static CellStyle bordered(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            return style;
        }
    }
}
