import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export interface Aliquota {
  id?: number;
  regimeTributario: string;
  pis: number;
  cofins: number;
  irpj: number;
  csll: number;
  vigencia?: string;
}

export interface ApuracaoRequest {
  competencia: string;
  receitaBruta: number;
  regimeTributario: string;
}

export interface ApuracaoFiscal {
  id?: number;
  competencia: string;
  receitaBruta: number;
  regimeTributario: string;
  pis: number;
  cofins: number;
  irpj: number;
  csll: number;
  totalTributos: number;
  cargaTributaria: number;
  usuario?: string;
  dataCalculo?: string;
}

export interface DashboardStats {
  totalAliquotas: number;
  ultimaVigencia: string;
  totalRegimes: number;
  receitaSimulada: number;
  cargaTributariaMedia: number;
  aliquotasPorRegime: { regime: string; quantidade: number }[];
  distribuicaoTributos: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class ApiService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.base}/dashboard/stats`);
  }

  getAliquotas(): Observable<Aliquota[]> {
    return this.http.get<Aliquota[]>(`${this.base}/aliquotas`);
  }

  getAliquotaByRegime(regime: string): Observable<Aliquota> {
    return this.http.get<Aliquota>(`${this.base}/aliquotas/regime/${encodeURIComponent(regime)}`);
  }

  calcularApuracao(request: ApuracaoRequest): Observable<ApuracaoFiscal> {
    return this.http.post<ApuracaoFiscal>(`${this.base}/apuracoes/calcular`, request);
  }

  getApuracoes(): Observable<ApuracaoFiscal[]> {
    return this.http.get<ApuracaoFiscal[]>(`${this.base}/apuracoes`);
  }

  getApuracao(id: number): Observable<ApuracaoFiscal> {
    return this.http.get<ApuracaoFiscal>(`${this.base}/apuracoes/${id}`);
  }
}
