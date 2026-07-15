import { CommonModule } from '@angular/common';
import { Component, HostBinding, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';

import { AuthApiService } from '../../core/auth-api.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, ButtonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  @HostBinding('class.is-expanded')
  protected isExpanded = false;

  protected isMobileMenuOpen = false;

  private readonly authApi = inject(AuthApiService);
  private readonly currentUser = this.authApi.getCurrentUserInfo();
  private expansionLocked = false;

  protected get isAdminUser(): boolean {
    const profileCode = this.currentUser?.profileCode?.toUpperCase();
    const roleCode = this.currentUser?.roleCode?.toUpperCase();

    return profileCode === 'ADMIN' || roleCode === 'HR';
  }

  protected openSidebar(): void {
    if (!this.expansionLocked) {
      this.isExpanded = true;
    }
  }

  protected closeSidebar(): void {
    this.isExpanded = false;
    this.expansionLocked = false;
  }

  protected closeAfterNavigation(event: MouseEvent): void {
    this.isExpanded = false;
    this.expansionLocked = true;
    this.isMobileMenuOpen = false;
    (event.currentTarget as HTMLElement).blur();
  }

  protected toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  protected closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}
