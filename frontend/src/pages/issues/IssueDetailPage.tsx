import { useNavigate, useParams } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { createComment, getComments, refreshComments } from '@/entities/comment/api/commentApi';
import { deleteIssue, getIssueDetail, updateIssue } from '@/entities/issue/api/issueApi';
import { CreateCommentForm } from '@/features/comment/create-comment/ui/CreateCommentForm';
import { IssueStatusControl } from '@/features/issue/update-issue-status/ui/IssueStatusControl';
import { queryKeys } from '@/shared/constants/queryKeys';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';
import { PageHeader } from '@/shared/ui/PageHeader';
import { IssueDetailSection } from '@/widgets/issue-detail/IssueDetailSection';

export function IssueDetailPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { repositoryId, issueId } = useParams();

  const issueQuery = useQuery({
    queryKey: queryKeys.issue(repositoryId ?? '', issueId ?? ''),
    queryFn: () => getIssueDetail(repositoryId ?? '', issueId ?? ''),
    enabled: Boolean(repositoryId && issueId),
  });

  const commentsQuery = useQuery({
    queryKey: queryKeys.comments(repositoryId ?? '', issueId ?? ''),
    queryFn: () => getComments(repositoryId ?? '', issueId ?? ''),
    enabled: Boolean(repositoryId && issueId),
  });

  const statusMutation = useMutation({
    mutationFn: (state: 'OPEN' | 'CLOSED') => updateIssue(repositoryId ?? '', issueId ?? '', { state }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.issue(repositoryId ?? '', issueId ?? '') });
      await queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
    },
  });

  const refreshCommentsMutation = useMutation({
    mutationFn: () => refreshComments(repositoryId ?? '', issueId ?? ''),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.comments(repositoryId ?? '', issueId ?? '') });
    },
  });

  const createCommentMutation = useMutation({
    mutationFn: ({ content }: { content: string }) =>
      createComment(repositoryId ?? '', issueId ?? '', { body: content }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.comments(repositoryId ?? '', issueId ?? '') });
    },
  });

  const deleteIssueMutation = useMutation({
    mutationFn: () => deleteIssue(repositoryId ?? '', issueId ?? ''),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
      void navigate(`/repositories/${repositoryId}/issues`);
    },
  });

  const actionError =
    statusMutation.error ??
    refreshCommentsMutation.error ??
    createCommentMutation.error ??
    deleteIssueMutation.error;

  if (!repositoryId || !issueId) {
    return <div className="error-banner">경로에 저장소 ID 또는 이슈 번호가 없습니다.</div>;
  }

  return (
    <>
      <PageHeader
        title="이슈 상세"
        description="선택한 GitHub 이슈의 상태와 댓글을 관리합니다."
        actions={
          issueQuery.data ? (
            <button
              className="button button-danger"
              type="button"
              disabled={deleteIssueMutation.isPending}
              onClick={() => deleteIssueMutation.mutate()}
            >
              이슈 닫기
            </button>
          ) : null
        }
      />

      {issueQuery.isLoading || commentsQuery.isLoading ? (
        <div className="info-banner">이슈 상세를 불러오는 중입니다...</div>
      ) : null}
      {issueQuery.isError ? (
        <div className="error-banner">이슈를 불러오지 못했습니다. {getErrorMessage(issueQuery.error)}</div>
      ) : null}
      {commentsQuery.isError ? (
        <div className="error-banner">댓글을 불러오지 못했습니다. {getErrorMessage(commentsQuery.error)}</div>
      ) : null}
      {actionError ? <div className="error-banner">{getErrorMessage(actionError)}</div> : null}

      {issueQuery.data ? (
        <div className="page-stack">
          <section className="form-card">
            <div className="card-header">
              <div>
                <h3 className="section-title">상태 및 댓글 동기화</h3>
                <p className="muted">이슈 상태를 변경하고 댓글 캐시를 새로고침할 수 있습니다.</p>
              </div>
              <div className="toolbar-actions">
                <button
                  className="button"
                  type="button"
                  disabled={refreshCommentsMutation.isPending}
                  onClick={() => refreshCommentsMutation.mutate()}
                >
                  댓글 새로고침
                </button>
              </div>
            </div>
            <div className="filters-grid">
              <IssueStatusControl
                value={issueQuery.data.status}
                disabled={statusMutation.isPending}
                onChange={(nextStatus) => statusMutation.mutate(nextStatus)}
              />
            </div>
          </section>

          <IssueDetailSection repositoryId={repositoryId} issue={issueQuery.data} comments={commentsQuery.data ?? []} />

          <CreateCommentForm
            disabled={createCommentMutation.isPending}
            onSubmit={async ({ content }) => {
              await createCommentMutation.mutateAsync({ content });
            }}
          />
        </div>
      ) : null}
    </>
  );
}
