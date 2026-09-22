import type AdresseDTO from "./AdresseDTO";
import type KontaktdatenDTO from "./KontaktdatenDTO";

export class TeamDTO {
  name: string;
  postfach: string;

  constructor(name: string, postfach: string) {
    this.name = name;
    this.postfach = postfach;
  }
}

export class AnsprechpartnerDTO {
  id: number;
  anrede: string;
  vorname: string;
  nachname: string;
  telefon: string;
  email: string;
  rollen: string[];

  constructor(
    id: number,
    anrede: string,
    vorname: string,
    nachname: string,
    telefon: string,
    email: string,
    rollen: string[]
  ) {
    this.id = id;
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
  kontaktdaten: KontaktdatenDTO;
  ansprechpartner: AnsprechpartnerDTO[];

  constructor(
    id: string,
    name: string,
    traegerform: string,
    adresse: AdresseDTO,
    team: TeamDTO,
    kontaktdaten: KontaktdatenDTO,
    ansprechpartner: AnsprechpartnerDTO[]
  ) {
    this.id = id;
    this.name = name;
    this.traegerform = traegerform;
    this.adresse = adresse;
    this.team = team;
    this.kontaktdaten = kontaktdaten;
    this.ansprechpartner = ansprechpartner;
  }
}
