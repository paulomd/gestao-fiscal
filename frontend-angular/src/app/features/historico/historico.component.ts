import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ApiService, ApuracaoFiscal } from '../../services/api.service';
import { MatDialog } from '@angular/material/dialog';
import { HistoricoDetalheDialogComponent } from './historico-detalhe-dialog.component';

@Component({
  selector: 'app-historico',
  templateUrl: './historico.component.html',
  styleUrls: ['./historico.component.scss']
})
export class HistoricoComponent implements OnInit {

  displayedColumns = ['competencia', 'receitaBruta', 'regimeTributario', 'totalTributos', 'cargaTributaria', 'usuario', 'dataCalculo', 'acoes'];
  dataSource = new MatTableDataSource<ApuracaoFiscal>([]);
  filtro = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private api: ApiService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.api.getApuracoes().subscribe(lista => {
      this.dataSource.data = lista;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  aplicarFiltro(): void {
    this.dataSource.filter = this.filtro.trim().toLowerCase();
  }

  detalhar(item: ApuracaoFiscal): void {
    this.dialog.open(HistoricoDetalheDialogComponent, {
      width: '500px',
      data: item
    });
  }
}
