import { PageHeader } from '@/shared/ui/PageHeader';
import { FormPlaceholder } from '@/widgets/forms/FormPlaceholder';

interface UserFormPageProps {
  mode: 'create' | 'edit';
}

export function UserFormPage({ mode }: UserFormPageProps) {
  const title = mode === 'create' ? '사용자 생성' : '사용자 수정';
  const description =
    mode === 'create'
      ? 'username, displayName, email, role 입력 폼이 들어갈 예정입니다.'
      : 'displayName, email, role 수정 폼이 들어갈 예정입니다.';

  return (
    <>
      <PageHeader title={title} description={description} />
      <FormPlaceholder title="사용자 폼" description="생성과 수정을 재사용 가능한 폼으로 분리할 예정입니다." />
    </>
  );
}
