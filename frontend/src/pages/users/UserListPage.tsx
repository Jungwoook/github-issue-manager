import { PageHeader } from '@/shared/ui/PageHeader';
import { PlaceholderCard } from '@/shared/ui/PlaceholderCard';

export function UserListPage() {
  return (
    <>
      <PageHeader title="사용자" description="사용자 목록과 역할 필터, 생성/수정/삭제 진입점입니다." />
      <PlaceholderCard
        title="사용자 목록"
        description="사용자 테이블, 검색 입력, 역할 필터, 삭제 확인 흐름이 들어갈 예정입니다."
      />
    </>
  );
}
