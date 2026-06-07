import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'tasks',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tasks/task-list/task-list.component').then((m) => m.TaskListComponent)
  },
  { path: '', pathMatch: 'full', redirectTo: 'tasks' },
  { path: 'dashboard', redirectTo: 'tasks' },
  { path: '**', redirectTo: 'tasks' }
];
