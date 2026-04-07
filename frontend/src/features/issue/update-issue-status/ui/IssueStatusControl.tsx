import type { ChangeEvent } from 'react';

import type { IssueStatus } from '@/entities/issue/model/types';

interface IssueStatusControlProps {
  value: IssueStatus;
  disabled?: boolean;
  onChange: (nextStatus: IssueStatus) => void;
}

export function IssueStatusControl({ value, disabled, onChange }: IssueStatusControlProps) {
  const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
    onChange(event.target.value as IssueStatus);
  };

  return (
    <div className="field" style={{ minWidth: 160 }}>
      <label htmlFor="issue-status">상태</label>
      <select id="issue-status" value={value} disabled={disabled} onChange={handleChange}>
        <option value="OPEN">열림</option>
        <option value="CLOSED">닫힘</option>
      </select>
    </div>
  );
}
