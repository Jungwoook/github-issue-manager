import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { getGitHubTokenStatus } from '@/entities/github/api/githubTokenApi';
import {
  getRepositories,
  refreshRepositories,
} from '@/entities/repository/api/repositoryApi';
import type { Repository } from '@/entities/repository/model/types';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';

export function RepositoryListWidget() {
  const queryClient = useQueryClient();
  const [hasAutoRefreshed, setHasAutoRefreshed] = useState(false);

  const tokenStatusQuery = useQuery({
    queryKey: queryKeys.githubTokenStatus,
    queryFn: getGitHubTokenStatus,
    retry: false,
  });

  const repositoryQuery = useQuery({
    queryKey: queryKeys.repositories,
    queryFn: getRepositories,
  });

  const refreshMutation = useMutation({
    mutationFn: refreshRepositories,
    onSuccess: async () => {
      setHasAutoRefreshed(true);
      await queryClient.invalidateQueries({ queryKey: queryKeys.repositories });
    },
  });

  const repositories = repositoryQuery.data ?? [];
  const tokenConnected = tokenStatusQuery.data?.connected ?? false;
  const connectedOwners = new Set(repositories.map((repository) => repository.ownerLogin)).size;
  const privateRepositoryCount = repositories.filter((repository) => repository.private).length;
  const submitError = refreshMutation.error;

  useEffect(() => {
    if (
      tokenConnected &&
      !repositoryQuery.isLoading &&
      repositories.length === 0 &&
      !refreshMutation.isPending &&
      !hasAutoRefreshed
    ) {
      setHasAutoRefreshed(true);
      refreshMutation.mutate();
    }
  }, [
    hasAutoRefreshed,
    refreshMutation,
    repositories.length,
    repositoryQuery.isLoading,
    tokenConnected,
  ]);

  return (
    <div className="page-stack">
      <section className="stats-grid">
        <article className="stat-card">
          <p className="muted">조회된 저장소</p>
          <p className="stat-card-value">{repositories.length}</p>
        </article>
        <article className="stat-card">
          <p className="muted">연결된 소유자</p>
          <p className="stat-card-value">{connectedOwners}</p>
        </article>
        <article className="stat-card">
          <p className="muted">비공개 저장소</p>
          <p className="stat-card-value">{privateRepositoryCount}</p>
        </article>
      </section>

      <section className="list-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">GitHub 저장소 목록</h3>
            <p className="muted">PAT로 접근 가능한 저장소를 불러오고, 이슈 화면으로 이동할 수 있습니다.</p>
          </div>
          <div className="toolbar-actions">
            <button
              className="button button-primary"
              type="button"
              disabled={refreshMutation.isPending || !tokenConnected}
              onClick={() => refreshMutation.mutate()}
            >
              저장소 새로고침
            </button>
          </div>
        </div>

        {!tokenConnected && !tokenStatusQuery.isLoading ? (
          <div className="info-banner">먼저 GitHub PAT를 등록해야 저장소를 불러올 수 있습니다.</div>
        ) : null}
        {repositoryQuery.isLoading ? <div className="info-banner">저장소를 불러오는 중입니다...</div> : null}
        {repositoryQuery.isError ? (
          <div className="error-banner">
            저장소를 불러오지 못했습니다. {getErrorMessage(repositoryQuery.error)}
          </div>
        ) : null}
        {submitError ? <div className="error-banner">{getErrorMessage(submitError)}</div> : null}
        {refreshMutation.isSuccess ? (
          <div className="success-banner">저장소 목록을 새로고침했습니다.</div>
        ) : null}

        {repositories.length === 0 && tokenConnected && !repositoryQuery.isLoading && !refreshMutation.isPending ? (
          <div className="empty-state">
            아직 불러온 저장소가 없습니다. PAT 권한을 확인한 뒤 `저장소 새로고침`을 눌러 GitHub 저장소를 가져오세요.
          </div>
        ) : null}

        <div className="card-grid compact">
          {repositories.map((repository) => (
            <RepositoryCard key={repository.githubRepositoryId} repository={repository} />
          ))}
        </div>
      </section>
    </div>
  );
}

function RepositoryCard({ repository }: { repository: Repository }) {
  return (
    <article className="detail-card">
      <div className="card-header">
        <div className="stack-sm">
          <Link className="issue-title-link" to={`/repositories/${repository.githubRepositoryId}/issues`}>
            {repository.fullName}
          </Link>
          <span className="muted">동기화: {formatDate(repository.lastSyncedAt)}</span>
        </div>
        <span className={`tag ${repository.private ? 'priority-high' : 'priority-low'}`}>
          {repository.private ? '비공개' : '공개'}
        </span>
      </div>
      <p className="muted">{repository.description?.trim() || '설명이 없습니다.'}</p>
      <div className="toolbar">
        <Link className="button" to={`/repositories/${repository.githubRepositoryId}/issues`}>
          이슈 보기
        </Link>
        <a
          className="button button-ghost"
          href={repository.htmlUrl}
          target="_blank"
          rel="noreferrer"
        >
          GitHub 열기
        </a>
      </div>
    </article>
  );
}
