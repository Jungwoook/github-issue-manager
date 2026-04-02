import type { ChangeEvent } from 'react';

import type { IssuePriority } from '@/entities/issue/model/types';

interface IssuePriorityControlProps {
  value: IssuePriority;
  disabled?: boolean;
  onChange: (nextPriority: IssuePriority) => void;
}

export function IssuePriorityControl({ value, disabled, onChange }: IssuePriorityControlProps) {
  const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
    onChange(event.target.value as IssuePriority);
  };

  return (
    <div className="field" style={{ minWidth: 160 }}>
      <label htmlFor="issue-priority">우선순위</label>
      <select id="issue-priority" value={value} disabled={disabled} onChange={handleChange}>
        <option value="HIGH">높음</option>
        <option value="MEDIUM">보통</option>
        <option value="LOW">낮음</option>
      </select>
    </div>
  );
}
