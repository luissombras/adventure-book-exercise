import { TestBed } from '@angular/core/testing';
import { Consequence } from '../game.model';
import { ConsequenceNote } from './consequence-note';

describe('ConsequenceNote', () => {
  function render(consequence: Consequence): HTMLElement {
    TestBed.configureTestingModule({ imports: [ConsequenceNote] });
    const fixture = TestBed.createComponent(ConsequenceNote);
    fixture.componentRef.setInput('consequence', consequence);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('announces a loss of health', () => {
    const element = render({ text: 'You cut yourself on a rusty nail.', healthChange: -6 });

    expect(element.querySelector('[role="status"]')?.textContent).toContain('You cut yourself on a rusty nail.');
    expect(element.querySelector('.note__change')?.textContent).toContain('−6 health');
    expect(element.querySelector('.note')?.classList).not.toContain('note--gain');
  });

  it('shows a gain of health in a different tone', () => {
    const element = render({ text: 'You feel better.', healthChange: 3 });

    expect(element.querySelector('.note__change')?.textContent).toContain('+3 health');
    expect(element.querySelector('.note')?.classList).toContain('note--gain');
  });

  it('leaves out the change when health is not affected', () => {
    expect(render({ text: 'Nothing happens.', healthChange: 0 }).querySelector('.note__change')).toBeNull();
  });
});
