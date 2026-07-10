import { Component, ElementRef, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  @ViewChild('introVideo') private introVideo?: ElementRef<HTMLVideoElement>;

  private readonly router = inject(Router);

  protected readonly videoSrc = 'assets/hero-video.mp4';
  protected readonly backgroundSrc = 'assets/proconsul-background.png';
  protected videoStarted = false;
  protected introComplete = false;
  protected whiteTransition = false;
  private completingIntro = false;

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
      void this.router.navigateByUrl('/prenotazioni');
    }, 120);
  }
}
