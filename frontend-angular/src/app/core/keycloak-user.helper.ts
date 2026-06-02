import { KeycloakService } from 'keycloak-angular';

/** Nome exibido a partir do JWT (evita CORS em /realms/.../account). */
export function nomeExibicaoFromToken(keycloak: KeycloakService): string {
  const parsed = keycloak.getKeycloakInstance()?.tokenParsed as Record<string, unknown> | undefined;
  if (!parsed) {
    return 'Usuário';
  }
  const given = parsed['given_name'] as string | undefined;
  const family = parsed['family_name'] as string | undefined;
  if (given) {
    return family ? `${given} ${family}`.trim() : given;
  }
  const preferred = parsed['preferred_username'] as string | undefined;
  if (preferred) {
    return preferred;
  }
  const name = parsed['name'] as string | undefined;
  if (name) {
    return name;
  }
  const sub = parsed['sub'] as string | undefined;
  return sub || 'Usuário';
}

export function perfilFromToken(keycloak: KeycloakService): string {
  const roles = keycloak.getUserRoles(true);
  const filtradas = roles.filter(r => r === 'ADMIN' || r === 'FISCAL');
  return filtradas.length ? filtradas.join(', ') : 'FISCAL';
}
