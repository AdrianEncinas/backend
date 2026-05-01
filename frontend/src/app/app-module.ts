import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Dashboard } from './components/dashboard/dashboard';
import { Stocks } from './components/stocks/stocks';
import { Login } from './components/login/login';
import { Nav } from './components/nav/nav';
import { Watchlist } from './components/watchlist/watchlist';
import { Profile } from './components/profile/profile';
import { StockDetails } from './components/stock-details/stock-details';
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [App, Dashboard, Stocks, Login, Nav, Watchlist, Profile, StockDetails],
  imports: [BrowserModule, AppRoutingModule, FormsModule, HttpClientModule],
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [App],
})
export class AppModule {}
