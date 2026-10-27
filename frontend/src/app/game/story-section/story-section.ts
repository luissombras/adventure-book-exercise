import { ChangeDetectionStrategy, Component, ElementRef, afterRenderEffect, input, output, viewChild } from '@angular/core';
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

  private readonly heading = viewChild.required<ElementRef<HTMLElement>>('heading');

  constructor() {
    afterRenderEffect(() => {
      this.section();
      this.heading().nativeElement.focus();
    });
  }
}
