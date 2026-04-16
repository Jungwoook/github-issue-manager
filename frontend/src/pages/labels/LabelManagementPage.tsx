import { Link, useParams } from 'react-router-dom';

import { repositoryIssuesPath } from '@/shared/lib/routes';
import { PageHeader } from '@/shared/ui/PageHeader';

export function LabelManagementPage() {
  const { platform, repositoryId } = useParams();

  if (!repositoryId) {
    return <div className="error-banner">경로에 저장소 ID가 없습니다.</div>;
  }

  return (
    <>
      <PageHeader
        title="라벨"
        description="현재 최소 구현 범위에서는 라벨 관리 기능을 제공하지 않습니다."
        actions={
          <Link className="button" to={repositoryIssuesPath(repositoryId, platform)}>
            이슈 목록으로
          </Link>
        }
      />

      <section className="list-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">지원 예정 기능</h3>
            <p className="muted">라벨 조회와 수정 API가 준비되면 같은 플랫폼 구조 안에서 확장할 수 있습니다.</p>
          </div>
        </div>
        <div className="empty-state">
          라벨 기능을 지원하려면 저장소 기준 라벨 조회 및 수정 API를 백엔드에 추가해야 합니다.
        </div>
      </section>
    </>
  );
}
