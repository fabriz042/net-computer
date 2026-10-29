export type FilterOption = {
  value: string;
  label: string;
};

interface FilterSelectProps {
  id: string;
  label: string;
  defaultValue: string;
  options: FilterOption[];
}

export default function FilterSelect({
  id,
  label,
  defaultValue,
  options,
}: FilterSelectProps) {
  return (
    <>
      <select
        id={id}
        aria-label={label}
        className="
          h-9 w-full px-3
          text-sm text-[#111827]
          rounded-md border border-[#d5deea] bg-white outline-none
          focus:border-blue-500
        "
        defaultValue={defaultValue}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </>
  );
}
