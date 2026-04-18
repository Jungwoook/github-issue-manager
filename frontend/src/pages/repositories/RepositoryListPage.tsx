import { useParams } from 'react-router-dom';

import { normalizePlatform } from '@/shared/lib/routes';
import { repositoriesPath } from '@/shared/lib/routes';
import { PageHeader } from '@/shared/ui/PageHeader';
import { PlatformTabs } from '@/shared/ui/PlatformTabs';
import { RepositoryListWidget } from '@/widgets/repository-list/RepositoryListWidget';

export function RepositoryListPage() {
  const { platform } = useParams();
  const currentPlatform = normalizePlatform(platform);
  const platformLabel = currentPlatform.toUpperCase();

  return (
    <>
      <PageHeader
        title="저장소"
        description={`${platformLabel} 연결 기준으로 접근 가능한 저장소를 불러오고, 선택한 저장소의 이슈를 관리합니다.`}
        actions={<PlatformTabs currentPlatform={currentPlatform} to={repositoriesPath} />}
      />
      <RepositoryListWidget />
    </>
  );
}
