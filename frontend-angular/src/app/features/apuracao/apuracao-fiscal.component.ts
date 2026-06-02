import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService, Aliquota, ApuracaoFiscal } from '../../services/api.service';
import { ChartConfiguration } from 'chart.js';

export type MensagemCentroTipo = 'sucesso' | 'erro' | 'info';

export interface MensagemCentro {
  tipo: MensagemCentroTipo;
  texto: string;
}

@Component({
  selector: 'app-apuracao-fiscal',
  templateUrl: './apuracao-fiscal.component.html',
  styleUrls: ['./apuracao-fiscal.component.scss']
})
export class ApuracaoFiscalComponent {

  form: FormGroup;
  regimes = ['Simples Nacional', 'Lucro Presumido', 'Lucro Real'];
  aliquota: Aliquota | null = null;
  resultado: ApuracaoFiscal | null = null;
  calculando = false;
  mensagem: MensagemCentro | null = null;

  composicaoChart: ChartConfiguration<'pie'> = {
    type: 'pie',
    data: {
      labels: ['PIS', 'COFINS', 'IRPJ', 'CSLL'],
      datasets: [{ data: [], backgroundColor: ['#3182ce', '#ed8936', '#38a169', '#e53e3e'] }]
    },
    options: { responsive: true }
  };

  constructor(
    private fb: FormBuilder,
    private api: ApiService
  ) {
    this.form = this.fb.group({
      competencia: ['', [Validators.required, Validators.pattern(/^\d{2}\/\d{4}$/)]],
      receitaBruta: [100000, [Validators.required, Validators.min(0.01)]],
      regimeTributario: ['Lucro Real', Validators.required]
    });
  }

  calcular(): void {
    if (this.form.invalid) return;
    this.mensagem = null;
    this.calculando = true;
    this.api.calcularApuracao(this.form.value).subscribe({
      next: res => {
        this.resultado = res;
        this.composicaoChart.data.datasets[0].data = [res.pis, res.cofins, res.irpj, res.csll];
        this.calculando = false;
        this.mostrarMensagem('sucesso', 'Apuração calculada e salva com sucesso!');
      },
      error: err => {
        this.calculando = false;
        this.mostrarMensagem('erro', err.error?.message || 'Erro ao calcular');
      }
    });
  }

  buscarAliquotas(): void {
    const regime = this.form.get('regimeTributario')?.value;
    this.mensagem = null;
    this.api.getAliquotaByRegime(regime).subscribe({
      next: a => this.aliquota = a,
      error: () => this.mostrarMensagem('erro', 'Alíquota não encontrada para o regime')
    });
  }

  mostrarMensagem(tipo: MensagemCentroTipo, texto: string): void {
    this.mensagem = { tipo, texto };
  }

  fecharMensagem(): void {
    this.mensagem = null;
  }
}
