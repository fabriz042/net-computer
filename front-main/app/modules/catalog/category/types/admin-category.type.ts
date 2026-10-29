export type CategorySpecificationInputType =
  | "text"
  | "number"
  | "boolean"
  | "select";

export interface CategorySpecificationDefinition {
  key: string;
  label: string;
  type: CategorySpecificationInputType;
  required: boolean;
  unit: string | null;
  options: string[];
}

export interface AdminCategory {
  id: string;
  slug: string;
  name: string;
  active: boolean;
  specificationSchema: CategorySpecificationDefinition[];
}
