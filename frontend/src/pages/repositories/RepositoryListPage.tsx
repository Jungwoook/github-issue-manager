import { PageHeader } from '@/shared/ui/PageHeader';
import { RepositoryListWidget } from '@/widgets/repository-list/RepositoryListWidget';

export function RepositoryListPage() {
  return (
    <>
      <PageHeader
        title="저장소"
        description="GitHub PAT로 접근 가능한 저장소를 불러오고, 선택한 저장소의 이슈를 관리합니다."
      />
      <RepositoryListWidget />
    </>
  );
}
