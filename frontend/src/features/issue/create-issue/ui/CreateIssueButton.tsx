import { Link, useParams } from 'react-router-dom';

export function CreateIssueButton() {
  const { repositoryId } = useParams();

  if (!repositoryId) {
    return null;
  }

  return (
    <Link className="button button-primary" to={`/repositories/${repositoryId}/issues/new`}>
      새 이슈
    </Link>
  );
}
