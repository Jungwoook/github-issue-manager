import { useMemo, useState } from 'react';

import type { Label } from '@/entities/label/model/types';

interface AttachLabelControlProps {
  labels: Label[];
  attachedLabelIds: number[];
  disabled?: boolean;
  onAttach: (labelId: number) => void;
}

export function AttachLabelControl({
  labels,
  attachedLabelIds,
  disabled,
  onAttach,
}: AttachLabelControlProps) {
  const availableLabels = useMemo(
    () => labels.filter((label) => !attachedLabelIds.includes(label.id)),
    [attachedLabelIds, labels],
  );
  const [selectedLabelId, setSelectedLabelId] = useState('');

  const canAttach = !disabled && selectedLabelId !== '';

  return (
    <div className="field" style={{ minWidth: 240 }}>
      <label htmlFor="attach-label">라벨 연결</label>
      <div className="inline-actions">
        <select
          id="attach-label"
          value={selectedLabelId}
          disabled={disabled || availableLabels.length === 0}
          onChange={(event) => setSelectedLabelId(event.target.value)}
        >
          <option value="">
            {availableLabels.length === 0 ? '연결 가능한 라벨이 없습니다' : '라벨을 선택해 주세요'}
          </option>
          {availableLabels.map((label) => (
            <option key={label.id} value={label.id}>
              {label.name}
            </option>
          ))}
        </select>
        <button
          className="button"
          type="button"
          disabled={!canAttach}
          onClick={() => {
            onAttach(Number(selectedLabelId));
            setSelectedLabelId('');
          }}
        >
          연결
        </button>
      </div>
    </div>
  );
}
