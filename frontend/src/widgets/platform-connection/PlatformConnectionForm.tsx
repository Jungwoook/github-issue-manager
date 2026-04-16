import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  disconnectPlatformToken,
  getPlatformTokenStatus,
  registerPlatformToken,
} from '@/entities/platform-connection/api/platformConnectionApi';
import { refreshRepositories } from '@/entities/repository/api/repositoryApi';
import { DEFAULT_PLATFORM } from '@/shared/constants/platform';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';
import { repositoriesPath } from '@/shared/lib/routes';

export function PlatformConnectionForm({ platform = DEFAULT_PLATFORM }: { platform?: string }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [accessToken, setAccessToken] = useState('');
  const [isTokenVisible, setIsTokenVisible] = useState(false);

  const tokenStatusQuery = useQuery({
    queryKey: queryKeys.platformTokenStatus(platform),
    queryFn: () => getPlatformTokenStatus(platform),
    retry: false,
  });

  const registerMutation = useMutation({
    mutationFn: (payload: { accessToken: string }) => registerPlatformToken(payload, platform),
    onSuccess: async () => {
      setAccessToken('');
      setIsTokenVisible(false);

      try {
        await refreshRepositories(platform);
      } catch {
        // Keep token registration successful even if the first sync fails.
      }

      await queryClient.invalidateQueries({ queryKey: queryKeys.platformTokenStatus(platform) });
      await queryClient.invalidateQueries({ queryKey: queryKeys.repositories(platform) });
      navigate(repositoriesPath(platform));
    },
  });

  const disconnectMutation = useMutation({
    mutationFn: () => disconnectPlatformToken(platform),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.platformTokenStatus(platform) });
      await queryClient.invalidateQueries({ queryKey: queryKeys.repositories(platform) });
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
            <h3 className="section-title">플랫폼 토큰 연결</h3>
            <p className="muted">현재는 GitHub fine-grained PAT를 등록해 저장소와 이슈를 관리할 수 있습니다.</p>
          </div>
        </div>

        {tokenStatusQuery.isLoading ? <div className="info-banner">현재 연결 상태를 불러오는 중입니다...</div> : null}
        {submitError ? <div className="error-banner">{getErrorMessage(submitError)}</div> : null}
        {registerMutation.isSuccess ? <div className="success-banner">토큰을 등록했고 저장소 화면으로 이동합니다.</div> : null}
        {disconnectMutation.isSuccess ? <div className="success-banner">토큰 연결을 해제했습니다.</div> : null}

        <div className="github-status-panel">
          <div className="github-status-item">
            <span className="muted">플랫폼</span>
            <strong>{platform.toUpperCase()}</strong>
          </div>
          <div className="github-status-item">
            <span className="muted">연결 상태</span>
            <strong>{status?.connected ? '연결됨' : '연결 필요'}</strong>
          </div>
          <div className="github-status-item">
            <span className="muted">계정</span>
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
            <label htmlFor="platform-pat">Personal Access Token</label>
            <div className="password-field">
              <input
                id="platform-pat"
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
                aria-controls="platform-pat"
                aria-label={isTokenVisible ? 'Hide personal access token' : 'Show personal access token'}
                aria-pressed={isTokenVisible}
                onClick={() => setIsTokenVisible((current) => !current)}
              >
                {isTokenVisible ? '숨기기' : '보기'}
              </button>
            </div>
            <p className="field-help">
              현재는 GitHub PAT를 기준으로 동작합니다. 이후 다른 플랫폼도 같은 구조에 맞춰 확장합니다.
            </p>
          </div>

          <div className="form-actions">
            <button
              className="button button-primary"
              type="submit"
              disabled={isSubmitting || accessToken.trim() === ''}
            >
              토큰 등록
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
            <h3 className="section-title">현재 안내</h3>
            <p className="muted">2차 리팩토링 단계에서는 구조를 공통화하고 실제 연결은 GitHub 기준으로 유지합니다.</p>
          </div>
        </div>
        <div className="page-stack">
          <div className="detail-card">
            <h4 className="card-title">지원 플랫폼</h4>
            <p className="muted">현재: GitHub</p>
          </div>
          <div className="detail-card">
            <h4 className="card-title">구조 목표</h4>
            <p className="muted">다른 플랫폼도 같은 화면 구조로 연결 가능하도록 정리</p>
          </div>
          <div className="detail-card">
            <h4 className="card-title">다음 단계</h4>
            <p className="muted">플랫폼 선택 UI와 추가 플랫폼 연결 화면 확장</p>
          </div>
        </div>
      </section>
    </div>
  );
}
