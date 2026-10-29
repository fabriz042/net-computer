import { FiSearch } from "react-icons/fi";

interface SearchInputProps {
  id: string;
  label: string;
  placeholder: string;
  value: string;
  onValueChange: (value: string) => void;
}

export default function SearchInput({
  id,
  label,
  placeholder,
  value,
  onValueChange,
}: SearchInputProps) {
  return (
    <div
      className="
        flex h-9 w-full min-w-0 items-center gap-2.5 px-3
        text-[#71839d]
        rounded-md border border-[#d5deea] bg-white
      "
      role="search"
    >
      <FiSearch aria-hidden="true" className="h-4 w-4 shrink-0" />
      <input
        id={id}
        type="search"
        placeholder={placeholder}
        aria-label={label}
        value={value}
        onChange={(event) => onValueChange(event.currentTarget.value)}
        className="
          w-full min-w-0 p-0
          text-sm text-[#111827] placeholder:text-[#94a3b8]
          border-0 bg-transparent outline-none
        "
      />
    </div>
  );
}
