import { ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  @ViewChild('introVideo') private introVideo?: ElementRef<HTMLVideoElement>;
  private readonly changeDetector = inject(ChangeDetectorRef);

  protected readonly videoSrc = 'assets/hero-video.mp4';
  protected readonly backgroundSrc = 'assets/proconsul-background.png';
  protected videoStarted = false;
  protected introComplete = false;
  protected whiteTransition = false;
  protected isHome = false;
  private completingIntro = false;
  private readonly syncRoute = (): void => {
    this.isHome = window.location.pathname === '/home';
    this.changeDetector.detectChanges();
  };

  ngOnInit(): void {
    this.syncRoute();
    window.addEventListener('popstate', this.syncRoute);
  }

  ngOnDestroy(): void {
    window.removeEventListener('popstate', this.syncRoute);
  }

  protected onIntroEnded(): void {
    if (this.completingIntro) {
      return;
    }

    this.completingIntro = true;
    this.whiteTransition = true;
    window.setTimeout(() => {
      this.introComplete = true;
      this.videoStarted = false;
      this.completingIntro = false;
      this.introVideo?.nativeElement.pause();
      this.openHome();
      this.changeDetector.detectChanges();
    }, 120);
  }

  protected goToLogin(): void {
    this.isHome = false;
    this.introComplete = false;
    this.whiteTransition = false;
    window.history.pushState({}, '', '/');
    this.changeDetector.detectChanges();
  }

  private openHome(): void {
    this.isHome = true;
    this.whiteTransition = false;
    window.history.pushState({}, '', '/home');
  }

  protected startLoginVideo(): void {
    const video = this.introVideo?.nativeElement;

    if (!video || this.videoStarted) {
      return;
    }

    this.introComplete = false;
    this.videoStarted = true;
    this.whiteTransition = false;
    this.completingIntro = false;
    video.muted = true;
    video.currentTime = 0;
    video.play()
      .catch(() => {
        video.controls = true;
        this.videoStarted = false;
      });
  }

  protected get loginTransform(): string {
    return 'translate3d(0, 0, 0) scale(1)';
  }

  protected onIntroTimeUpdate(video: HTMLVideoElement): void {
    if (!Number.isFinite(video.duration) || this.introComplete || this.completingIntro) {
      return;
    }

    const remaining = video.duration - video.currentTime;
    this.whiteTransition = remaining <= 0.25;

    if (video.ended || remaining <= 0.08) {
      this.onIntroEnded();
    }
  }
}
