import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';

export type BookingZoneType = 'office' | 'meeting' | 'training' | 'support' | 'service';
export type BookingZoneStatus = 'free' | 'reserved' | 'selected' | 'disabled';

export interface BookingMetric {
  label: string;
  value: string;
}

export interface BookingZone {
  id: string;
  label: string;
  type: BookingZoneType;
  status: BookingZoneStatus;
  cssClass: string;
  seats?: number;
  description?: string;
}

@Component({
  selector: 'app-prenotazioni',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    DialogModule,
    TagModule,
    TooltipModule
  ],
  templateUrl: './prenotazioni.component.html',
  styleUrl: './prenotazioni.component.css'
})
export class PrenotazioniComponent {
  private readonly router = inject(Router);

  selectedZone: BookingZone | null = null;
  showZoneDialog = false;

  readonly metrics: BookingMetric[] = [
    { label: 'Posti Totali', value: '12' },
    { label: 'Posti Liberi', value: '7' },
    { label: 'Giorno', value: 'gg-MM-yyyy' }
  ];

  readonly zones: BookingZone[] = [
    {
      id: 'office-2',
      label: 'Ufficio 2',
      type: 'office',
      status: 'disabled',
      cssClass: 'zone-office-2',    
    },
    {
      id: 'office-1',
      label: 'Ufficio 1',
      type: 'office',
      status: 'free',
      cssClass: 'zone-office-1',
      seats: 2    
    },
    {
      id: 'server',
      label: 'Server',
      type: 'service',
      status: 'disabled',
      cssClass: 'zone-server',
    },
    {
      id: 'academy',
      label: 'Academy',
      type: 'training',
      status: 'free',
      cssClass: 'zone-academy',
    },
    {
      id: 'office-3',
      label: 'Ufficio 3',
      type: 'office',
      status: 'free',
      cssClass: 'zone-office-3',
      seats: 6   
    },
    {
      id: 'office-4',
      label: 'Ufficio 4',
      type: 'office',
      status: 'free',
      cssClass: 'zone-office-4',
      seats: 4   
    },
    {
      id: 'meeting',
      label: 'Sala riunioni',
      type: 'meeting',
      status: 'free',
      cssClass: 'zone-meeting'
    },
    {
      id: 'support',
      label: 'Support',
      type: 'support',
      status: 'disabled',
      cssClass: 'zone-support'
    }
  ];

  goToLogin(): void {
    void this.router.navigateByUrl('/login');
  }

  selectZone(zone: BookingZone): void {
    if (zone.status === 'disabled') {
      return;
    }

    this.selectedZone = zone;
    this.showZoneDialog = true;
  }

  reserveSelectedZone(): void {
    this.setSelectedZoneStatus('reserved');
  }

  freeSelectedZone(): void {
    this.setSelectedZoneStatus('free');
  }

  getZoneSeverity(status: BookingZoneStatus): 'success' | 'danger' | 'info' | 'secondary' | 'warn' {
    const severities: Record<BookingZoneStatus, 'success' | 'danger' | 'info' | 'secondary' | 'warn'> = {
      free: 'success',
      reserved: 'danger',
      selected: 'info',
      disabled: 'secondary'
    };

    return severities[status];
  }

  getZoneStatusLabel(status: BookingZoneStatus): string {
    const labels: Record<BookingZoneStatus, string> = {
      free: 'Libero',
      reserved: 'Prenotato',
      selected: 'Selezionato',
      disabled: 'Privato'
    };

    return labels[status];
  }

  private setSelectedZoneStatus(status: BookingZoneStatus): void {
    if (!this.selectedZone) {
      return;
    }

    this.selectedZone.status = status;
    this.showZoneDialog = false;
  }
}
