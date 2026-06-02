import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApuracaoFiscal } from '../../services/api.service';

@Component({
  selector: 'app-historico-detalhe-dialog',
  template: `
    <h2 mat-dialog-title>Detalhe da Apuração</h2>
    <mat-dialog-content>
      <p><strong>Competência:</strong> {{ data.competencia }}</p>
      <p><strong>Receita Bruta:</strong> {{ data.receitaBruta | currency:'BRL' }}</p>
      <p><strong>Regime:</strong> {{ data.regimeTributario }}</p>
      <p><strong>PIS:</strong> {{ data.pis | currency:'BRL' }}</p>
      <p><strong>COFINS:</strong> {{ data.cofins | currency:'BRL' }}</p>
      <p><strong>IRPJ:</strong> {{ data.irpj | currency:'BRL' }}</p>
      <p><strong>CSLL:</strong> {{ data.csll | currency:'BRL' }}</p>
      <p><strong>Total Tributos:</strong> {{ data.totalTributos | currency:'BRL' }}</p>
      <p><strong>Carga Tributária:</strong> {{ data.cargaTributaria }}%</p>
      <p><strong>Usuário:</strong> {{ data.usuario }}</p>
      <p><strong>Data Cálculo:</strong> {{ data.dataCalculo | date:'dd/MM/yyyy HH:mm' }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Fechar</button>
    </mat-dialog-actions>
  `
})
export class HistoricoDetalheDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: ApuracaoFiscal) {}
}
