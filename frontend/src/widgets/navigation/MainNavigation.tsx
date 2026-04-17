import { NavLink } from 'react-router-dom';

import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { platformSettingsPath, repositoriesPath } from '@/shared/lib/routes';

function getNavItems(platform: string) {
  return [
    { to: repositoriesPath(platform), label: '저장소' },
    { to: platformSettingsPath(platform), label: '플랫폼 연결' },
  ];
}

export function MainNavigation({ platform = DEFAULT_PLATFORM }: { platform?: string }) {
  return (
    <nav className="nav-list" aria-label="주요 메뉴">
      {getNavItems(platform).map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  );
}
