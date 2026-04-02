import { CreateIssueButton } from '@/features/issue/create-issue/ui/CreateIssueButton';
import { PageHeader } from '@/shared/ui/PageHeader';
import { IssueListWidget } from '@/widgets/issue-list/IssueListWidget';

export function IssueListPage() {
  return (
    <>
      <PageHeader
        title="이슈"
        description="저장소의 GitHub 이슈 목록을 조회하고, 새 이슈를 생성할 수 있습니다."
        actions={<CreateIssueButton />}
      />
      <IssueListWidget />
    </>
  );
}
