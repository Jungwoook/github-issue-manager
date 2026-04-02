import { useEffect, useState } from 'react';

export interface IssueFormValues {
  title: string;
  content: string;
}

interface IssueFormProps {
  title: string;
  description: string;
  submitLabel: string;
  initialValues: IssueFormValues;
  errorMessage?: string | null;
  isSubmitting?: boolean;
  onSubmit: (values: IssueFormValues) => void;
}

export function IssueForm({
  title,
  description,
  submitLabel,
  initialValues,
  errorMessage,
  isSubmitting,
  onSubmit,
}: IssueFormProps) {
  const [values, setValues] = useState(initialValues);

  useEffect(() => {
    setValues(initialValues);
  }, [initialValues]);

  return (
    <section className="form-card">
      <div className="card-header">
        <div>
          <h3 className="section-title">{title}</h3>
          <p className="muted">{description}</p>
        </div>
      </div>

      {errorMessage ? <div className="error-banner">{errorMessage}</div> : null}

      <form
        className="form-stack"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit({
            title: values.title.trim(),
            content: values.content.trim(),
          });
        }}
      >
        <div className="field">
          <label htmlFor="issue-title">제목</label>
          <input
            id="issue-title"
            value={values.title}
            maxLength={200}
            disabled={isSubmitting}
            placeholder="이슈를 쉽게 이해할 수 있는 제목을 입력하세요."
            onChange={(event) => setValues((current) => ({ ...current, title: event.target.value }))}
          />
        </div>

        <div className="field">
          <label htmlFor="issue-content">설명</label>
          <textarea
            id="issue-content"
            value={values.content}
            disabled={isSubmitting}
            placeholder="배경, 현재 상태, 기대 결과 등을 입력하세요."
            onChange={(event) => setValues((current) => ({ ...current, content: event.target.value }))}
          />
        </div>

        <div className="form-actions">
          <button
            className="button button-primary"
            type="submit"
            disabled={isSubmitting || values.title.trim() === ''}
          >
            {submitLabel}
          </button>
        </div>
      </form>
    </section>
  );
}
