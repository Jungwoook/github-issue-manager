import type { Label } from '@/entities/label/model/types';

interface LabelBadgeProps {
  label: Label;
}

export function LabelBadge({ label }: LabelBadgeProps) {
  return (
    <span
      className="tag"
      style={{
        background: `${label.color}20`,
        color: label.color,
      }}
    >
      {label.name}
    </span>
  );
}
