import type { ChangeEvent } from 'react';

import type { UserSummary } from '@/entities/user/model/types';

interface IssueAssigneeControlProps {
  assigneeId: number | null;
  users: UserSummary[];
  disabled?: boolean;
  onChange: (nextAssigneeId: number | null) => void;
}

export function IssueAssigneeControl({
  assigneeId,
  users,
  disabled,
  onChange,
}: IssueAssigneeControlProps) {
  const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
    onChange(event.target.value ? Number(event.target.value) : null);
  };

  return (
    <div className="field" style={{ minWidth: 220 }}>
      <label htmlFor="issue-assignee">담당자</label>
      <select
        id="issue-assignee"
        value={assigneeId ?? ''}
        disabled={disabled}
        onChange={handleChange}
      >
        <option value="">미지정</option>
        {users.map((user) => (
          <option key={user.id} value={user.id}>
            {user.displayName} ({user.username})
          </option>
        ))}
      </select>
    </div>
  );
}
