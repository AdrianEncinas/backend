import { Component, HostListener } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss',
})
export class App {
  showNav = false;
  mobileNavOpen = false;

  constructor(private router: Router, private auth: AuthService) {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.showNav = this.auth.isAuthenticated() && !this.router.url.startsWith('/login');
      this.mobileNavOpen = false;
    });
  }

  toggleMobileNav(): void {
    this.mobileNavOpen = !this.mobileNavOpen;
  }

  closeMobileNav(): void {
    this.mobileNavOpen = false;
  }

  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth > 960 && this.mobileNavOpen) {
      this.mobileNavOpen = false;
    }
  }
}
