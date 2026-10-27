import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Icon } from '../../shared/icon/icon';

const LOW_HEALTH_PERCENT = 30;

@Component({
  selector: 'app-health-meter',
  imports: [Icon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './health-meter.html',
  styleUrl: './health-meter.scss',
})
export class HealthMeter {
  readonly health = input.required<number>();
  readonly max = input.required<number>();

  protected readonly percent = computed(() => (this.max() > 0 ? (this.health() / this.max()) * 100 : 0));
  protected readonly low = computed(() => this.percent() <= LOW_HEALTH_PERCENT);
}
