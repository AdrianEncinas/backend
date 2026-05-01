import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { RegisterRequest, UserResponse } from '../models/interfaces';

export interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly BASE = 'http://localhost:8080/api/v1/users';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  constructor(private http: HttpClient, private router: Router) {
    // Drop legacy persisted auth so the app always starts from login.
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.BASE}/login`, { username, password }).pipe(
      tap((res) => {
        sessionStorage.setItem(this.TOKEN_KEY, res.token);
      })
    );
  }

  register(data: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.BASE}/register`, data);
  }

  getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.BASE}/me`);
  }

  updateMe(data: RegisterRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.BASE}/me`, data);
  }

  deleteMe(): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/me`);
  }

  logout(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
