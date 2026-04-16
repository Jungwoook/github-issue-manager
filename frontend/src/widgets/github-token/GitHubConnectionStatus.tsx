import { Link } from 'react-router-dom';

import { useQuery } from '@tanstack/react-query';

import { getGitHubTokenStatus } from '@/entities/github/api/githubTokenApi';
import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';

export function GitHubConnectionStatus() {
  const statusQuery = useQuery({
    queryKey: queryKeys.platformTokenStatus(DEFAULT_PLATFORM),
    queryFn: getGitHubTokenStatus,
    retry: false,
  });

  if (statusQuery.isLoading) {
    return <div className="sidebar-status-card">GitHub 연결 상태를 확인하는 중...</div>;
  }

  if (statusQuery.isError || !statusQuery.data) {
    return (
      <div className="sidebar-status-card">
        <strong>GitHub 연결 안 됨</strong>
        <p className="muted">PAT를 등록한 뒤 저장소와 이슈를 불러오세요.</p>
        <Link className="button button-ghost" to="/settings/github">
          PAT 설정
        </Link>
      </div>
    );
  }

  const status = statusQuery.data;

  return (
    <div className="sidebar-status-card">
      <strong>{status.connected ? 'GitHub 연결됨' : 'GitHub 연결 안 됨'}</strong>
      <p className="muted">
        {status.connected && status.accountLogin
          ? `계정: ${status.accountLogin}`
          : 'PAT를 등록한 뒤 저장소와 이슈를 불러오세요.'}
      </p>
      {status.connected && status.tokenVerifiedAt ? (
        <p className="muted">최근 확인: {formatDate(status.tokenVerifiedAt)}</p>
      ) : null}
      <Link className="button button-ghost" to="/settings/github">
        {status.connected ? '연결 관리' : 'PAT 설정'}
      </Link>
    </div>
  );
}
