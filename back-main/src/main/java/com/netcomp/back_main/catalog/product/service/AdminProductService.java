package com.netcomp.back_main.catalog.product.service;

import com.google.common.collect.Lists;
import com.netcomp.back_main.catalog.brand.domain.Brand;
import com.netcomp.back_main.catalog.brand.repository.BrandRepository;
import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import com.netcomp.back_main.catalog.product.api.request.AdminProductSearchRequest;
import com.netcomp.back_main.catalog.product.api.request.AdminProductImageCreateRequest;
import com.netcomp.back_main.catalog.product.api.request.AdminProductCreateRequest;
import com.netcomp.back_main.catalog.product.api.response.AdminProductResponse;
import com.netcomp.back_main.catalog.product.api.response.AdminProductEmbeddingResponse;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.common.embedding.BedrockEmbeddingService;
import com.netcomp.back_main.common.util.SlugUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminProductService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductService.class);
    private static final String INPUT_TYPE = "search_document";
    private static final int EMBEDDING_DIMENSION = 1024;
    private static final int MAX_BATCH_SIZE = 20;

    // --- Dependencies ---
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BedrockEmbeddingService bedrockEmbeddingService;

    // --- Constructor ---
    public AdminProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            BedrockEmbeddingService bedrockEmbeddingService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.bedrockEmbeddingService = bedrockEmbeddingService;
    }

    // --- Public operations ---
    public PageResponse<AdminProductResponse> getProducts(AdminProductSearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.resolvedPage() - 1,
                request.resolvedPerPage()
        );
        String sortOrder = "asc".equalsIgnoreCase(request.order()) ? "asc" : "desc";
        String search = StringUtils.trimToEmpty(request.search());
        String categorySlug = StringUtils.isBlank(request.categorySlug())
                ? null
                : StringUtils.trim(request.categorySlug());
        Page<Product> page = productRepository.searchAdminProducts(search, categorySlug, sortOrder, pageable);
        return PageResponse.from(page, AdminProductResponse::from);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminProductEmbeddingResponse generateMissingEmbeddings() {
        List<Product> products = productRepository.findAllByEmbeddingIsNullOrderByCreatedAtAsc();
        int generatedCount = 0;

        for (List<Product> batch : Lists.partition(products, MAX_BATCH_SIZE)) {
            List<List<Float>> embeddings = requestEmbeddings(batch.stream()
                    .map(this::buildProductText)
                    .toList());

            if (embeddings.size() != batch.size()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "El servicio de embeddings devolvió una cantidad inesperada de resultados."
                );
            }

            for (int index = 0; index < batch.size(); index++) {
                batch.get(index).setEmbedding(toVector(embeddings.get(index)));
            }

            productRepository.saveAll(batch);
            generatedCount += batch.size();
        }

        return new AdminProductEmbeddingResponse(generatedCount);
    }

    @Transactional
    public AdminProductResponse createProduct(AdminProductCreateRequest request) {
        String name = request.name().trim();
        String requestedSlug = request.slug() == null ? "" : request.slug().trim();
        String slug = SlugUtils.generate(requestedSlug.isEmpty() ? name : requestedSlug);

        if (slug.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo generar un slug válido para el producto."
            );
        }
        if (productRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese slug."
            );
        }

        Category category = categoryRepository.findBySlugAndActiveTrue(request.categorySlug())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría seleccionada no existe o está inactiva."
                ));
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La marca seleccionada no existe."
                ));

        Map<String, Object> specifications = validateSpecifications(
                category,
                request.specifications()
        );
        List<AdminProductImageCreateRequest> requestedImages = request.images() == null
                ? List.of()
                : request.images();
        long primaryImageCount = requestedImages.stream()
                .filter(image -> Boolean.TRUE.equals(image.isPrimary()))
                .count();

        if (primaryImageCount > 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo una imagen puede ser la imagen principal."
            );
        }

        Product product = new Product(
                category,
                brand,
                name,
                request.price(),
                request.stock(),
                slug,
                request.active(),
                null,
                specifications
        );

        for (int index = 0; index < requestedImages.size(); index++) {
            AdminProductImageCreateRequest image = requestedImages.get(index);
            boolean isPrimary = primaryImageCount == 0
                    ? index == 0
                    : Boolean.TRUE.equals(image.isPrimary());
            product.addImage(new ProductImage(
                    validateImageUrl(image.imageUrl()),
                    isPrimary,
                    image.displayOrder()
            ));
        }

        Product savedProduct;
        try {
            savedProduct = productRepository.save(product);
        } catch (DataAccessException exception) {
            LOGGER.error("Failed to create product with slug={}", slug, exception);
            throw exception;
        }

        LOGGER.info("Product created. id={}, slug={}", savedProduct.getId(), slug);
        return AdminProductResponse.from(savedProduct);
    }

    // --- Private helpers ---
    private List<List<Float>> requestEmbeddings(List<String> productTexts) {
        try {
            return bedrockEmbeddingService.generateEmbeddings(productTexts, INPUT_TYPE);
        } catch (Exception exception) {
            LOGGER.error("Failed to generate product embeddings with Amazon Bedrock.", exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudieron generar los embeddings con Amazon Bedrock.",
                    exception
            );
        }
    }

    private float[] toVector(List<Float> values) {
        if (values.size() != EMBEDDING_DIMENSION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio de embeddings devolvió un vector con una dimensión inesperada."
            );
        }

        float[] vector = new float[EMBEDDING_DIMENSION];
        for (int index = 0; index < values.size(); index++) {
            vector[index] = values.get(index);
        }
        return vector;
    }

    private String buildProductText(Product product) {
        String brandName = product.getBrand() == null ? "" : product.getBrand().getName();
        String specifications = product.getSpecifications() == null
                ? ""
                : product.getSpecifications().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        String text = "Producto: " + product.getName()
                + ". Marca: " + brandName
                + ". Categoría: " + product.getCategory().getName()
                + ". Especificaciones: " + specifications;
        return truncateCharacters(text, 2000);
    }

    private String truncateCharacters(String text, int maxCharacters) {
        int characterCount = text.codePointCount(0, text.length());
        if (characterCount <= maxCharacters) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, maxCharacters));
    }

    private Map<String, Object> validateSpecifications(
            Category category,
            Map<String, Object> submittedSpecifications
    ) {
        List<CategorySpecificationDefinition> schema = category.getSpecificationSchema() == null
                ? List.of()
                : category.getSpecificationSchema();
        Map<String, Object> submitted = submittedSpecifications == null
                ? Map.of()
                : submittedSpecifications;
        Set<String> allowedKeys = new HashSet<>();
        for (CategorySpecificationDefinition definition : schema) {
            allowedKeys.add(definition.key());
        }

        for (String key : submitted.keySet()) {
            if (!allowedKeys.contains(key)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La especificación '" + key + "' no está permitida para esta categoría."
                );
            }
        }

        Map<String, Object> validated = new LinkedHashMap<>();
        for (CategorySpecificationDefinition definition : schema) {
            String key = definition.key();
            Object value = submitted.get(key);
            if (value == null) {
                if (definition.required()) {
                    throw requiredSpecification(definition);
                }
                continue;
            }
            if (value instanceof String text && text.isBlank()) {
                if (definition.required()) {
                    throw requiredSpecification(definition);
                }
                continue;
            }

            switch (definition.type()) {
                case "text" -> {
                    if (!(value instanceof String text)) {
                        throw invalidSpecificationType(definition);
                    }
                    validated.put(key, text.trim());
                }
                case "number" -> {
                    if (!(value instanceof Number)) {
                        throw invalidSpecificationType(definition);
                    }
                    validated.put(key, value);
                }
                case "boolean" -> {
                    if (!(value instanceof Boolean)) {
                        throw invalidSpecificationType(definition);
                    }
                    validated.put(key, value);
                }
                case "select" -> {
                    if (!(value instanceof String option)
                            || definition.options() == null
                            || !definition.options().contains(option)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "La opción seleccionada para '" + definition.label() + "' no es válida."
                        );
                    }
                    validated.put(key, option);
                }
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría tiene una configuración de especificaciones no válida."
                );
            }
        }

        return validated;
    }

    private static ResponseStatusException invalidSpecificationType(
            CategorySpecificationDefinition definition
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El valor de '" + definition.label() + "' no tiene el tipo esperado."
        );
    }

    private static ResponseStatusException requiredSpecification(
            CategorySpecificationDefinition definition
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La especificación '" + definition.label() + "' es obligatoria."
        );
    }

    private static String validateImageUrl(String imageUrl) {
        String normalizedUrl = imageUrl.trim();
        try {
            URI uri = URI.create(normalizedUrl);
            if (uri.getHost() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La URL de la imagen no es válida."
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La URL de la imagen no es válida."
            );
        }
        return normalizedUrl;
    }
}
