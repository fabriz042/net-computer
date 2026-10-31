"use client";

import { useState } from "react";
import Button from "@/app/components/ui/button";
import InputField from "@/app/components/ui/input-field";
import {
  createAdminBrand,
  updateAdminBrand,
} from "@/app/modules/catalog/brand/services/admin-brand.service";
import type {
  AdminBrand,
  AdminBrandRequest,
} from "@/app/modules/catalog/brand/types/admin-brand.type";
import { getApiErrorMessage } from "@/app/modules/catalog/utils/api-error-message";

interface BrandFormProps {
  brand: AdminBrand | null;
  onCancel: () => void;
  onSaved: (message: string) => void;
}

export default function BrandForm({ brand, onCancel, onSaved }: BrandFormProps) {
  const [name, setName] = useState(brand?.name ?? "");
  const [slug, setSlug] = useState(brand?.slug ?? "");
  const [isSaving, setIsSaving] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const isEditing = brand !== null;

  const handleSave = async () => {
    setSubmitError("");

    const brandName = name.trim();
    if (!brandName) {
      setSubmitError("El nombre de la marca es obligatorio.");
      return;
    }

    const request: AdminBrandRequest = {
      name: brandName,
      slug: slug.trim() || undefined,
    };

    setIsSaving(true);
    let wasSaved = false;
    try {
      if (brand) {
        await updateAdminBrand(brand.id, request);
      } else {
        await createAdminBrand(request);
      }
      wasSaved = true;
    } catch (error) {
      setSubmitError(
        getApiErrorMessage(
          error,
          "No se pudo guardar la marca. Revisa los datos e inténtalo de nuevo.",
          "Ya existe una marca con ese nombre o slug."
        )
      );
    } finally {
      setIsSaving(false);
      if (wasSaved) {
        onSaved(
          isEditing
            ? "Marca actualizada correctamente."
            : "Marca creada correctamente."
        );
      }
    }
  };

  return (
    <div className="mt-5 space-y-5">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <InputField
          id="brand-name"
          label="Nombre"
          placeholder="Nombre de la marca"
          maxLength={100}
          required
          value={name}
          onValueChange={setName}
        />
        <InputField
          id="brand-slug"
          label="Slug"
          placeholder="Se genera a partir del nombre"
          maxLength={100}
          value={slug}
          onValueChange={setSlug}
        />
      </div>

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
              : "Guardar Marca"}
        </Button>
      </div>
    </div>
  );
}
