export default class TraegerDTO {
    id: string;
    name: string;
    form: string;
    adresse: string;
    team: string;
  
    constructor(id: string, name: string, form: string, adresse: string, team: string) {
      this.id = id;
      this.name = name;
      this.form = form;
      this.adresse = adresse;
      this.team = team;
    }
  }
  