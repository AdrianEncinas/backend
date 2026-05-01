import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  tab: 'login' | 'register' = 'login';

  // Login
  loginUsername = '';
  loginPassword = '';

  // Register
  regUsername = '';
  regPassword = '';
  regPasswordConfirm = '';
  regCurrency = 'USD';

  error = '';
  success = '';
  loading = false;

  currencies = ['USD', 'EUR'];

  constructor(private auth: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

  switchTab(t: 'login' | 'register'): void {
    this.tab = t;
    this.error = '';
    this.success = '';
  }

  onLogin(): void {
    if (!this.loginUsername || !this.loginPassword) {
      this.error = 'Por favor completa todos los campos.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.auth.login(this.loginUsername, this.loginPassword).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Usuario o contraseña incorrectos.';
      },
    });
  }

  onRegister(): void {
    if (!this.regUsername || !this.regPassword) {
      this.error = 'Por favor completa todos los campos.';
      return;
    }
    if (this.regPassword !== this.regPasswordConfirm) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.auth.register({
      username: this.regUsername,
      password: this.regPassword,
      baseCurrency: this.regCurrency,
    }).pipe(finalize(() => {
      this.loading = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: () => {
        this.success = '¡Cuenta creada! Ahora puedes iniciar sesión.';
        this.loginUsername = this.regUsername;
        this.regUsername = this.regPassword = this.regPasswordConfirm = '';
        this.tab = 'login';
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al crear la cuenta.';
      },
    });
  }
}
