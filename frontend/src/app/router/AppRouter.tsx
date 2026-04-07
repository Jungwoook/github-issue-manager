import { Navigate, Route, Routes } from 'react-router-dom';

import { RootLayout } from '@/app/layout/RootLayout';
import { IssueCreatePage } from '@/pages/issues/IssueCreatePage';
import { IssueDetailPage } from '@/pages/issues/IssueDetailPage';
import { IssueEditPage } from '@/pages/issues/IssueEditPage';
import { IssueListPage } from '@/pages/issues/IssueListPage';
import { LabelManagementPage } from '@/pages/labels/LabelManagementPage';
import { RepositoryListPage } from '@/pages/repositories/RepositoryListPage';
import { GitHubTokenPage } from '@/pages/settings/GitHubTokenPage';

export function AppRouter() {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        <Route index element={<Navigate to="/repositories" replace />} />
        <Route path="/repositories" element={<RepositoryListPage />} />
        <Route path="/repositories/:repositoryId" element={<IssueListPage />} />
        <Route path="/repositories/:repositoryId/issues" element={<IssueListPage />} />
        <Route path="/repositories/:repositoryId/issues/new" element={<IssueCreatePage />} />
        <Route path="/repositories/:repositoryId/issues/:issueId" element={<IssueDetailPage />} />
        <Route path="/repositories/:repositoryId/issues/:issueId/edit" element={<IssueEditPage />} />
        <Route path="/repositories/:repositoryId/labels" element={<LabelManagementPage />} />
        <Route path="/settings/github" element={<GitHubTokenPage />} />
      </Route>
    </Routes>
  );
}
