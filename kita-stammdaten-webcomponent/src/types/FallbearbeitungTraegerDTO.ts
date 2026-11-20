import type AdresseDTO from "./AdresseDTO";

export class TeamDTO {
  name: string;
  postfach: string;

  constructor(name: string, postfach: string) {
    this.name = name;
    this.postfach = postfach;
  }
}

export default class TraegerDTO {
  id: string;
  name: string;
  adresse: AdresseDTO;
  team: TeamDTO;

  constructor(id: string, name: string, adresse: AdresseDTO, team: TeamDTO) {
    this.id = id;
    this.name = name;
    this.adresse = adresse;
    this.team = team;
  }
}
