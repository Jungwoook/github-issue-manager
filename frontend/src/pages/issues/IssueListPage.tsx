import { useParams } from 'react-router-dom';

import { CreateIssueButton } from '@/features/issue/create-issue/ui/CreateIssueButton';
import { normalizePlatform } from '@/shared/lib/routes';
import { PageHeader } from '@/shared/ui/PageHeader';
import { IssueListWidget } from '@/widgets/issue-list/IssueListWidget';

export function IssueListPage() {
  const { platform } = useParams();
  const currentPlatform = normalizePlatform(platform);
  const platformLabel = currentPlatform.toUpperCase();

  return (
    <>
      <PageHeader
        title="이슈"
        description={`${platformLabel} 저장소의 이슈 목록을 조회하고, 새 이슈를 등록합니다.`}
        actions={<CreateIssueButton />}
      />
      <IssueListWidget />
    </>
  );
}
