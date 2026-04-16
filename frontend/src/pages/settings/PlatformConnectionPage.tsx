import { useParams } from 'react-router-dom';

import { normalizePlatform } from '@/shared/lib/routes';
import { PageHeader } from '@/shared/ui/PageHeader';
import { PlatformConnectionForm } from '@/widgets/platform-connection/PlatformConnectionForm';

export function PlatformConnectionPage() {
  const { platform } = useParams();
  const currentPlatform = normalizePlatform(platform);

  return (
    <>
      <PageHeader
        title="플랫폼 연결 설정"
        description="플랫폼 계정 토큰을 등록하고 현재 연결 상태를 확인합니다."
      />
      <PlatformConnectionForm platform={currentPlatform} />
    </>
  );
}
