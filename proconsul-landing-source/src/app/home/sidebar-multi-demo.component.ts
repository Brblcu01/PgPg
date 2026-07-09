import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { SidebarModule } from 'primeng/sidebar';

import { Home } from '@primeicons/angular/home';
import { Database } from '@primeicons/angular/database';
import { Key } from '@primeicons/angular/key';
import { Server } from '@primeicons/angular/server';
import { Globe } from '@primeicons/angular/globe';
import { Code } from '@primeicons/angular/code';
import { Cog } from '@primeicons/angular/cog';
import { ExternalLink } from '@primeicons/angular/external-link';

@Component({
  selector: 'app-sidebar-multi-demo',
  standalone: true,
  templateUrl: './sidebar-multi-demo.component.html',
  styleUrls: ['./sidebar-multi-demo.component.css'],
  imports: [
    SidebarModule,
    Home,
    Database,
    Key,
    Server,
    Globe,
    Code,
    Cog,
    ExternalLink
  ]
})
export class SidebarMultiDemoComponent implements OnInit, OnDestroy {
  isMobile = signal(false);

  open = signal(false);

  secondaryOpen = signal(true);

  private mql?: MediaQueryList;

  private mqlListener?: (e: MediaQueryListEvent) => void;

  ngOnInit(): void {
    if (typeof window === 'undefined') return;

    this.mql = window.matchMedia('(max-width: 1023px)');

    this.isMobile.set(this.mql.matches);
    this.secondaryOpen.set(!this.mql.matches);

    this.mqlListener = (e: MediaQueryListEvent) => {
      this.isMobile.set(e.matches);
      this.secondaryOpen.set(!e.matches);
    };

    this.mql.addEventListener('change', this.mqlListener);
  }

  ngOnDestroy(): void {
    if (this.mqlListener) {
      this.mql?.removeEventListener('change', this.mqlListener);
    }
  }
}