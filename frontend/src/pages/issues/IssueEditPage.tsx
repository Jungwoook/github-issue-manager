import { useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { getIssueDetail, updateIssue } from '@/entities/issue/api/issueApi';
import { queryKeys } from '@/shared/constants/queryKeys';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';
import { PageHeader } from '@/shared/ui/PageHeader';
import { IssueForm, type IssueFormValues } from '@/widgets/forms/IssueForm';

export function IssueEditPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { repositoryId, issueId } = useParams();

  const issueQuery = useQuery({
    queryKey: queryKeys.issue(repositoryId ?? '', issueId ?? ''),
    queryFn: () => getIssueDetail(repositoryId ?? '', issueId ?? ''),
    enabled: Boolean(repositoryId && issueId),
  });

  const updateIssueMutation = useMutation({
    mutationFn: (values: IssueFormValues) =>
      updateIssue(repositoryId ?? '', issueId ?? '', {
        title: values.title,
        body: values.content || undefined,
      } as never),
    onSuccess: (issue: { number: number }) => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.issue(repositoryId ?? '', issue.number) });
      void queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
      void navigate(`/repositories/${repositoryId}/issues/${issue.number}`);
    },
  });

  const initialValues = useMemo(
    () => ({
      title: issueQuery.data?.title ?? '',
      content: (issueQuery.data as { body?: string | null } | undefined)?.body ?? '',
    }),
    [issueQuery.data],
  );

  if (!repositoryId || !issueId) {
    return <div className="error-banner">경로에 저장소 ID 또는 이슈 번호가 없습니다.</div>;
  }

  return (
    <>
      <PageHeader title="이슈 수정" description="GitHub 이슈의 제목과 설명을 수정합니다." />
      {issueQuery.isLoading ? <div className="info-banner">이슈 정보를 불러오는 중입니다...</div> : null}
      {issueQuery.isError ? (
        <div className="error-banner">이슈를 불러오지 못했습니다. {getErrorMessage(issueQuery.error)}</div>
      ) : null}
      {issueQuery.data ? (
        <IssueForm
          title="이슈 수정"
          description="변경한 내용은 GitHub 이슈에 반영됩니다."
          submitLabel="변경 사항 저장"
          initialValues={initialValues}
          errorMessage={updateIssueMutation.isError ? getErrorMessage(updateIssueMutation.error) : null}
          isSubmitting={updateIssueMutation.isPending}
          onSubmit={(values) => updateIssueMutation.mutate(values)}
        />
      ) : null}
    </>
  );
}
