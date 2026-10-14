import type AdresseDTO from "./AdresseDTO";

export class StadtbezirkDTO {
  number: number;
  name: string;

  constructor(number: number, name: string) {
    this.number = number;
    this.name = name;
  }
}

export default class EinrichtungDTO {
  id: string;
  name: string;
  adresse: AdresseDTO;
  stadtbezirk: StadtbezirkDTO;
  stadtquartier: string;
  kibigWebId: string;
  einrichtungsartLhm: string;

  constructor(
    id: string,
    name: string,
    adresse: AdresseDTO,
    stadtbezirk: StadtbezirkDTO,
    stadtquartier: string,
    kibigWebId: string,
    einrichtungsartLhm: string
  ) {
    this.id = id;
    this.name = name;
    this.adresse = adresse;
    this.stadtbezirk = stadtbezirk;
    this.stadtquartier = stadtquartier;
    this.kibigWebId = kibigWebId;
    this.einrichtungsartLhm = einrichtungsartLhm;
  }
}
