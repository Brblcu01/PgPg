import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';

@Component({
  selector: 'app-date-picker',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePickerModule],
  templateUrl: './app-date-picker.component.html',
  styleUrl: './app-date-picker.component.css'
})
export class AppDatePickerComponent implements OnChanges {
  @Input() value = '';
  @Input() placeholder = 'Seleziona data';
  @Input() ariaLabel = 'Seleziona data';
  @Input() allowClear = true;

  @Output() valueChange = new EventEmitter<string>();

  readonly inputId = `app-date-picker-${Math.random().toString(36).slice(2)}`;
  dateValue: Date | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value']) {
      this.dateValue = this.parseIsoDate(this.value);
    }
  }

  onDateChange(value: Date | null): void {
    const nextValue = value ? this.toIsoDate(value) : '';

    if (nextValue !== this.value) {
      this.valueChange.emit(nextValue);
    }
  }

  private parseIsoDate(value: string): Date | null {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      return null;
    }

    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
