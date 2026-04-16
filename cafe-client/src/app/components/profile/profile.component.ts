import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [],
  template: ''
})
export class ProfileComponent {
  constructor(private router: Router) {
    this.router.navigate(['/dashboard']);
  }
}
