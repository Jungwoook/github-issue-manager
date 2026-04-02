import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/repositories', label: '저장소' },
  { to: '/settings/github', label: 'GitHub PAT' },
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
