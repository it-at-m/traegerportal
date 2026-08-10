import type AdresseDTO from "./AdresseDTO";

export class MerkmaleDTO {
  kibigWebId: string;

  constructor(kibigWebId: string) {
    this.kibigWebId = kibigWebId;
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
