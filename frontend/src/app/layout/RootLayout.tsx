import { Outlet } from 'react-router-dom';

import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { MainNavigation } from '@/widgets/navigation/MainNavigation';
import { PlatformConnectionStatus } from '@/widgets/platform-connection/PlatformConnectionStatus';

export function RootLayout() {
  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <h1>이슈 관리자</h1>
        <p className="muted">플랫폼별 저장소, 이슈, 댓글을 한 화면에서 관리합니다.</p>
        <MainNavigation />
        <PlatformConnectionStatus platform={DEFAULT_PLATFORM} />
      </aside>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
