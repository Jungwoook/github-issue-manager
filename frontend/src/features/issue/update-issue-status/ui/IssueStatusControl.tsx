import type { ChangeEvent } from 'react';

import type { IssueState } from '@/entities/issue/model/types';

interface IssueStatusControlProps {
  value: IssueState;
  disabled?: boolean;
  onChange: (nextStatus: IssueState) => void;
}

export function IssueStatusControl({ value, disabled, onChange }: IssueStatusControlProps) {
  const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
    onChange(event.target.value as IssueState);
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
