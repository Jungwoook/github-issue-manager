import type { PropsWithChildren, ReactNode } from 'react';

interface PageHeaderProps extends PropsWithChildren {
  title: string;
  description?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, description, actions }: PageHeaderProps) {
  return (
    <header className="page-header">
      <div>
        <h2>{title}</h2>
        {description ? <p className="muted">{description}</p> : null}
      </div>
      {actions}
    </header>
  );
}
