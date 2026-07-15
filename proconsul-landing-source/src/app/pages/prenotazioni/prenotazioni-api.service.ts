import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface DisponibilitaPrenotazioneDTO {
  idRisorsaPrenotabile?: number;
  idWorkspace?: number;
  codice?: string;
  nome?: string;
  tipoRisorsa?: string;
  capienza?: number;
  prenotazioneEsclusiva?: boolean;
  prenotazioniConfermate?: number;
  postiDisponibili?: number;
}

export interface PostoWorkspaceDTO {
  idWorkspaceSeat: number;
  idWorkspace: number;
  codice?: string;
  nome?: string;
  occupato: boolean;
}

export interface PrenotazioneDTO {
  idPrenotazione: number;
  idWorkspace?: number;
  idRisorsaPrenotabile?: number;
  codiceWorkspace?: string;
  codiceRisorsa?: string;
  nomeWorkspace?: string;
  nomeRisorsa?: string;
  postazioneName?: string;
  idUtente?: number;
  utenteName?: string;
  dataPrenotazione: string;
  stato?: string;
  dataCreazione?: string;
  prenotazioneUtenteCorrente?: boolean;
}

export interface BloccoPrenotazioniDTO {
  idBlocco: number;
  dataInizio: string;
  dataFine: string;
  motivo?: string;
  idUtenteCreazione?: number;
  dataCreazione?: string;
}

export interface RichiestaPrenotazioneDTO {
  idWorkspace: number;
  dataPrenotazione: string;
  idWorkspaceSeat?: number;
}

export interface RichiestaBloccoPrenotazioniDTO {
  dataInizio: string;
  dataFine: string;
  motivo: string;
}

export interface MessageResponse {
  message?: string;
  status?: number;
}

@Injectable({ providedIn: 'root' })
export class PrenotazioniApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/prenotazioni';

  trovaDisponibili(data: string): Observable<DisponibilitaPrenotazioneDTO[]> {
    return this.http.get<DisponibilitaPrenotazioneDTO[]>(`${this.baseUrl}/disponibili`, {
      params: new HttpParams().set('data', data)
    });
  }

  trovaPrenotate(data?: string, dataDa?: string, dataA?: string): Observable<PrenotazioneDTO[]> {
    let params = new HttpParams();

    if (data) {
      params = params.set('data', data);
    }

    if (dataDa) {
      params = params.set('dataDa', dataDa);
    }

    if (dataA) {
      params = params.set('dataA', dataA);
    }

    return this.http.get<PrenotazioneDTO[]>(`${this.baseUrl}/prenotate`, { params });
  }

  trovaMiePrenotazioni(data: string): Observable<PrenotazioneDTO[]> {
    return this.http.get<PrenotazioneDTO[]>(`${this.baseUrl}/mie`, {
      params: new HttpParams().set('data', data)
    });
  }

  trovaPostiWorkspace(idWorkspace: number, data: string): Observable<PostoWorkspaceDTO[]> {
    return this.http.get<unknown>(`${this.baseUrl}/workspace/${idWorkspace}/posti`, {
      params: new HttpParams().set('data', data)
    }).pipe(
      map(response => this.toArray(response)
        .map(item => this.normalizePostoWorkspace(item, idWorkspace))
        .filter(posto => Number.isFinite(posto.idWorkspaceSeat))
      )
    );
  }

  creaPrenotazione(richiesta: RichiestaPrenotazioneDTO): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.baseUrl, richiesta);
  }

  eliminaPrenotazione(idPrenotazione: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/${idPrenotazione}`);
  }

  trovaBlocchiPrenotazioni(data?: string, dataDa?: string, dataA?: string): Observable<BloccoPrenotazioniDTO[]> {
    let params = new HttpParams();

    if (data) {
      params = params.set('data', data);
    }

    if (dataDa) {
      params = params.set('dataDa', dataDa);
    }

    if (dataA) {
      params = params.set('dataA', dataA);
    }

    return this.http.get<BloccoPrenotazioniDTO[]>(`${this.baseUrl}/admin/blocchi`, { params });
  }

  creaBloccoPrenotazioni(richiesta: RichiestaBloccoPrenotazioniDTO): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/admin/blocchi`, richiesta);
  }

  eliminaBloccoPrenotazioni(idBlocco: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/admin/blocchi/${idBlocco}`);
  }

  private toArray(response: unknown): Record<string, unknown>[] {
    if (Array.isArray(response)) {
      return response as Record<string, unknown>[];
    }

    if (response && typeof response === 'object') {
      const wrapped = response as Record<string, unknown>;
      const data = wrapped['data'] ?? wrapped['content'] ?? wrapped['items'];

      if (Array.isArray(data)) {
        return data as Record<string, unknown>[];
      }
    }

    return [];
  }

  private normalizePostoWorkspace(item: Record<string, unknown>, fallbackWorkspaceId: number): PostoWorkspaceDTO {
    return {
      idWorkspaceSeat: this.toNumber(item['idWorkspaceSeat'] ?? item['id'] ?? item['idWorkspaceSeatFk']),
      idWorkspace: this.toNumber(item['idWorkspace'] ?? item['idWorkspaceFk'] ?? fallbackWorkspaceId),
      codice: this.toStringOrUndefined(item['codice'] ?? item['code']),
      nome: this.toStringOrUndefined(item['nome'] ?? item['name']),
      occupato: Boolean(item['occupato'])
    };
  }

  private toStringOrUndefined(value: unknown): string | undefined {
    return typeof value === 'string' ? value : undefined;
  }

  private toNumber(value: unknown): number {
    if (value == null || value === '') {
      return Number.NaN;
    }

    return Number(value);
  }
}
