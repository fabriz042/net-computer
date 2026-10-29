export type SelectFieldOption = {
  value: string;
  label: string;
};

type SelectFieldProps = {
  id: string;
  label: string;
  options: SelectFieldOption[];
  placeholder?: string;
  required?: boolean;
  defaultValue?: string;
  value?: string;
  disabled?: boolean;
  onChange?: (value: string) => void;
};

export default function SelectField({
  id,
  label,
  options,
  placeholder = "Selecciona una opción",
  required = false,
  defaultValue = "",
  value,
  disabled = false,
  onChange,
}: SelectFieldProps) {
  return (
    <div className="min-w-0">
      <span
        id={`${id}-label`}
        className="mb-1.5 block text-sm font-semibold text-[#344054]"
      >
        {label}
        {required && <span className="ml-1 text-red-600" aria-hidden="true">*</span>}
      </span>
      <select
        className="
          h-10 w-full px-3
          text-sm text-[#111827]
          rounded-md border border-[#d5deea] bg-white outline-none
          transition focus:border-[#64748b] focus:ring-2 focus:ring-slate-100
        "
        id={id}
        aria-labelledby={`${id}-label`}
        required={required}
        disabled={disabled}
        value={value}
        defaultValue={value === undefined ? defaultValue : undefined}
        onChange={(event) => onChange?.(event.target.value)}
      >
        <option value="" disabled={required}>
          {placeholder}
        </option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
}
