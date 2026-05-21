import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';



@Component({
  standalone: true,
  imports: [
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule
  ],
  selector: 'app-login',
  templateUrl: './login.html',
})
export class LoginComponent {

  email: string = '';
  password: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login() {
    console.log("EMAIL:", this.email);
  console.log("PASSWORD:", this.password);
    this.authService.login({
      email: this.email,
      password: this.password
    }).subscribe({
      next: (res: any) => {
        console.log("LOGIN SUCCESS", res);
        this.authService.saveToken(res.token);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error("LOGIN FAILED", err);
      }
    });
  }
}