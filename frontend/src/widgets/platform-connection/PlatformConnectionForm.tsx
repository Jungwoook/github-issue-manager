import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  disconnectPlatformToken,
  getPlatformTokenStatus,
  registerPlatformToken,
} from '@/entities/platform-connection/api/platformConnectionApi';
import { refreshRepositories } from '@/entities/repository/api/repositoryApi';
import { DEFAULT_PLATFORM, PLATFORM_METADATA } from '@/shared/constants/platform';
import { queryKeys } from '@/shared/constants/queryKeys';
import { formatDate } from '@/shared/lib/formatDate';
import { getErrorMessage } from '@/shared/lib/getErrorMessage';
import { platformSettingsPath, repositoriesPath } from '@/shared/lib/routes';
import { PlatformTabs } from '@/shared/ui/PlatformTabs';

export function PlatformConnectionForm({ platform = DEFAULT_PLATFORM }: { platform?: string }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [accessToken, setAccessToken] = useState('');
  const [baseUrl, setBaseUrl] = useState('');
  const [isTokenVisible, setIsTokenVisible] = useState(false);
  const platformMeta = PLATFORM_METADATA[platform as keyof typeof PLATFORM_METADATA] ?? PLATFORM_METADATA.github;

  const tokenStatusQuery = useQuery({
    queryKey: queryKeys.platformTokenStatus(platform),
    queryFn: () => getPlatformTokenStatus(platform),
    retry: false,
  });

  const registerMutation = useMutation({
    mutationFn: (payload: { accessToken: string; baseUrl?: string | null }) => registerPlatformToken(payload, platform),
    onSuccess: async () => {
      setAccessToken('');
      setBaseUrl('');
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

  useEffect(() => {
    if (platformMeta.supportsCustomBaseUrl) {
      setBaseUrl(status?.baseUrl ?? '');
      return;
    }

    setBaseUrl('');
  }, [platformMeta.supportsCustomBaseUrl, status?.baseUrl]);

  return (
    <div className="page-stack">
      <section className="form-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">플랫폼 토큰 연결</h3>
            <p className="muted">{platformMeta.tokenHelp}</p>
          </div>
        </div>

        <PlatformTabs currentPlatform={platform} to={platformSettingsPath} />

        {tokenStatusQuery.isLoading ? <div className="info-banner">현재 연결 상태를 불러오는 중입니다...</div> : null}
        {submitError ? <div className="error-banner">{getErrorMessage(submitError)}</div> : null}
        {registerMutation.isSuccess ? <div className="success-banner">토큰을 등록하고 저장소 화면으로 이동합니다.</div> : null}
        {disconnectMutation.isSuccess ? <div className="success-banner">토큰 연결을 해제했습니다.</div> : null}

        <div className="github-status-panel">
          <div className="github-status-item">
            <span className="muted">플랫폼</span>
            <strong>{platformMeta.label}</strong>
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
          <div className="github-status-item">
            <span className="muted">Base URL</span>
            <strong>{status?.baseUrl ?? platformMeta.defaultBaseUrl ?? '-'}</strong>
          </div>
        </div>

        <form
          className="form-stack"
          onSubmit={(event) => {
            event.preventDefault();
            if (!accessToken.trim()) {
              return;
            }

            registerMutation.mutate({
              accessToken: accessToken.trim(),
              baseUrl: platformMeta.supportsCustomBaseUrl ? baseUrl.trim() || null : null,
            });
          }}
        >
          <div className="field">
            <label htmlFor="platform-pat">{platformMeta.tokenLabel}</label>
            <div className="password-field">
              <input
                id="platform-pat"
                type={isTokenVisible ? 'text' : 'password'}
                autoComplete="new-password"
                spellCheck={false}
                value={accessToken}
                disabled={isSubmitting}
                placeholder={platformMeta.tokenPlaceholder}
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
            <p className="field-help">{platformMeta.tokenHelp}</p>
          </div>

          {platformMeta.supportsCustomBaseUrl ? (
            <div className="field">
              <label htmlFor="platform-base-url">Base URL</label>
              <input
                id="platform-base-url"
                type="url"
                autoComplete="url"
                spellCheck={false}
                value={baseUrl}
                disabled={isSubmitting}
                placeholder={platformMeta.defaultBaseUrl}
                onChange={(event) => setBaseUrl(event.target.value)}
              />
              <p className="field-help">
                GitLab self-managed 인스턴스를 쓰는 경우에만 API base URL을 입력합니다. 비워두면 {platformMeta.defaultBaseUrl}
                를 사용합니다.
              </p>
            </div>
          ) : null}

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
    </div>
  );
}
