import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

/** login-required no APP_INITIALIZER já autentica; evita segundo login() que causa loop/502 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private keycloak: KeycloakService) {}

  async canActivate(): Promise<boolean> {
    return this.keycloak.isLoggedIn();
  }
}
