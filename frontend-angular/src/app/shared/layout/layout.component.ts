import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { environment } from '../../../environments/environment';
import { nomeExibicaoFromToken, perfilFromToken } from '../../core/keycloak-user.helper';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {

  nomeUsuario = '';
  perfil = '';
  jsfBase = environment.jsfBaseUrl || '';

  constructor(private keycloak: KeycloakService) {}

  async ngOnInit(): Promise<void> {
    this.nomeUsuario = nomeExibicaoFromToken(this.keycloak);
    this.perfil = perfilFromToken(this.keycloak);
  }

  urlDashboardJsf(): string {
    return `${this.jsfBase}/dashboard.xhtml`;
  }

  urlAliquotasJsf(): string {
    return `${this.jsfBase}/aliquotas.xhtml`;
  }

  async logout(): Promise<void> {
    await this.keycloak.logout(`${window.location.origin}/dashboard-angular`);
  }
}
