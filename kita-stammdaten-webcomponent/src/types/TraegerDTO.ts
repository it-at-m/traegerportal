import type AdresseDTO from "./AdresseDTO";

export default class TraegerDTO {
  id: string;
  name: string;
  form: string;
  adresse: AdresseDTO;
  team: string;

  constructor(
    id: string,
    name: string,
    form: string,
    adresse: AdresseDTO,
    team: string
  ) {
    this.id = id;
    this.name = name;
    this.form = form;
    this.adresse = adresse;
    this.team = team;
  }
}
