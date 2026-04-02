import type { IssueStatus } from '@/entities/issue/model/types';

interface IssueMetaTagsProps {
  status: IssueStatus;
}

function toStatusClass(status: IssueStatus) {
  return status === 'OPEN' ? 'status-open' : 'status-closed';
}

export function IssueMetaTags({ status }: IssueMetaTagsProps) {
  const statusLabel = status === 'OPEN' ? '열림' : '닫힘';

  return (
    <div className="tag-row">
      <span className={`tag ${toStatusClass(status)}`}>{statusLabel}</span>
    </div>
  );
}
