import { useNavigate, useParams } from 'react-router-dom';

import { useMutation, useQueryClient } from '@tanstack/react-query';

import { createIssue } from '@/entities/issue/api/issueApi';
import { queryKeys } from '@/shared/constants/queryKeys';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';
import { PageHeader } from '@/shared/ui/PageHeader';
import { IssueForm, type IssueFormValues } from '@/widgets/forms/IssueForm';

export function IssueCreatePage() {
  const navigate = useNavigate();
  const { repositoryId } = useParams();
  const queryClient = useQueryClient();

  const createIssueMutation = useMutation({
    mutationFn: (values: IssueFormValues) =>
      createIssue(repositoryId ?? '', {
        title: values.title,
        body: values.content || undefined,
      } as never),
    onSuccess: (issue: { number: number }) => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.issuesRoot(repositoryId ?? '') });
      void navigate(`/repositories/${repositoryId}/issues/${issue.number}`);
    },
  });

  if (!repositoryId) {
    return <div className="error-banner">경로에 저장소 ID가 없습니다.</div>;
  }

  return (
    <>
      <PageHeader title="이슈 생성" description="선택한 저장소에 새 GitHub 이슈를 등록합니다." />
      <IssueForm
        title="새 이슈"
        description="제목과 설명을 입력하면 GitHub 이슈가 생성됩니다."
        submitLabel="이슈 생성"
        initialValues={{
          title: '',
          content: '',
        }}
        errorMessage={createIssueMutation.isError ? getErrorMessage(createIssueMutation.error) : null}
        isSubmitting={createIssueMutation.isPending}
        onSubmit={(values) => createIssueMutation.mutate(values)}
      />
    </>
  );
}
