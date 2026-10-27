import { TestBed } from '@angular/core/testing';
import { HealthMeter } from './health-meter';

describe('HealthMeter', () => {
  beforeEach(() => TestBed.configureTestingModule({ imports: [HealthMeter] }));

  function render(health: number, max = 10): HTMLElement {
    const fixture = TestBed.createComponent(HealthMeter);
    fixture.componentRef.setInput('health', health);
    fixture.componentRef.setInput('max', max);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the health as text, as a bar and to assistive technology', () => {
    const element = render(7);
    const meter = element.querySelector('[role="meter"]')!;

    expect(element.querySelector('.health__value')?.textContent).toContain('7 / 10');
    expect(element.querySelector<HTMLElement>('.health__fill')?.style.width).toBe('70%');
    expect(meter.getAttribute('aria-valuenow')).toBe('7');
    expect(meter.getAttribute('aria-valuemax')).toBe('10');
    expect(meter.getAttribute('aria-valuetext')).toBe('7 of 10');
    expect(meter.classList).not.toContain('health--low');
  });

  it('warns when health is low relative to the maximum', () => {
    expect(render(3).querySelector('.health')?.classList).toContain('health--low');
    expect(render(6, 20).querySelector('.health')?.classList).toContain('health--low');
    expect(render(7, 20).querySelector('.health')?.classList).not.toContain('health--low');
  });
});
