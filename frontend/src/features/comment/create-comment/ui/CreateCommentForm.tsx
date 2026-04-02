import { useState } from 'react';

interface CreateCommentFormProps {
  disabled?: boolean;
  onSubmit: (payload: { content: string }) => Promise<void> | void;
}

export function CreateCommentForm({ disabled, onSubmit }: CreateCommentFormProps) {
  const [content, setContent] = useState('');
  const canSubmit = !disabled && content.trim() !== '';

  return (
    <section className="form-card">
      <div className="card-header">
        <div>
          <h3 className="section-title">댓글 작성</h3>
          <p className="muted">현재 이슈에 남길 내용을 입력합니다.</p>
        </div>
      </div>
      <form
        className="form-stack"
        onSubmit={async (event) => {
          event.preventDefault();

          if (!canSubmit) {
            return;
          }

          await onSubmit({ content: content.trim() });
          setContent('');
        }}
      >
        <div className="field">
          <label htmlFor="comment-content">댓글</label>
          <textarea
            id="comment-content"
            rows={5}
            value={content}
            disabled={disabled}
            placeholder="이슈 진행 상황이나 추가 정보를 입력하세요."
            onChange={(event) => setContent(event.target.value)}
          />
        </div>
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={!canSubmit}>
            댓글 등록
          </button>
        </div>
      </form>
    </section>
  );
}
