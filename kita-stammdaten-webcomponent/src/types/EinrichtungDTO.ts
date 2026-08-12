import type AdresseDTO from "./AdresseDTO";

export class MerkmaleDTO {
  kibigWebId: string;

  constructor(kibigWebId: string) {
    this.kibigWebId = kibigWebId;
  }
}

export class BetriebserlaubnisDTO {
  aktuellGueltigeSollbelegung: SollbelegungDTO;

  constructor(aktuellGueltigeSollbelegung: SollbelegungDTO) {
    this.aktuellGueltigeSollbelegung = aktuellGueltigeSollbelegung;
  }
}

export class SollbelegungDTO {
  plaetzeGesamt: number;
  plaetzeKinderkrippe: number;
  plaetzeKindergarten: number;
  plaetzeHort: number;

  constructor(
    plaetzeGesamt: number,
    plaetzeKinderkrippe: number,
    plaetzeKindergarten: number,
    plaetzeHort: number
  ) {
    this.plaetzeGesamt = plaetzeGesamt;
    this.plaetzeKinderkrippe = plaetzeKinderkrippe;
    this.plaetzeKindergarten = plaetzeKindergarten;
    this.plaetzeHort = plaetzeHort;
  }
}

export class SchulsprengelDTO {
  schulId: number;
  schule: string;

  constructor(schulId: number, schule: string) {
    this.schulId = schulId;
    this.schule = schule;
  }
}

export class StadtbezirkDTO {
  number: number;
  name: string;

  constructor(number: number, name: string) {
    this.number = number;
    this.name = name;
  }
}

export class EinrichtungsstatusDTO {
  status: string;

  constructor(status: string) {
    this.status = status;
  }
}

export class KontaktdatenDTO {
  telefon: string;
  fax: string;
  homepageUrl: string;
  email: string;

  constructor(
    telefon: string,
    fax: string,
    homepageUrl: string,
    email: string
  ) {
    this.telefon = telefon;
    this.fax = fax;
    this.homepageUrl = homepageUrl;
    this.email = email;
  }
}

export default class EinrichtungDTO {
  id: string;
  name: string;
  adresse: AdresseDTO;
  merkmale: MerkmaleDTO;
  kontaktdaten: KontaktdatenDTO;
  aktuellGueltigerStatus: EinrichtungsstatusDTO;
  aktuellGueltigeBetriebserlaubnis: BetriebserlaubnisDTO;
  schulsprengel: SchulsprengelDTO;
  stadtbezirk: StadtbezirkDTO;

  constructor(
    id: string,
    name: string,
    adresse: AdresseDTO,
    merkmale: MerkmaleDTO,
    kontaktdaten: KontaktdatenDTO,
    aktuellGueltigerStatus: EinrichtungsstatusDTO
  ) {
    this.id = id;
    this.name = name;
    this.adresse = adresse;
    this.merkmale = merkmale;
    this.kontaktdaten = kontaktdaten;
    this.aktuellGueltigerStatus = aktuellGueltigerStatus;
  }
}
