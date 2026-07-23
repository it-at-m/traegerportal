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

export default class EinrichtungDTO {
  id: string;
  name: string;
  adresse: AdresseDTO;
  merkmale: MerkmaleDTO;
  aktuellGueltigerStatus: EinrichtungsstatusDTO;

  constructor(
    id: string,
    name: string,
    adresse: AdresseDTO,
    merkmale: MerkmaleDTO,
    aktuellGueltigerStatus: EinrichtungsstatusDTO
  ) {
    this.id = id;
    this.name = name;
    this.adresse = adresse;
    this.merkmale = merkmale;
    this.aktuellGueltigerStatus = aktuellGueltigerStatus;
  }
}
