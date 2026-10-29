import Link from "next/link";
import type {
  AnchorHTMLAttributes,
  ButtonHTMLAttributes,
  ReactNode,
} from "react";

type ButtonVariant = "black" | "transparent" | "outline";
type ButtonSize =
  | "search"
  | "icon"
  | "chip"
  | "small"
  | "medium"
  | "default"
  | "wide"
  | "large";

type BaseActionButtonProps = {
  children: ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
};

type LinkActionButtonProps = BaseActionButtonProps &
  Omit<
    AnchorHTMLAttributes<HTMLAnchorElement>,
    "children" | "className" | "href" | "style"
  > & {
    href: string;
  };

type NativeActionButtonProps = BaseActionButtonProps &
  Omit<ButtonHTMLAttributes<HTMLButtonElement>, "children" | "className" | "style">;

type ActionButtonProps = LinkActionButtonProps | NativeActionButtonProps;

const sizeClasses: Record<ButtonSize, string> = {
  search: "h-6 w-5 p-0",
  icon: "relative h-9 w-9 rounded-lg p-0",
  chip: "h-7 px-2.5 text-xs font-medium",
  small: "h-7 px-3 text-sm font-semibold",
  medium: "h-8 px-5 text-sm font-medium",
  default: "h-9 px-4 text-sm font-semibold",
  wide: "h-[35px] w-[98px] px-3 text-sm font-semibold min-[421px]:w-[116px]",
  large: "h-10 min-w-[134px] px-5 text-sm font-semibold",
};

const variantClasses: Record<ButtonVariant, string> = {
  black: "text-white rounded-md bg-[#111827] transition hover:bg-[#273246]",
  transparent: "text-[#344054] rounded-md bg-transparent transition hover:bg-slate-50 hover:text-[#111827]",
  outline: "text-[#344054] rounded-md border border-[#d5deea] bg-white transition hover:bg-slate-50",
};

function getClassName(size: ButtonSize, variant: ButtonVariant): string {
  return `
    inline-flex shrink-0 cursor-pointer items-center justify-center gap-2
    ${sizeClasses[size]} no-underline
    ${variantClasses[variant]}
    focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#111827]
    disabled:cursor-not-allowed disabled:opacity-50
  `;
}

export default function Button(props: ActionButtonProps) {
  const { children, variant = "black", size = "default", ...rest } = props;
  const className = getClassName(size, variant);

  if ("href" in rest) {
    const { href, ...linkProps } = rest;

    return (
      <Link className={className} href={href} {...linkProps}>
        {children}
      </Link>
    );
  }

  return (
    <button className={className} {...rest}>
      {children}
    </button>
  );
}
