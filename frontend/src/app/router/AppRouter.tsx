import { Navigate, Route, Routes, useParams } from 'react-router-dom';

import { RootLayout } from '@/app/layout/RootLayout';
import { IssueCreatePage } from '@/pages/issues/IssueCreatePage';
import { IssueDetailPage } from '@/pages/issues/IssueDetailPage';
import { IssueEditPage } from '@/pages/issues/IssueEditPage';
import { IssueListPage } from '@/pages/issues/IssueListPage';
import { LabelManagementPage } from '@/pages/labels/LabelManagementPage';
import { RepositoryListPage } from '@/pages/repositories/RepositoryListPage';
import { PlatformConnectionPage } from '@/pages/settings/PlatformConnectionPage';
import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import {
  platformSettingsPath,
  repositoriesPath,
  repositoryIssueEditPath,
  repositoryIssueNewPath,
  repositoryIssuePath,
  repositoryIssuesPath,
  repositoryLabelsPath,
} from '@/shared/lib/routes';

export function AppRouter() {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        <Route index element={<Navigate to={repositoriesPath(DEFAULT_PLATFORM)} replace />} />

        <Route path="/platforms/:platform/repositories" element={<RepositoryListPage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId" element={<IssueListPage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId/issues" element={<IssueListPage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId/issues/new" element={<IssueCreatePage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId/issues/:issueId" element={<IssueDetailPage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId/issues/:issueId/edit" element={<IssueEditPage />} />
        <Route path="/platforms/:platform/repositories/:repositoryId/labels" element={<LabelManagementPage />} />
        <Route path="/settings/platforms/:platform" element={<PlatformConnectionPage />} />

        <Route path="/repositories" element={<Navigate to={repositoriesPath(DEFAULT_PLATFORM)} replace />} />
        <Route
          path="/repositories/:repositoryId"
          element={<LegacyRouteRedirect to={(repositoryId) => repositoryIssuesPath(repositoryId, DEFAULT_PLATFORM)} />}
        />
        <Route
          path="/repositories/:repositoryId/issues"
          element={<LegacyRouteRedirect to={(repositoryId) => repositoryIssuesPath(repositoryId, DEFAULT_PLATFORM)} />}
        />
        <Route
          path="/repositories/:repositoryId/issues/new"
          element={<LegacyRouteRedirect to={(repositoryId) => repositoryIssueNewPath(repositoryId, DEFAULT_PLATFORM)} />}
        />
        <Route
          path="/repositories/:repositoryId/issues/:issueId"
          element={<LegacyIssueRouteRedirect to={(repositoryId, issueId) => repositoryIssuePath(repositoryId, issueId, DEFAULT_PLATFORM)} />}
        />
        <Route
          path="/repositories/:repositoryId/issues/:issueId/edit"
          element={<LegacyIssueRouteRedirect to={(repositoryId, issueId) => repositoryIssueEditPath(repositoryId, issueId, DEFAULT_PLATFORM)} />}
        />
        <Route
          path="/repositories/:repositoryId/labels"
          element={<LegacyRouteRedirect to={(repositoryId) => repositoryLabelsPath(repositoryId, DEFAULT_PLATFORM)} />}
        />
        <Route path="/settings/github" element={<Navigate to={platformSettingsPath(DEFAULT_PLATFORM)} replace />} />
      </Route>
    </Routes>
  );
}

function LegacyRouteRedirect({ to }: { to: (repositoryId: string) => string }) {
  const { repositoryId } = useParams();

  if (!repositoryId) {
    return <Navigate to={repositoriesPath(DEFAULT_PLATFORM)} replace />;
  }

  return <Navigate to={to(repositoryId)} replace />;
}

function LegacyIssueRouteRedirect({ to }: { to: (repositoryId: string, issueId: string) => string }) {
  const { repositoryId, issueId } = useParams();

  if (!repositoryId || !issueId) {
    return <Navigate to={repositoriesPath(DEFAULT_PLATFORM)} replace />;
  }

  return <Navigate to={to(repositoryId, issueId)} replace />;
}
