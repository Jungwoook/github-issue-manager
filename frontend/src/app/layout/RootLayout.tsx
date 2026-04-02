import { Outlet } from 'react-router-dom';

import { GitHubConnectionStatus } from '@/widgets/github-token/GitHubConnectionStatus';
import { MainNavigation } from '@/widgets/navigation/MainNavigation';

export function RootLayout() {
  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <h1>이슈 관리자</h1>
        <p className="muted">저장소, 이슈, 댓글을 한 화면에서 관리합니다.</p>
        <MainNavigation />
        <GitHubConnectionStatus />
      </aside>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
