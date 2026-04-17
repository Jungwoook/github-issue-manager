import { NavLink } from 'react-router-dom';

import { PLATFORM_METADATA, SUPPORTED_PLATFORMS } from '@/shared/constants/platform';

export function PlatformTabs({
  currentPlatform,
  to,
}: {
  currentPlatform: string;
  to: (platform: string) => string;
}) {
  return (
    <div className="platform-tabs" role="tablist" aria-label="플랫폼 선택">
      {SUPPORTED_PLATFORMS.map((platform) => (
        <NavLink
          key={platform}
          to={to(platform)}
          role="tab"
          aria-selected={currentPlatform === platform}
          className={({ isActive }) => `platform-tab${isActive ? ' active' : ''}`}
        >
          {PLATFORM_METADATA[platform].label}
        </NavLink>
      ))}
    </div>
  );
}
