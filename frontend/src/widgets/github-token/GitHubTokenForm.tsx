import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  disconnectGitHubToken,
  getGitHubTokenStatus,
  registerGitHubToken,
} from '@/entities/github/api/githubTokenApi';
import { refreshRepositories } from '@/entities/repository/api/repositoryApi';
import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';

export function GitHubTokenForm() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [accessToken, setAccessToken] = useState('');
  const [isTokenVisible, setIsTokenVisible] = useState(false);

  const tokenStatusQuery = useQuery({
    queryKey: queryKeys.platformTokenStatus(DEFAULT_PLATFORM),
    queryFn: getGitHubTokenStatus,
    retry: false,
  });

  const registerMutation = useMutation({
    mutationFn: registerGitHubToken,
    onSuccess: async () => {
      setAccessToken('');
      setIsTokenVisible(false);

      try {
        await refreshRepositories();
      } catch {
        // Keep PAT registration successful even if the first sync fails.
      }

      await queryClient.invalidateQueries({ queryKey: queryKeys.platformTokenStatus(DEFAULT_PLATFORM) });
      await queryClient.invalidateQueries({ queryKey: queryKeys.repositories(DEFAULT_PLATFORM) });
      navigate('/repositories');
    },
  });

  const disconnectMutation = useMutation({
    mutationFn: disconnectGitHubToken,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.platformTokenStatus(DEFAULT_PLATFORM) });
      await queryClient.invalidateQueries({ queryKey: queryKeys.repositories(DEFAULT_PLATFORM) });
    },
  });

  const submitError = registerMutation.error ?? disconnectMutation.error;
  const isSubmitting = registerMutation.isPending || disconnectMutation.isPending;
  const status = tokenStatusQuery.data;

  return (
    <div className="page-stack">
      <section className="form-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">GitHub PAT 연결</h3>
            <p className="muted">
              Fine-grained PAT를 등록하면 본인 계정에서 접근 가능한 저장소와 이슈를 조회하고 수정할 수 있습니다.
            </p>
          </div>
        </div>

        {tokenStatusQuery.isLoading ? <div className="info-banner">현재 연결 상태를 불러오는 중입니다...</div> : null}
        {submitError ? <div className="error-banner">{getErrorMessage(submitError)}</div> : null}
        {registerMutation.isSuccess ? (
          <div className="success-banner">PAT가 등록되었고 저장소 화면으로 이동합니다.</div>
        ) : null}
        {disconnectMutation.isSuccess ? <div className="success-banner">PAT 연결이 해제되었습니다.</div> : null}

        <div className="github-status-panel">
          <div className="github-status-item">
            <span className="muted">연결 상태</span>
            <strong>{status?.connected ? '연결됨' : '연결 안 됨'}</strong>
          </div>
          <div className="github-status-item">
            <span className="muted">GitHub 계정</span>
            <strong>{status?.accountLogin ?? '-'}</strong>
          </div>
          <div className="github-status-item">
            <span className="muted">최근 확인</span>
            <strong>{status?.tokenVerifiedAt ? formatDate(status.tokenVerifiedAt) : '-'}</strong>
          </div>
        </div>

        <form
          className="form-stack"
          onSubmit={(event) => {
            event.preventDefault();
            if (!accessToken.trim()) {
              return;
            }

            registerMutation.mutate({ accessToken: accessToken.trim() });
          }}
        >
          <div className="field">
            <label htmlFor="github-pat">Personal Access Token</label>
            <div className="password-field">
              <input
                id="github-pat"
                type={isTokenVisible ? 'text' : 'password'}
                autoComplete="new-password"
                spellCheck={false}
                value={accessToken}
                disabled={isSubmitting}
                placeholder="github_pat_..."
                onChange={(event) => setAccessToken(event.target.value)}
              />
              <button
                className="button button-ghost"
                type="button"
                disabled={isSubmitting}
                aria-controls="github-pat"
                aria-label={isTokenVisible ? 'Hide personal access token' : 'Show personal access token'}
                aria-pressed={isTokenVisible}
                onClick={() => setIsTokenVisible((current) => !current)}
              >
                {isTokenVisible ? '숨기기' : '보기'}
              </button>
            </div>
            <p className="field-help">
              최소 권한은 저장소 메타데이터 조회와 이슈 읽기/쓰기입니다. 개인 프로젝트 기준으로는 fine-grained PAT를 권장합니다.
            </p>
          </div>

          <div className="form-actions">
            <button
              className="button button-primary"
              type="submit"
              disabled={isSubmitting || accessToken.trim() === ''}
            >
              PAT 등록
            </button>
            <button
              className="button button-danger"
              type="button"
              disabled={isSubmitting || !status?.connected}
              onClick={() => disconnectMutation.mutate()}
            >
              연결 해제
            </button>
          </div>
        </form>
      </section>

      <section className="list-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">발급 가이드</h3>
            <p className="muted">GitHub에서 PAT를 발급할 때는 아래 기준으로 설정하면 됩니다.</p>
          </div>
        </div>
        <div className="page-stack">
          <div className="detail-card">
            <h4 className="card-title">권장 방식</h4>
            <p className="muted">Fine-grained personal access token</p>
          </div>
          <div className="detail-card">
            <h4 className="card-title">필수 권한</h4>
            <p className="muted">Metadata 읽기, Issues 읽기/쓰기</p>
          </div>
          <div className="detail-card">
            <h4 className="card-title">적용 범위</h4>
            <p className="muted">처음에는 관리할 본인 저장소만 선택하는 것을 권장합니다.</p>
          </div>
        </div>
      </section>
    </div>
  );
}
