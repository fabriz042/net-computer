"use client";

import { isAxiosError } from "axios";
import { useEffect, useRef, useState } from "react";
import { FiPlus, FiTrash2 } from "react-icons/fi";
import Button from "@/app/components/ui/button";
import CheckboxField from "@/app/components/ui/checkbox-field";
import InputField from "@/app/components/ui/input-field";
import SelectField, {
  type SelectFieldOption,
} from "@/app/components/ui/select-field";
import { listAdminBrands } from "@/app/modules/catalog/brand/services/admin-brand.service";
import type { AdminBrand } from "@/app/modules/catalog/brand/types/admin-brand.type";
import { listAdminCategories } from "@/app/modules/catalog/category/services/admin-category.service";
import { createAdminProduct } from "@/app/modules/catalog/product/services/admin-product.service";
import type { AdminProductCreateRequest } from "@/app/modules/catalog/product/types/admin-product.type";
import type { AdminCategory } from "@/app/modules/catalog/category/types/admin-category.type";

interface ProductFormProps {
  onCancel: () => void;
  onCreated: () => void;
}

interface ProductImageDraft {
  id: number;
  imageUrl: string;
  displayOrder: string;
  isPrimary: boolean;
}

type SpecificationInputValue = string | boolean;

export default function ProductForm({ onCancel, onCreated }: ProductFormProps) {
  const [brands, setBrands] = useState<AdminBrand[]>([]);
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [selectedCategorySlug, setSelectedCategorySlug] = useState("");
  const [selectedBrandId, setSelectedBrandId] = useState("");
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("0");
  const [active, setActive] = useState(true);
  const [images, setImages] = useState<ProductImageDraft[]>([
    { id: 0, imageUrl: "", displayOrder: "0", isPrimary: true },
  ]);
  const [specifications, setSpecifications] = useState<
    Record<string, SpecificationInputValue>
  >({});
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [hasCategoryError, setHasCategoryError] = useState(false);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);
  const [hasBrandError, setHasBrandError] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const nextImageIdRef = useRef(1);

  useEffect(() => {
    let isCurrent = true;

    void listAdminCategories()
      .then((result) => {
        if (isCurrent) setCategories(result);
      })
      .catch(() => {
        if (isCurrent) setHasCategoryError(true);
      })
      .finally(() => {
        if (isCurrent) setIsLoadingCategories(false);
      });

    void listAdminBrands()
      .then((result) => {
        if (isCurrent) setBrands(result);
      })
      .catch(() => {
        if (isCurrent) setHasBrandError(true);
      })
      .finally(() => {
        if (isCurrent) setIsLoadingBrands(false);
      });

    return () => {
      isCurrent = false;
    };
  }, []);

  const productCategories: SelectFieldOption[] = categories.map((category) => ({
    value: category.slug,
    label: category.name,
  }));
  const productBrands: SelectFieldOption[] = brands.map((brand) => ({
    value: brand.id,
    label: brand.name,
  }));
  const selectedBrand = brands.find((brand) => brand.id === selectedBrandId);
  const selectedCategory = categories.find(
    (category) => category.slug === selectedCategorySlug
  );
  const specificationSchema = selectedCategory?.specificationSchema ?? [];

  const addImage = () => {
    const id = nextImageIdRef.current++;
    setImages((current) => [
      ...current,
      {
        id,
        imageUrl: "",
        displayOrder: String(current.length),
        isPrimary: current.length === 0,
      },
    ]);
  };

  const updateImage = (
    imageId: number,
    updates: Partial<Omit<ProductImageDraft, "id">>
  ) => {
    setImages((current) =>
      current.map((image) =>
        image.id === imageId ? { ...image, ...updates } : image
      )
    );
  };

  const removeImage = (imageId: number) => {
    setImages((current) => {
      const remaining = current.filter((image) => image.id !== imageId);
      if (remaining.length > 0 && !remaining.some((image) => image.isPrimary)) {
        return remaining.map((image, index) =>
          index === 0 ? { ...image, isPrimary: true } : image
        );
      }
      return remaining;
    });
  };

  const updateSpecification = (
    key: string,
    value: SpecificationInputValue
  ) => {
    setSpecifications((current) => ({ ...current, [key]: value }));
  };

  const handleSave = async () => {
    setSubmitError("");

    if (!name.trim()) {
      setSubmitError("El nombre del producto es obligatorio.");
      return;
    }
    if (!selectedCategory) {
      setSubmitError("Selecciona una categoría activa.");
      return;
    }
    if (!selectedBrand) {
      setSubmitError("Selecciona una marca.");
      return;
    }

    const productPrice = Number(price);
    if (price.trim() === "" || !Number.isFinite(productPrice) || productPrice < 0) {
      setSubmitError("Ingresa un precio válido mayor o igual a cero.");
      return;
    }

    const productStock = Number(stock);
    if (!Number.isInteger(productStock) || productStock < 0) {
      setSubmitError("El stock debe ser un número entero mayor o igual a cero.");
      return;
    }

    const productSpecifications: AdminProductCreateRequest["specifications"] = {};
    for (const definition of specificationSchema) {
      const value = specifications[definition.key];
      if (definition.type === "boolean") {
        productSpecifications[definition.key] = value === true;
        continue;
      }

      const textValue = typeof value === "string" ? value.trim() : "";
      if (!textValue) {
        if (definition.required) {
          setSubmitError(`Completa la especificación “${definition.label}”.`);
          return;
        }
        continue;
      }

      if (definition.type === "number") {
        const numericValue = Number(textValue);
        if (!Number.isFinite(numericValue)) {
          setSubmitError(`Ingresa un número válido para “${definition.label}”.`);
          return;
        }
        productSpecifications[definition.key] = numericValue;
        continue;
      }

      if (
        definition.type === "select" &&
        !(definition.options ?? []).includes(textValue)
      ) {
        setSubmitError(`Selecciona una opción válida para “${definition.label}”.`);
        return;
      }

      productSpecifications[definition.key] = textValue;
    }

    const productImages: AdminProductCreateRequest["images"] = [];
    for (const image of images) {
      const imageUrl = image.imageUrl.trim();
      if (!imageUrl) continue;

      let parsedUrl: URL;
      try {
        parsedUrl = new URL(imageUrl);
      } catch {
        setSubmitError("Ingresa una URL válida para cada imagen.");
        return;
      }
      if (parsedUrl.protocol !== "http:" && parsedUrl.protocol !== "https:") {
        setSubmitError("Las imágenes deben usar una URL HTTP o HTTPS.");
        return;
      }

      const displayOrder = Number(image.displayOrder);
      if (!Number.isInteger(displayOrder) || displayOrder < 0) {
        setSubmitError("El orden de cada imagen debe ser un entero mayor o igual a cero.");
        return;
      }

      productImages.push({
        imageUrl,
        displayOrder,
        isPrimary: image.isPrimary,
      });
    }

    if (productImages.length > 0 && !productImages.some((image) => image.isPrimary)) {
      productImages[0].isPrimary = true;
    }

    const request: AdminProductCreateRequest = {
      name: name.trim(),
      slug: slug.trim() || undefined,
      price: productPrice,
      stock: productStock,
      active,
      categorySlug: selectedCategory.slug,
      brandId: selectedBrand.id,
      images: productImages,
      specifications: productSpecifications,
    };

    setIsSaving(true);
    let wasCreated = false;
    try {
      await createAdminProduct(request);
      wasCreated = true;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 409) {
        setSubmitError("Ya existe un producto con ese slug. Cambia el nombre o el slug.");
      } else {
        const responseData = isAxiosError(error)
          ? error.response?.data as {
              detail?: unknown;
              message?: unknown;
              errors?: Record<string, string>;
            } | undefined
          : undefined;
        const validationMessage = responseData?.errors
          ? Object.values(responseData.errors).join(" ")
          : undefined;
        const serverMessage = responseData?.detail ?? responseData?.message;

        setSubmitError(
          validationMessage ??
            (typeof serverMessage === "string"
              ? serverMessage
              : "No se pudo guardar el producto. Revisa los datos e inténtalo de nuevo.")
        );
      }
    } finally {
      setIsSaving(false);
      if (wasCreated) onCreated();
    }
  };

  return (
    <div className="mt-5 space-y-5">
      <section>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <InputField
            id="product-name"
            label="Nombre"
            placeholder="Nombre del producto"
            maxLength={150}
            required
            value={name}
            onValueChange={setName}
          />
          <InputField
            id="product-slug"
            label="Slug"
            placeholder="Se genera a partir del nombre"
            maxLength={150}
            value={slug}
            onValueChange={setSlug}
          />
          <SelectField
            id="product-category"
            label="Categoría"
            options={productCategories}
            placeholder={
              isLoadingCategories
                ? "Cargando categorías..."
                : hasCategoryError
                  ? "Categorías no disponibles"
                  : categories.length === 0
                    ? "No hay categorías activas"
                    : "Selecciona una categoría"
            }
            required
            disabled={isLoadingCategories || hasCategoryError || categories.length === 0}
            value={selectedCategorySlug}
            onChange={(value) => {
              setSelectedCategorySlug(value);
              setSpecifications({});
            }}
          />
          <SelectField
            id="product-brand"
            label="Marca"
            options={productBrands}
            placeholder={
              isLoadingBrands
                ? "Cargando marcas..."
                : hasBrandError
                  ? "Marcas no disponibles"
                  : brands.length === 0
                    ? "No hay marcas disponibles"
                    : "Selecciona una marca"
            }
            required
            disabled={isLoadingBrands || hasBrandError || brands.length === 0}
            value={selectedBrandId}
            onChange={setSelectedBrandId}
          />
          <InputField
            id="product-price"
            label="Precio (S/.)"
            type="number"
            placeholder="0.00"
            min={0}
            step={0.01}
            required
            value={price}
            onValueChange={setPrice}
          />
          <InputField
            id="product-stock"
            label="Stock"
            type="number"
            placeholder="0"
            min={0}
            step={1}
            required
            value={stock}
            onValueChange={setStock}
          />
          <div className="flex items-end pb-2">
            <CheckboxField
              id="product-active"
              label="Producto activo"
              checked={active}
              onCheckedChange={setActive}
            />
          </div>
        </div>
      </section>

      {hasBrandError && (
        <p className="m-0 text-sm text-red-700" role="alert">
          No se pudieron cargar las marcas.
        </p>
      )}

      <section>
        <h3 className="mb-3 text-sm font-bold uppercase tracking-wide text-[#647895]">
          Imágenes
        </h3>
        <div className="space-y-3">
          {images.map((image, index) => (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2" key={image.id}>
              <InputField
                id={`product-image-url-${image.id}`}
                label={`URL de imagen ${index + 1}`}
                type="url"
                placeholder="https://..."
                maxLength={500}
                value={image.imageUrl}
                onValueChange={(imageUrl) => updateImage(image.id, { imageUrl })}
              />
              <InputField
                id={`product-image-order-${image.id}`}
                label="Orden"
                type="number"
                min={0}
                step={1}
                value={image.displayOrder}
                onValueChange={(displayOrder) =>
                  updateImage(image.id, { displayOrder })
                }
              />
              <div className="flex items-center justify-between sm:col-span-2">
                <CheckboxField
                  id={`product-image-primary-${image.id}`}
                  label="Imagen principal"
                  checked={image.isPrimary}
                  onCheckedChange={(isPrimary) => {
                    setImages((current) =>
                      current.map((currentImage) => ({
                        ...currentImage,
                        isPrimary: isPrimary && currentImage.id === image.id,
                      }))
                    );
                  }}
                />
                {images.length > 1 && (
                  <Button
                    type="button"
                    size="small"
                    variant="transparent"
                    onClick={() => removeImage(image.id)}
                    aria-label={`Quitar imagen ${index + 1}`}
                  >
                    <FiTrash2 aria-hidden="true" className="h-4 w-4" />
                    Quitar
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
        <div className="mt-3 flex justify-end">
          <Button type="button" size="small" variant="outline" onClick={addImage}>
            <FiPlus aria-hidden="true" className="h-3.5 w-3.5" />
            Agregar imagen
          </Button>
        </div>
      </section>

      <section>
        <h3 className="mb-3 text-sm font-bold uppercase tracking-wide text-[#647895]">
          Especificaciones
        </h3>
        {selectedCategory ? (
          specificationSchema.length > 0 ? (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {specificationSchema.map((specification) => {
                const id = `product-specification-${specification.key}`;
                const label = specification.unit
                  ? `${specification.label} (${specification.unit})`
                  : specification.label;
                const value = specifications[specification.key];

                if (specification.type === "boolean") {
                  return (
                    <div className="flex items-end pb-2" key={specification.key}>
                      <CheckboxField
                        id={id}
                        label={label}
                        checked={value === true}
                        onCheckedChange={(checked) =>
                          updateSpecification(specification.key, checked)
                        }
                      />
                    </div>
                  );
                }

                if (specification.type === "select") {
                  return (
                    <SelectField
                      id={id}
                      key={specification.key}
                      label={label}
                      options={(specification.options ?? []).map((option) => ({
                        value: option,
                        label: option,
                      }))}
                      required={specification.required}
                      value={typeof value === "string" ? value : ""}
                      onChange={(nextValue) =>
                        updateSpecification(specification.key, nextValue)
                      }
                    />
                  );
                }

                return (
                  <InputField
                    id={id}
                    key={specification.key}
                    label={label}
                    type={specification.type === "number" ? "number" : "text"}
                    step={specification.type === "number" ? "any" : undefined}
                    required={specification.required}
                    value={typeof value === "string" ? value : ""}
                    onValueChange={(nextValue) =>
                      updateSpecification(specification.key, nextValue)
                    }
                  />
                );
              })}
            </div>
          ) : (
            <p className="m-0 text-sm text-[#71839d]">
              Esta categoría no tiene especificaciones configuradas.
            </p>
          )
        ) : hasCategoryError ? (
          <p className="m-0 text-sm text-red-700" role="alert">
            No se pudieron cargar las categorías.
          </p>
        ) : categories.length === 0 && !isLoadingCategories ? (
          <p className="m-0 text-sm text-[#71839d]">
            No hay categorías activas configuradas.
          </p>
        ) : (
          <p className="m-0 text-sm text-[#71839d]">
            {isLoadingCategories
              ? "Cargando categorías..."
              : "Selecciona una categoría para ver sus especificaciones."}
          </p>
        )}
      </section>

      {submitError && (
        <p className="m-0 text-sm text-red-700" role="alert">
          {submitError}
        </p>
      )}

      <div className="flex justify-end gap-2 border-t border-[#e0e6ef] pt-4">
        <Button type="button" size="small" variant="outline" onClick={onCancel}>
          Cancelar
        </Button>
        <Button
          type="button"
          size="small"
          disabled={
            isSaving ||
            isLoadingCategories ||
            hasCategoryError ||
            !selectedCategory ||
            isLoadingBrands ||
            hasBrandError ||
            !selectedBrand
          }
          onClick={() => void handleSave()}
        >
          {isSaving ? "Guardando..." : "Guardar Producto"}
        </Button>
      </div>
    </div>
  );
}
