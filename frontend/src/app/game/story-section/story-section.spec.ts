import { TestBed } from '@angular/core/testing';
import { GameSection } from '../game.model';
import { StorySection } from './story-section';

describe('StorySection', () => {
  function render(section: GameSection, disabled = false) {
    TestBed.configureTestingModule({ imports: [StorySection] });
    const fixture = TestBed.createComponent(StorySection);
    fixture.componentRef.setInput('section', section);
    fixture.componentRef.setInput('disabled', disabled);
    fixture.detectChanges();
    return fixture;
  }

  it('shows the text and numbered choices, and emits the chosen index', () => {
    const fixture = render({ id: 1, text: 'A dark cell.', options: ['Open the door', 'Look under the bed'] });
    const element = fixture.nativeElement as HTMLElement;
    const chosen: number[] = [];
    fixture.componentInstance.choose.subscribe((index) => chosen.push(index));

    const buttons = element.querySelectorAll<HTMLButtonElement>('.choice');
    buttons[1].click();

    expect(element.querySelector('.story__text')?.textContent).toContain('A dark cell.');
    expect([...buttons].map((b) => b.textContent?.replace(/\s+/g, ' ').trim())).toEqual(['1 Open the door', '2 Look under the bed']);
    expect(chosen).toEqual([1]);
  });

  it('moves focus to the heading whenever the section changes', async () => {
    const fixture = render({ id: 1, text: 'A dark cell.', options: ['Open the door'] });
    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.choice')!.focus();

    fixture.componentRef.setInput('section', { id: 500, text: 'The door is locked.', options: ['Think'] });
    await fixture.whenStable();

    expect(document.activeElement).toBe(element.querySelector('.story__title'));
  });

  it('has no question when the section has no options', () => {
    const element = render({ id: 1000, text: 'You are free.', options: [] }).nativeElement as HTMLElement;

    expect(element.querySelector('.story__question')).toBeNull();
  });

  it('disables the choices while a choice is being sent', () => {
    const element = render({ id: 1, text: 'A dark cell.', options: ['Open the door'] }, true).nativeElement as HTMLElement;

    expect(element.querySelector<HTMLButtonElement>('.choice')?.disabled).toBe(true);
  });
});
