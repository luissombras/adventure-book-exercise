import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Consequence } from '../game.model';

@Component({
  selector: 'app-consequence-note',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './consequence-note.html',
  styleUrl: './consequence-note.scss',
})
export class ConsequenceNote {
  readonly consequence = input.required<Consequence>();

  protected readonly gain = computed(() => this.consequence().healthChange > 0);
  protected readonly change = computed(() => healthChangeLabel(this.consequence().healthChange));
}

export function healthChangeLabel(change: number): string {
  return `${change > 0 ? '+' : '−'}${Math.abs(change)} health`;
}
