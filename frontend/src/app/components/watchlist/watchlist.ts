import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { WatchlistDTO } from '../../models/interfaces';

@Component({
	selector: 'app-watchlist',
	standalone: false,
	templateUrl: './watchlist.html',
	styleUrl: './watchlist.scss',
})
export class Watchlist implements OnInit {
	items: WatchlistDTO[] = [];
	loading = true;
	toast = '';
	toastType: 'success' | 'error' = 'success';

	showAddModal = false;
	addTicker = '';
	addName = '';
	addLoading = false;
	addError = '';

	deleteLoading: { [id: number]: boolean } = {};

	constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

	ngOnInit(): void {
		this.load();
	}

	load(): void {
		this.loading = true;
		this.api.getWatchlist().pipe(
			finalize(() => {
				this.loading = false;
				this.cdr.detectChanges();
			})
		).subscribe({
			next: (data) => {
				this.items = data;
			},
			error: () => {},
		});
	}

	openAdd(): void {
		this.addTicker = '';
		this.addName = '';
		this.addError = '';
		this.showAddModal = true;
	}

	submitAdd(): void {
		if (!this.addTicker || !this.addName) {
			this.addError = 'Ticker y nombre son obligatorios.';
			return;
		}
		this.addLoading = true;
		this.addError = '';
		this.api.addToWatchlist({ ticker: this.addTicker.toUpperCase(), companyName: this.addName }).pipe(
			finalize(() => {
				this.addLoading = false;
				this.cdr.detectChanges();
			})
		).subscribe({
			next: (item) => {
				this.items.push(item);
				this.showAddModal = false;
				this.showToast(`${item.ticker} agregado a watchlist`, 'success');
			},
			error: (err) => {
				this.addError = err?.error || 'Error al agregar.';
			},
		});
	}

	remove(item: WatchlistDTO): void {
		this.deleteLoading[item.id] = true;
		this.api.deleteFromWatchlist(item.id).pipe(
			finalize(() => {
				delete this.deleteLoading[item.id];
				this.cdr.detectChanges();
			})
		).subscribe({
			next: () => {
				this.items = this.items.filter((i) => i.id !== item.id);
				this.showToast(`${item.ticker} eliminado`, 'success');
			},
			error: () => {
				this.showToast('Error al eliminar.', 'error');
			},
		});
	}

	showToast(msg: string, type: 'success' | 'error'): void {
		this.toast = msg;
		this.toastType = type;
		setTimeout(() => (this.toast = ''), 3000);
	}
}
