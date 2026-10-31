package com.netcomp.back_main.catalog.product.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminProductReportService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductReportService.class);
    private static final int ROW_WINDOW_SIZE = 100;
    private static final ZoneId STORE_ZONE = ZoneId.of("America/Lima");
    private static final String[] HEADERS = {
            "UUID", "Nombre", "Slug", "Marca", "Categoría", "Precio (S/.)", "Stock", "Estado", "Embedding", "Fecha de registro"
    };
    private static final int[] COLUMN_WIDTHS = {38, 36, 32, 24, 24, 16, 12, 14, 16, 22};

    // --- Dependencies ---
    private final ProductRepository productRepository;

    // --- Constructor ---
    public AdminProductReportService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // --- Public operations ---
    public byte[] exportProducts() {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_WINDOW_SIZE);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");
            ProductReportStyles styles = new ProductReportStyles(workbook);
            writeHeader(sheet, styles);

            int rowIndex = 1;
            for (Product product : products) {
                writeProductRow(sheet.createRow(rowIndex++), product, styles);
            }

            sheet.createFreezePane(0, 1);
            if (!products.isEmpty()) {
                sheet.setAutoFilter(new CellRangeAddress(0, rowIndex - 1, 0, HEADERS.length - 1));
            }

            workbook.write(output);
            LOGGER.info("Admin product report generated. products={}", products.size());
            return output.toByteArray();
        } catch (IOException exception) {
            LOGGER.error("Failed to generate admin product report.", exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el reporte de productos.",
                    exception
            );
        }
    }

    public String buildFileName() {
        return "reporte-productos-" + LocalDate.now(STORE_ZONE) + ".xlsx";
    }

    // --- Private helpers ---
    private void writeHeader(Sheet sheet, ProductReportStyles styles) {
        Row header = sheet.createRow(0);
        for (int index = 0; index < HEADERS.length; index++) {
            createTextCell(header, index, HEADERS[index], styles.header);
            sheet.setColumnWidth(index, COLUMN_WIDTHS[index] * 256);
        }
    }

    private void writeProductRow(Row row, Product product, ProductReportStyles styles) {
        createTextCell(row, 0, product.getId().toString(), styles.text);
        createTextCell(row, 1, product.getName(), styles.text);
        createTextCell(row, 2, product.getSlug(), styles.text);
        createTextCell(row, 3, product.getBrand() == null ? "" : product.getBrand().getName(), styles.text);
        createTextCell(row, 4, product.getCategory().getName(), styles.text);
        createCurrencyCell(row, 5, product.getPrice().doubleValue(), styles.currency);
        createNumberCell(row, 6, product.getStock(), styles.integer);
        createTextCell(row, 7, Boolean.TRUE.equals(product.getActive()) ? "Activo" : "Inactivo", styles.text);
        createTextCell(row, 8, product.getEmbedding() == null ? "Pendiente" : "Generado", styles.text);
        createDateCell(row, 9, product.getCreatedAt(), styles.dateTime);
    }

    private void createTextCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createNumberCell(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCurrencyCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDateCell(Row row, int column, java.time.Instant value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(LocalDateTime.ofInstant(value, STORE_ZONE));
        }
        cell.setCellStyle(style);
    }

    private static final class ProductReportStyles {
        private final CellStyle header;
        private final CellStyle text;
        private final CellStyle integer;
        private final CellStyle currency;
        private final CellStyle dateTime;

        private ProductReportStyles(Workbook workbook) {
            DataFormat dataFormat = workbook.createDataFormat();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header = bordered(workbook);
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);

            text = bordered(workbook);

            integer = bordered(workbook);
            integer.setAlignment(HorizontalAlignment.CENTER);
            integer.setDataFormat(dataFormat.getFormat("#,##0"));

            currency = bordered(workbook);
            currency.setDataFormat(dataFormat.getFormat("\"S/. \"#,##0.00"));

            dateTime = bordered(workbook);
            dateTime.setDataFormat(dataFormat.getFormat("dd/mm/yyyy hh:mm"));
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
