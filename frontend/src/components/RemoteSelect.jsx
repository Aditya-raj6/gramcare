import { Select } from 'antd';
import { useEffect, useState } from 'react';
import api from '../api/client';

const cache = new Map();

/** Loads options from an API list endpoint (cached briefly) and renders a searchable select. */
export function useRemoteOptions(endpoint, labelOf, params) {
  const [items, setItems] = useState([]);
  const key = endpoint + JSON.stringify(params || {});
  useEffect(() => {
    let alive = true;
    const hit = cache.get(key);
    if (hit && Date.now() - hit.at < 15000) {
      setItems(hit.data);
      return undefined;
    }
    api.get(endpoint, { params }).then(({ data }) => {
      cache.set(key, { at: Date.now(), data });
      if (alive) setItems(data);
    }).catch(() => alive && setItems([]));
    return () => {
      alive = false;
    };
  }, [key]); // eslint-disable-line react-hooks/exhaustive-deps
  return items.map((it) => ({ value: it.id, label: labelOf ? labelOf(it) : it.name, item: it }));
}

export function invalidateRemote() {
  cache.clear();
}

export default function RemoteSelect({ endpoint, labelOf, params, onPick, value, onChange, ...rest }) {
  const options = useRemoteOptions(endpoint, labelOf, params);
  return (
    <Select
      showSearch
      allowClear
      optionFilterProp="label"
      options={options}
      value={value}
      onChange={(v, opt) => {
        onChange?.(v);
        onPick?.(opt?.item || null);
      }}
      {...rest}
    />
  );
}
