type CheckboxFieldProps = {
  id: string;
  label: string;
  description?: string;
  defaultChecked?: boolean;
  checked?: boolean;
  onCheckedChange?: (checked: boolean) => void;
};

export default function CheckboxField({
  id,
  label,
  description,
  defaultChecked = false,
  checked,
  onCheckedChange,
}: CheckboxFieldProps) {
  return (
    <div className="flex min-w-0 items-center gap-2.5">
      <input
        className="h-4 w-4 shrink-0 accent-[#111827]"
        id={id}
        aria-labelledby={`${id}-label`}
        type="checkbox"
        checked={checked}
        defaultChecked={checked === undefined ? defaultChecked : undefined}
        onChange={(event) => onCheckedChange?.(event.currentTarget.checked)}
      />
      <div className="min-w-0">
        <span
          id={`${id}-label`}
          className="text-sm font-medium text-[#344054]"
        >
          {label}
        </span>
        {description && (
          <p className="mb-0 mt-1 text-xs leading-relaxed text-[#71839d]">
            {description}
          </p>
        )}
      </div>
    </div>
  );
}
