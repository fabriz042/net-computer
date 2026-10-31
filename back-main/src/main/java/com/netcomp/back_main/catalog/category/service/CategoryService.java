package com.netcomp.back_main.catalog.category.service;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.netcomp.back_main.catalog.category.api.request.CreateCategoryRequest;
import com.netcomp.back_main.catalog.category.api.request.UpdateCategoryRequest;
import com.netcomp.back_main.catalog.category.api.response.CategoryResponse;
import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import com.netcomp.back_main.common.util.SlugUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    // --- Constants ---
    private static final int MAX_SLUG_LENGTH = 100;
    private static final int MAX_SPECIFICATION_LABEL_LENGTH = 100;
    private static final Set<String> SPECIFICATION_TYPES = ImmutableSet.of("text", "number", "boolean", "select");
    private static final CharMatcher SPECIFICATION_KEY_CHARS = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.is('_'));

    // --- Dependencies ---
    private final CategoryRepository categoryRepository;

    // --- Constructor ---
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // --- Public operations ---
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = normalizeName(request.name());
        validateName(name);

        String slug = resolveSlug(request.slug(), name);
        if (categoryRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese slug.");
        }
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }

        Category category = new Category(slug, name, request.active());
        List<CategorySpecificationDefinition> specificationSchema =
                normalizeSpecificationSchema(request.specificationSchema());
        if (specificationSchema != null) {
            category.setSpecificationSchema(specificationSchema);
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = findCategory(id);

        String name = normalizeName(request.name());
        validateName(name);

        // A blank slug keeps the current one, so renaming a category does not break its public URL.
        String slug = StringUtils.isBlank(request.slug())
                ? category.getSlug()
                : resolveSlug(request.slug(), name);
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese slug.");
        }
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }

        category.setName(name);
        category.setSlug(slug);
        category.setActive(request.active());

        // A null schema keeps the current specifications.
        List<CategorySpecificationDefinition> specificationSchema =
                normalizeSpecificationSchema(request.specificationSchema());
        if (specificationSchema != null) {
            category.setSpecificationSchema(specificationSchema);
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = findCategory(id);

        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar la categoría porque tiene " + productCount
                            + (productCount == 1 ? " producto asociado" : " productos asociados")
                            + ". Desactívala en su lugar."
            );
        }

        categoryRepository.delete(category);
    }

    // --- Private helpers ---
    private Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La categoría no existe."
                ));
    }

    private String normalizeName(String name) {
        return StringUtils.defaultString(StringUtils.normalizeSpace(name));
    }

    private void validateName(String name) {
        if (StringUtils.isBlank(name)) {
            throw badRequest("El nombre de la categoría es obligatorio.");
        }
        if (CharMatcher.javaLetterOrDigit().matchesNoneOf(name)) {
            throw badRequest("El nombre de la categoría debe contener letras o números.");
        }
    }

    private String resolveSlug(String requestedSlug, String name) {
        String source = StringUtils.isBlank(requestedSlug) ? name : requestedSlug;
        String slug = SlugUtils.generate(source);

        if (StringUtils.isBlank(slug)) {
            throw badRequest("No se pudo generar un slug válido para la categoría.");
        }
        if (slug.length() > MAX_SLUG_LENGTH) {
            throw badRequest("El slug no puede superar " + MAX_SLUG_LENGTH + " caracteres.");
        }

        return slug;
    }

    private List<CategorySpecificationDefinition> normalizeSpecificationSchema(
            List<CategorySpecificationDefinition> specificationSchema
    ) {
        if (specificationSchema == null) {
            return null;
        }

        Set<String> usedKeys = new HashSet<>();
        List<CategorySpecificationDefinition> normalized = new ArrayList<>();

        for (CategorySpecificationDefinition definition : specificationSchema) {
            if (definition == null) {
                throw badRequest("Hay una especificación vacía en el esquema de la categoría.");
            }

            String key = StringUtils.trimToEmpty(definition.key());
            String label = StringUtils.defaultString(StringUtils.normalizeSpace(definition.label()));
            String type = StringUtils.lowerCase(StringUtils.trimToEmpty(definition.type()));

            if (key.isEmpty() || !SPECIFICATION_KEY_CHARS.matchesAllOf(key)) {
                throw badRequest("La clave de especificación “" + key
                        + "” solo admite minúsculas, números y guion bajo.");
            }
            if (!usedKeys.add(key)) {
                throw badRequest("La clave de especificación “" + key + "” está repetida.");
            }
            if (label.isEmpty() || label.length() > MAX_SPECIFICATION_LABEL_LENGTH) {
                throw badRequest("La etiqueta de “" + key + "” es obligatoria y admite hasta "
                        + MAX_SPECIFICATION_LABEL_LENGTH + " caracteres.");
            }
            if (!SPECIFICATION_TYPES.contains(type)) {
                throw badRequest("El tipo de “" + key + "” debe ser texto, número, booleano o selección.");
            }

            List<String> options = List.of();
            if ("select".equals(type)) {
                options = definition.options().stream()
                        .map(StringUtils::trimToEmpty)
                        .filter(StringUtils::isNotEmpty)
                        .distinct()
                        .toList();
                if (options.isEmpty()) {
                    throw badRequest("La especificación “" + label + "” necesita al menos una opción.");
                }
            }

            normalized.add(new CategorySpecificationDefinition(
                    key,
                    label,
                    type,
                    definition.required(),
                    StringUtils.trimToNull(definition.unit()),
                    options
            ));
        }

        return normalized;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
