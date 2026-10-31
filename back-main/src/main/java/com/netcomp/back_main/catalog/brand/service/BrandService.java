package com.netcomp.back_main.catalog.brand.service;

import com.google.common.base.CharMatcher;
import com.netcomp.back_main.catalog.brand.api.request.CreateBrandRequest;
import com.netcomp.back_main.catalog.brand.api.request.UpdateBrandRequest;
import com.netcomp.back_main.catalog.brand.api.response.BrandResponse;
import com.netcomp.back_main.catalog.brand.domain.Brand;
import com.netcomp.back_main.catalog.brand.repository.BrandRepository;
import com.netcomp.back_main.common.util.SlugUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BrandService {

    // --- Constants ---
    private static final int MAX_SLUG_LENGTH = 100;

    // --- Dependencies ---
    private final BrandRepository brandRepository;

    // --- Constructor ---
    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    // --- Public operations ---
    public List<BrandResponse> getBrands() {
        return brandRepository.findAllByOrderByNameAsc()
                .stream()
                .map(BrandResponse::from)
                .toList();
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String name = normalizeName(request.name());
        validateName(name);

        String slug = resolveSlug(request.slug(), name);
        if (brandRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una marca con ese slug.");
        }
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una marca con ese nombre.");
        }

        return BrandResponse.from(brandRepository.save(new Brand(slug, name)));
    }

    @Transactional
    public BrandResponse updateBrand(UUID id, UpdateBrandRequest request) {
        Brand brand = findBrand(id);

        String name = normalizeName(request.name());
        validateName(name);

        // A blank slug keeps the current one, so renaming a brand does not change its identifier.
        String slug = StringUtils.isBlank(request.slug())
                ? brand.getSlug()
                : resolveSlug(request.slug(), name);
        if (brandRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una marca con ese slug.");
        }
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una marca con ese nombre.");
        }

        brand.setName(name);
        brand.setSlug(slug);

        return BrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(UUID id) {
        Brand brand = findBrand(id);

        long productCount = brandRepository.countProductsByBrandId(id);
        if (productCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar la marca porque tiene " + productCount
                            + (productCount == 1 ? " producto asociado." : " productos asociados.")
            );
        }

        brandRepository.delete(brand);
    }

    // --- Private helpers ---
    private Brand findBrand(UUID id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La marca no existe."
                ));
    }

    private String normalizeName(String name) {
        return StringUtils.defaultString(StringUtils.normalizeSpace(name));
    }

    private void validateName(String name) {
        if (StringUtils.isBlank(name)) {
            throw badRequest("El nombre de la marca es obligatorio.");
        }
        if (CharMatcher.javaLetterOrDigit().matchesNoneOf(name)) {
            throw badRequest("El nombre de la marca debe contener letras o números.");
        }
    }

    private String resolveSlug(String requestedSlug, String name) {
        String source = StringUtils.isBlank(requestedSlug) ? name : requestedSlug;
        String slug = SlugUtils.generate(source);

        if (StringUtils.isBlank(slug)) {
            throw badRequest("No se pudo generar un slug válido para la marca.");
        }
        if (slug.length() > MAX_SLUG_LENGTH) {
            throw badRequest("El slug no puede superar " + MAX_SLUG_LENGTH + " caracteres.");
        }

        return slug;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
