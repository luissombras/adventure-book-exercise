import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';

@Component({
  selector: 'app-game-header',
  imports: [Icon, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-header.html',
  styleUrl: './game-header.scss',
})
export class GameHeader {
  readonly bookTitle = input<string>();
}
