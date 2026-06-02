import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { nomeExibicaoFromToken, perfilFromToken } from '../../core/keycloak-user.helper';
import { ApiService, DashboardStats } from '../../services/api.service';
import { ChartConfiguration } from 'chart.js';
import { timeout, catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  nomeUsuario = '';
  perfil = '';
  tokenRemaining = '';
  stats: DashboardStats | null = null;
  loading = true;
  erroCarregamento = '';

  regimeChart: ChartConfiguration<'bar'> = {
    type: 'bar',
    data: { labels: [], datasets: [{ data: [], label: 'Alíquotas por regime', backgroundColor: '#2c5282' }] },
    options: { responsive: true, plugins: { legend: { display: false } } }
  };

  tributosChart: ChartConfiguration<'doughnut'> = {
    type: 'doughnut',
    data: {
      labels: ['PIS', 'COFINS', 'IRPJ', 'CSLL'],
      datasets: [{ data: [0, 0, 0, 0], backgroundColor: ['#3182ce', '#ed8936', '#38a169', '#e53e3e'] }]
    },
    options: { responsive: true }
  };

  constructor(
    private api: ApiService,
    private keycloak: KeycloakService
  ) {}

  async ngOnInit(): Promise<void> {
    this.nomeUsuario = nomeExibicaoFromToken(this.keycloak);
    this.perfil = perfilFromToken(this.keycloak);
    this.updateTokenTimer();
    setInterval(() => this.updateTokenTimer(), 1000);

    try {
      await this.keycloak.updateToken(30);
    } catch {
      this.loading = false;
      this.erroCarregamento = 'Sessão expirada. Faça logout e login novamente.';
      return;
    }
    this.carregarEstatisticas();
  }

  carregarEstatisticas(): void {
    this.loading = true;
    this.erroCarregamento = '';
    this.stats = null;

    this.api.getDashboardStats().pipe(
      timeout(12000),
      catchError(err => {
        const status = err?.status;
        if (status === 401 || status === 403) {
          this.erroCarregamento = 'Sem permissão na API. Faça logout e login novamente.';
        } else if (status === 502 || status === 503 || status === 504) {
          this.erroCarregamento = 'API indisponível (erro ' + status + '). Execute: ./scripts/start-all.sh';
        } else if (err?.name === 'TimeoutError') {
          this.erroCarregamento = 'API não respondeu a tempo. Verifique se o backend está no ar (porta 8082).';
        } else {
          this.erroCarregamento = 'Não foi possível carregar o dashboard. Verifique o gateway e o backend.';
        }
        return of(null);
      }),
      finalize(() => { this.loading = false; })
    ).subscribe(stats => {
      if (!stats) {
        return;
      }
      this.stats = stats;
      this.regimeChart.data.labels = stats.aliquotasPorRegime?.map(r => r.regime) || [];
      this.regimeChart.data.datasets[0].data = stats.aliquotasPorRegime?.map(r => r.quantidade) || [];

      const dist = stats.distribuicaoTributos || {};
      this.tributosChart.data.datasets[0].data = [
        dist['PIS'] || 0, dist['COFINS'] || 0, dist['IRPJ'] || 0, dist['CSLL'] || 0
      ];
      this.erroCarregamento = '';
    });
  }

  private updateTokenTimer(): void {
    const exp = this.keycloak.getKeycloakInstance().tokenParsed?.exp;
    if (!exp) {
      this.tokenRemaining = '-';
      return;
    }
    const secs = Math.max(0, exp - Math.floor(Date.now() / 1000));
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    this.tokenRemaining = `${m}m ${s}s`;
  }
}
