type InputFieldProps = {
  id: string;
  label: string;
  type?: "text" | "number" | "url" | "email";
  placeholder?: string;
  required?: boolean;
  min?: number;
  max?: number;
  step?: number | "any";
  maxLength?: number;
  defaultValue?: string | number;
  value?: string | number;
  onValueChange?: (value: string) => void;
};

export default function InputField({
  id,
  label,
  type = "text",
  placeholder,
  required = false,
  min,
  max,
  step,
  maxLength,
  defaultValue,
  value,
  onValueChange,
}: InputFieldProps) {
  return (
    <div className="min-w-0">
      <span
        id={`${id}-label`}
        className="mb-1.5 block text-sm font-semibold text-[#344054]"
      >
        {label}
        {required && <span className="ml-1 text-red-600" aria-hidden="true">*</span>}
      </span>
      <input
        className="
          h-10 w-full px-3
          text-sm text-[#111827] placeholder:text-[#94a3b8]
          rounded-md border border-[#d5deea] bg-white outline-none
          transition focus:border-[#64748b] focus:ring-2 focus:ring-slate-100
        "
        id={id}
        aria-labelledby={`${id}-label`}
        type={type}
        placeholder={placeholder}
        required={required}
        min={min}
        max={max}
        step={step}
        maxLength={maxLength}
        value={value}
        defaultValue={value === undefined ? defaultValue : undefined}
        onChange={(event) => onValueChange?.(event.currentTarget.value)}
      />
    </div>
  );
}
