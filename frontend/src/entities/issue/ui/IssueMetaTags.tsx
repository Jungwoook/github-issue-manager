import type { IssueState } from '@/entities/issue/model/types';

interface IssueMetaTagsProps {
  state: IssueState;
}

function toStatusClass(state: IssueState) {
  return state === 'OPEN' ? 'status-open' : 'status-closed';
}

export function IssueMetaTags({ state }: IssueMetaTagsProps) {
  const statusLabel = state === 'OPEN' ? '열림' : '닫힘';

  return (
    <div className="tag-row">
      <span className={`tag ${toStatusClass(state)}`}>{statusLabel}</span>
    </div>
  );
}
