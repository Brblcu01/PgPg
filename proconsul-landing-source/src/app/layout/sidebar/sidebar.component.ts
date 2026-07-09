import { Component, HostBinding } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [ButtonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  @HostBinding('class.is-expanded')
  protected isExpanded = false;

  private expansionLocked = false;

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
    (event.currentTarget as HTMLElement).blur();
  }
}
