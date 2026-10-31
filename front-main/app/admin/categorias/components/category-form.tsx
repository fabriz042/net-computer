"use client";

import { useState } from "react";
import Button from "@/app/components/ui/button";
import CheckboxField from "@/app/components/ui/checkbox-field";
import InputField from "@/app/components/ui/input-field";
import {
  createAdminCategory,
  updateAdminCategory,
} from "@/app/modules/catalog/category/services/admin-category.service";
import type {
  AdminCategory,
  AdminCategoryRequest,
} from "@/app/modules/catalog/category/types/admin-category.type";
import { getApiErrorMessage } from "@/app/modules/catalog/utils/api-error-message";

interface CategoryFormProps {
  category: AdminCategory | null;
  onCancel: () => void;
  onSaved: (message: string) => void;
}

export default function CategoryForm({
  category,
  onCancel,
  onSaved,
}: CategoryFormProps) {
  const [name, setName] = useState(category?.name ?? "");
  const [slug, setSlug] = useState(category?.slug ?? "");
  const [active, setActive] = useState(category?.active ?? true);
  const [isSaving, setIsSaving] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const isEditing = category !== null;
  const specificationCount = category?.specificationSchema.length ?? 0;

  const handleSave = async () => {
    setSubmitError("");

    const categoryName = name.trim();
    if (!categoryName) {
      setSubmitError("El nombre de la categoría es obligatorio.");
      return;
    }

    // The specification schema is omitted on purpose: the backend keeps the current one.
    const request: AdminCategoryRequest = {
      name: categoryName,
      slug: slug.trim() || undefined,
      active,
    };

    setIsSaving(true);
    let wasSaved = false;
    try {
      if (category) {
        await updateAdminCategory(category.id, request);
      } else {
        await createAdminCategory(request);
      }
      wasSaved = true;
    } catch (error) {
      setSubmitError(
        getApiErrorMessage(
          error,
          "No se pudo guardar la categoría. Revisa los datos e inténtalo de nuevo.",
          "Ya existe una categoría con ese nombre o slug."
        )
      );
    } finally {
      setIsSaving(false);
      if (wasSaved) {
        onSaved(
          isEditing
            ? "Categoría actualizada correctamente."
            : "Categoría creada correctamente."
        );
      }
    }
  };

  return (
    <div className="mt-5 space-y-5">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <InputField
          id="category-name"
          label="Nombre"
          placeholder="Nombre de la categoría"
          maxLength={100}
          required
          value={name}
          onValueChange={setName}
        />
        <InputField
          id="category-slug"
          label="Slug"
          placeholder="Se genera a partir del nombre"
          maxLength={100}
          value={slug}
          onValueChange={setSlug}
        />
        <div className="sm:col-span-2">
          <CheckboxField
            id="category-active"
            label="Categoría activa"
            description="Las categorías inactivas no se pueden elegir al registrar productos."
            checked={active}
            onCheckedChange={setActive}
          />
        </div>
      </div>

      {isEditing && (
        <p className="m-0 text-sm text-[#71839d]">
          {specificationCount === 0
            ? "Esta categoría no tiene especificaciones configuradas."
            : `Esta categoría tiene ${specificationCount} ${
                specificationCount === 1 ? "especificación" : "especificaciones"
              } que se mantienen sin cambios.`}
        </p>
      )}

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
          disabled={isSaving}
          onClick={() => void handleSave()}
        >
          {isSaving
            ? "Guardando..."
            : isEditing
              ? "Guardar Cambios"
              : "Guardar Categoría"}
        </Button>
      </div>
    </div>
  );
}
