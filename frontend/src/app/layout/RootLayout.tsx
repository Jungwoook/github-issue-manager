import { Outlet, useParams } from 'react-router-dom';

import { normalizePlatform } from '@/shared/lib/routes';
import { MainNavigation } from '@/widgets/navigation/MainNavigation';
import { PlatformConnectionStatus } from '@/widgets/platform-connection/PlatformConnectionStatus';

export function RootLayout() {
  const { platform } = useParams();
  const currentPlatform = normalizePlatform(platform);

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <h1>이슈 관리자</h1>
        <p className="muted">플랫폼별 저장소, 이슈, 댓글을 한 화면에서 관리합니다.</p>
        <MainNavigation platform={currentPlatform} />
        <PlatformConnectionStatus platform={currentPlatform} />
      </aside>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
