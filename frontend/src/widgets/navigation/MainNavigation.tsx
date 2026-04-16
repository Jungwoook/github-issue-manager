import { NavLink } from 'react-router-dom';

import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { platformSettingsPath, repositoriesPath } from '@/shared/lib/routes';

const navItems = [
  { to: repositoriesPath(DEFAULT_PLATFORM), label: '저장소' },
  { to: platformSettingsPath(DEFAULT_PLATFORM), label: '플랫폼 연결' },
];

export function MainNavigation() {
  return (
    <nav className="nav-list" aria-label="주요 메뉴">
      {navItems.map((item) => (
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
