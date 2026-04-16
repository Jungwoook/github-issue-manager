import { Link, useParams } from 'react-router-dom';

import { normalizePlatform, repositoryIssueNewPath } from '@/shared/lib/routes';

export function CreateIssueButton() {
  const { platform, repositoryId } = useParams();
  const currentPlatform = normalizePlatform(platform);

  if (!repositoryId) {
    return null;
  }

  return (
    <Link className="button button-primary" to={repositoryIssueNewPath(repositoryId, currentPlatform)}>
      새 이슈
    </Link>
  );
}
