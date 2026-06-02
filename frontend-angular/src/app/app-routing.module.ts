import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './shared/layout/layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ApuracaoFiscalComponent } from './features/apuracao/apuracao-fiscal.component';
import { HistoricoComponent } from './features/historico/historico.component';
import { AuthGuard } from './core/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard-angular', pathMatch: 'full' },
      { path: 'dashboard-angular', component: DashboardComponent },
      { path: 'apuracao-fiscal', component: ApuracaoFiscalComponent },
      { path: 'historico', component: HistoricoComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard-angular' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
