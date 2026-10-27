import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';
import { HealthMeter } from '../health-meter/health-meter';

@Component({
  selector: 'app-game-header',
  imports: [HealthMeter, Icon, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-header.html',
  styleUrl: './game-header.scss',
})
export class GameHeader {
  readonly bookTitle = input<string>();
  readonly health = input<number>();
  readonly maxHealth = input<number>();
}
