import { PageHeader } from '@/shared/ui/PageHeader';
import { GitHubTokenForm } from '@/widgets/github-token/GitHubTokenForm';

export function GitHubTokenPage() {
  return (
    <>
      <PageHeader
        title="GitHub PAT 설정"
        description="GitHub personal access token을 등록하고 현재 연결 상태를 확인합니다."
      />
      <GitHubTokenForm />
    </>
  );
}
