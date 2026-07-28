import type AdresseDTO from "./AdresseDTO";

export class TeamDTO {
  name: string;
  postfach: string;

  constructor(
    name: string,
    postfach: string
  ) {
    this.name = name;
    this.postfach = postfach;
  }
}

export class AnsprechpartnerDTO {
  anrede: string;
  vorname: string;
  nachname: string;
  telefon: string;
  email: string;
  rollen: string[];

  constructor(
    anrede: string,
    vorname: string,
    nachname: string,
    telefon: string,
    email: string,
    rollen: string[]
  ) {
    this.anrede = anrede;
    this.vorname = vorname;
    this.nachname = nachname;
    this.telefon = telefon;
    this.email = email;
    this.rollen = rollen;
  }
}


export default class TraegerDTO {
  id: string;
  name: string;
  traegerform: string;
  adresse: AdresseDTO;
  team: TeamDTO;
  telefon: string;
  fax: string;
  email: string;
  homepage: string;
  ansprechpartner: AnsprechpartnerDTO[];

  constructor(
    id: string,
    name: string,
    traegerform: string,
    adresse: AdresseDTO,
    team: TeamDTO,
    telefon: string,
    fax: string,
    email: string,
    homepage: string,
    ansprechpartner: AnsprechpartnerDTO[]
  ) {
    this.id = id;
    this.name = name;
    this.traegerform = traegerform;
    this.adresse = adresse;
    this.team = team;
    this.telefon = telefon;
    this.fax = fax;
    this.email = email;
    this.homepage = homepage;
    this.ansprechpartner = ansprechpartner;
  }
}
