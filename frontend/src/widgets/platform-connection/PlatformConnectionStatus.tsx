import { Link } from 'react-router-dom';

import { useQuery } from '@tanstack/react-query';

import { getPlatformTokenStatus } from '@/entities/platform-connection/api/platformConnectionApi';
import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { platformSettingsPath } from '@/shared/lib/routes';

export function PlatformConnectionStatus({ platform = DEFAULT_PLATFORM }: { platform?: string }) {
  const statusQuery = useQuery({
    queryKey: queryKeys.platformTokenStatus(platform),
    queryFn: () => getPlatformTokenStatus(platform),
    retry: false,
  });

  if (statusQuery.isLoading) {
    return <div className="sidebar-status-card">플랫폼 연결 상태를 확인하는 중...</div>;
  }

  if (statusQuery.isError || !statusQuery.data) {
    return (
      <div className="sidebar-status-card">
        <strong>연결 필요</strong>
        <p className="muted">토큰을 등록하면 저장소와 이슈를 불러올 수 있습니다.</p>
        <Link className="button button-ghost" to={platformSettingsPath(platform)}>
          연결 설정
        </Link>
      </div>
    );
  }

  const status = statusQuery.data;

  return (
    <div className="sidebar-status-card">
      <strong>{status.connected ? '연결됨' : '연결 필요'}</strong>
      <p className="muted">
        {status.connected && status.accountLogin
          ? `계정: ${status.accountLogin}`
          : '토큰을 등록하면 저장소와 이슈를 불러올 수 있습니다.'}
      </p>
      {status.connected && status.tokenVerifiedAt ? (
        <p className="muted">최근 확인: {formatDate(status.tokenVerifiedAt)}</p>
      ) : null}
      <Link className="button button-ghost" to={platformSettingsPath(platform)}>
        {status.connected ? '연결 관리' : '연결 설정'}
      </Link>
    </div>
  );
}
