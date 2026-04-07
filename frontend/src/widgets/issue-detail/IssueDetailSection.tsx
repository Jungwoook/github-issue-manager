import { Link } from 'react-router-dom';

import type { Comment } from '@/entities/comment/model/types';
import type { IssueDetail } from '@/entities/issue/model/types';
import { IssueMetaTags } from '@/entities/issue/ui/IssueMetaTags';
import { formatDate } from '@/shared/lib/formatDate';

interface IssueDetailSectionProps {
  repositoryId: string;
  issue: IssueDetail;
  comments: Comment[];
}

export function IssueDetailSection({ repositoryId, issue, comments }: IssueDetailSectionProps) {
  return (
    <div className="detail-stack">
      <section className="detail-card">
        <div className="card-header">
          <div className="stack-sm">
            <h3 className="section-title">{issue.title}</h3>
            <span className="muted">이슈 #{issue.number} 생성일 {formatDate(issue.createdAt)}</span>
          </div>
          <div className="toolbar-actions">
            <Link className="button" to={`/repositories/${repositoryId}/issues/${issue.number}/edit`}>
              이슈 수정
            </Link>
          </div>
        </div>

        <div className="stack-sm">
          <IssueMetaTags status={issue.status} />
          <div className="meta-list">
            <span className="muted">작성자: {issue.authorLogin ?? '-'}</span>
            <span className="muted">최종 수정: {formatDate(issue.updatedAt)}</span>
          </div>
        </div>

        <div style={{ marginTop: 20 }}>
          <h4 className="section-title">설명</h4>
          <p className="detail-body">{issue.body?.trim() || '설명이 없습니다.'}</p>
        </div>
      </section>

      <section className="detail-card">
        <div className="card-header">
          <div>
            <h3 className="section-title">댓글</h3>
            <p className="muted">GitHub 이슈 댓글 목록입니다.</p>
          </div>
        </div>

        {comments.length === 0 ? (
          <div className="empty-state">댓글이 없습니다. 아래에서 첫 댓글을 작성할 수 있습니다.</div>
        ) : (
          <div className="comment-list">
            {comments.map((comment) => (
              <article key={comment.githubCommentId} className="comment-card">
                <div className="row-between">
                  <div className="stack-sm">
                    <strong>{comment.authorLogin}</strong>
                    <span className="muted">{formatDate(comment.createdAt)}</span>
                  </div>
                </div>
                <p>{comment.body}</p>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
