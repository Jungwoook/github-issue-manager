import { Link, useParams } from 'react-router-dom';

import { PageHeader } from '@/shared/ui/PageHeader';

export function LabelManagementPage() {
  const { repositoryId } = useParams();

  if (!repositoryId) {
    return <div className="error-banner">경로에 저장소 ID가 없습니다.</div>;
  }

  return (
    <>
      <PageHeader
        title="라벨"
        description="현재 PAT 기반 최소 구현 범위에서는 라벨 관리를 지원하지 않습니다."
        actions={
          <Link className="button" to={`/repositories/${repositoryId}/issues`}>
            이슈 목록으로
          </Link>
        }
      />

      <section className="list-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">지원 예정 기능</h3>
            <p className="muted">현재 백엔드에는 GitHub 라벨 조회 및 수정 엔드포인트가 없어 이 화면은 안내용으로 유지됩니다.</p>
          </div>
        </div>
        <div className="empty-state">
          라벨 기능을 사용하려면 백엔드에 `/api/repositories/{'{repositoryId}'}/labels` 계열 API를 추가해야 합니다.
        </div>
      </section>
    </>
  );
}
