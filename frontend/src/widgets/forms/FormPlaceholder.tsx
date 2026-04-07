import type { ReactNode } from 'react';

import { PlaceholderCard } from '@/shared/ui/PlaceholderCard';

interface FormPlaceholderProps {
  title: string;
  description: string;
  children?: ReactNode;
}

export function FormPlaceholder({ title, description, children }: FormPlaceholderProps) {
  return (
    <PlaceholderCard title={title} description={description} footer={children} />
  );
}
