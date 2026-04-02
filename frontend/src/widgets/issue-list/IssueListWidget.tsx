import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { deleteIssue, getIssues, refreshIssues } from '@/entities/issue/api/issueApi';
import { IssueMetaTags } from '@/entities/issue/ui/IssueMetaTags';
import { getRepository } from '@/entities/repository/api/repositoryApi';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';

export function IssueListWidget() {
  const { repositoryId } = useParams();
  const queryClient = useQueryClient();
  const [keyword, setKeyword] = useState('');
  const [state, setState] = useState('');

  const repositoryQuery = useQuery({
    queryKey: queryKeys.repository(repositoryId ?? ''),
    queryFn: () => getRepository(repositoryId ?? ''),
    enabled: Boolean(repositoryId),
  });

  const issuesQuery = useQuery({
    queryKey: queryKeys.issues(repositoryId ?? '', { keyword, state }),
    queryFn: () =>
      getIssues(repositoryId ?? '', {
        keyword: keyword || undefined,
        state: (state || undefined) as never,
      }),
    enabled: Boolean(repositoryId),
  });

  const refreshIssuesMutation = useMutation({
    mutationFn: () => refreshIssues(repositoryId ?? ''),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
    },
  });

  const deleteIssueMutation = useMutation({
    mutationFn: (issueId: number) => deleteIssue(repositoryId ?? '', issueId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
    },
  });

  if (!repositoryId) {
    return <div className="error-banner">경로에 저장소 ID가 없습니다.</div>;
  }

  const issues = (issuesQuery.data ?? []) as Array<{
    number: number;
    title: string;
    status: 'OPEN' | 'CLOSED';
    authorLogin?: string | null;
    updatedAt: string;
  }>;

  return (
    <div className="page-stack">
      {repositoryQuery.data ? (
        <section className="stats-grid">
          <article className="stat-card">
            <p className="muted">저장소</p>
            <p className="stat-card-value">
              {(repositoryQuery.data as { fullName?: string; name?: string }).fullName ??
                (repositoryQuery.data as { name?: string }).name}
            </p>
          </article>
          <article className="stat-card">
            <p className="muted">열린 이슈</p>
            <p className="stat-card-value">{issues.filter((issue) => issue.status === 'OPEN').length}</p>
          </article>
          <article className="stat-card">
            <p className="muted">전체 이슈</p>
            <p className="stat-card-value">{issues.length}</p>
          </article>
        </section>
      ) : null}

      <section className="form-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">필터</h3>
            <p className="muted">검색어와 상태로 이슈를 조회합니다.</p>
          </div>
          <div className="toolbar-actions">
            <button
              className="button"
              type="button"
              disabled={refreshIssuesMutation.isPending}
              onClick={() => refreshIssuesMutation.mutate()}
            >
              이슈 새로고침
            </button>
          </div>
        </div>
        <div className="filters-grid">
          <div className="field">
            <label htmlFor="issue-filter-keyword">검색어</label>
            <input
              id="issue-filter-keyword"
              value={keyword}
              placeholder="이슈 제목 검색"
              onChange={(event) => setKeyword(event.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="issue-filter-state">상태</label>
            <select id="issue-filter-state" value={state} onChange={(event) => setState(event.target.value)}>
              <option value="">전체</option>
              <option value="OPEN">열림</option>
              <option value="CLOSED">닫힘</option>
            </select>
          </div>
        </div>
      </section>

      <section className="list-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">이슈 목록</h3>
            <p className="muted">선택한 저장소의 GitHub 이슈 목록입니다.</p>
          </div>
        </div>

        {issuesQuery.isLoading || repositoryQuery.isLoading ? (
          <div className="info-banner">이슈를 불러오는 중입니다...</div>
        ) : null}
        {issuesQuery.isError ? (
          <div className="error-banner">이슈를 불러오지 못했습니다. {getErrorMessage(issuesQuery.error)}</div>
        ) : null}
        {refreshIssuesMutation.isError ? (
          <div className="error-banner">
            이슈 새로고침에 실패했습니다. {getErrorMessage(refreshIssuesMutation.error)}
          </div>
        ) : null}
        {refreshIssuesMutation.isSuccess ? (
          <div className="success-banner">이슈 목록을 새로고침했습니다.</div>
        ) : null}
        {issues.length === 0 && !issuesQuery.isLoading ? (
          <div className="empty-state">조건에 맞는 이슈가 없습니다.</div>
        ) : null}

        {issues.length > 0 ? (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>이슈</th>
                  <th>상태</th>
                  <th>작성자</th>
                  <th>수정일</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {issues.map((issue) => (
                  <tr key={issue.number}>
                    <td>
                      <div className="stack-sm">
                        <Link className="issue-title-link" to={`/repositories/${repositoryId}/issues/${issue.number}`}>
                          {issue.title}
                        </Link>
                        <span className="muted">#{issue.number}</span>
                      </div>
                    </td>
                    <td>
                      <IssueMetaTags status={issue.status} />
                    </td>
                    <td>{issue.authorLogin ?? '-'}</td>
                    <td>{formatDate(issue.updatedAt)}</td>
                    <td>
                      <div className="toolbar-actions">
                        <Link className="inline-button" to={`/repositories/${repositoryId}/issues/${issue.number}`}>
                          열기
                        </Link>
                        <Link className="inline-button" to={`/repositories/${repositoryId}/issues/${issue.number}/edit`}>
                          수정
                        </Link>
                        <button
                          className="inline-button button-danger"
                          type="button"
                          disabled={deleteIssueMutation.isPending}
                          onClick={() => deleteIssueMutation.mutate(issue.number)}
                        >
                          닫기
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </div>
  );
}
