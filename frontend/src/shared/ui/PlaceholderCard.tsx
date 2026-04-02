import type { ReactNode } from 'react';

interface PlaceholderCardProps {
  title: string;
  description: string;
  footer?: ReactNode;
}

export function PlaceholderCard({ title, description, footer }: PlaceholderCardProps) {
  return (
    <section className="placeholder-card">
      <h3 className="section-title">{title}</h3>
      <p className="muted">{description}</p>
      {footer}
    </section>
  );
}
