import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { GameSection } from '../game.model';

@Component({
  selector: 'app-story-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './story-section.html',
  styleUrl: './story-section.scss',
})
export class StorySection {
  readonly section = input.required<GameSection>();
  readonly disabled = input(false);
  readonly choose = output<number>();
}
