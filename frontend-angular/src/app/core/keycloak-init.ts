import { KeycloakService } from 'keycloak-angular';
import { environment } from '../../environments/environment';

const ROTAS_ANGULAR = new Set([
  '/dashboard-angular',
  '/apuracao-fiscal',
  '/historico'
]);

/** Preserva a rota Angular ao voltar do login (ex.: vindo do menu JSF). */
const REDIRECT_URI = () => {
  const path = window.location.pathname || '/dashboard-angular';
  const destino = ROTAS_ANGULAR.has(path) ? path : '/dashboard-angular';
  return `${window.location.origin}${destino}`;
};

function limparFragmentoKeycloak(): void {
  if (window.location.hash && window.location.hash.includes('iss=')) {
    const path = window.location.pathname + window.location.search;
    window.history.replaceState(null, '', path);
  }
}

export function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak
      .init({
        config: {
          url: environment.keycloak.url,
          realm: environment.keycloak.realm,
          clientId: environment.keycloak.clientId
        },
        initOptions: {
          onLoad: 'login-required',
          checkLoginIframe: false,
          redirectUri: REDIRECT_URI(),
          flow: 'standard',
          pkceMethod: 'S256',
          locale: 'pt-BR'
        },
        loadUserProfileAtStartUp: false,
        enableBearerInterceptor: true,
        bearerExcludedUrls: ['/assets', '/realms', '/protocol']
      })
      .then(() => {
        limparFragmentoKeycloak();
        return true;
      });
}
