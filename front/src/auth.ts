export type AccessRole = 'ADMIN' | 'USER';

export function getStoredRole(): AccessRole | '' {
  const stored = localStorage.getItem('veiculos.role');
  return stored === 'ADMIN' || stored === 'USER' ? stored : '';
}

export function getRoleFromToken(token: string): AccessRole {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) {
      return 'USER';
    }
    const payloadJson = decodeBase64Url(payloadBase64);
    const payload = JSON.parse(payloadJson) as { roles?: string[] };
    return payload.roles?.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
  } catch {
    return 'USER';
  }
}

function decodeBase64Url(value: string) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
  return atob(padded);
}
