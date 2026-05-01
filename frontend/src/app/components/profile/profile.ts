import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UserResponse } from '../../models/interfaces';

@Component({
	selector: 'app-profile',
	standalone: false,
	templateUrl: './profile.html',
	styleUrl: './profile.scss',
})
export class Profile implements OnInit {
	user: UserResponse | null = null;
	loading = true;
	saving = false;
	deleting = false;
	error = '';
	toast = '';
	toastType: 'success' | 'error' = 'success';

	showDeleteConfirm = false;
	form = {
		username: '',
		password: '',
		confirmPassword: '',
		baseCurrency: 'USD',
	};

	currencies = ['USD', 'EUR'];

	constructor(private auth: AuthService, private router: Router, private cdr: ChangeDetectorRef) {}

	ngOnInit(): void {
		this.loadUser();
	}

	loadUser(): void {
		this.loading = true;
		this.auth.getMe().pipe(
			finalize(() => {
				this.loading = false;
				this.cdr.detectChanges();
			})
		).subscribe({
			next: (user) => {
				this.user = user;
				this.form.username = user.username;
				this.form.baseCurrency = user.baseCurrency;
				this.form.password = '';
				this.form.confirmPassword = '';
			},
			error: () => {
				this.error = 'No se pudo cargar tu perfil.';
			},
		});
	}

	save(): void {
		if (!this.form.username || !this.form.baseCurrency || !this.form.password) {
			this.showToast('Completa usuario, divisa y nueva contrasena.', 'error');
			return;
		}
		if (this.form.password !== this.form.confirmPassword) {
			this.showToast('Las contrasenas no coinciden.', 'error');
			return;
		}

		this.saving = true;
		this.auth.updateMe({
			username: this.form.username,
			password: this.form.password,
			baseCurrency: this.form.baseCurrency,
		}).pipe(
			finalize(() => {
				this.saving = false;
				this.cdr.detectChanges();
			})
		).subscribe({
			next: (user) => {
				this.user = user;
				this.form.password = '';
				this.form.confirmPassword = '';
				this.showToast('Perfil actualizado.', 'success');
			},
			error: () => {
				this.showToast('No se pudo actualizar el perfil.', 'error');
			},
		});
	}

	deleteAccount(): void {
		this.deleting = true;
		this.auth.deleteMe().pipe(
			finalize(() => {
				this.deleting = false;
				this.cdr.detectChanges();
			})
		).subscribe({
			next: () => {
				this.auth.logout();
				this.router.navigate(['/login']);
			},
			error: () => {
				this.showToast('No se pudo eliminar la cuenta.', 'error');
			},
		});
	}

	showToast(message: string, type: 'success' | 'error'): void {
		this.toast = message;
		this.toastType = type;
		setTimeout(() => {
			this.toast = '';
		}, 3000);
	}
}
