export default class EinrichtungDTO {
    id: string;
    name: string;
    adresse: string;
    kibigwebid: string;
    status: string;
  
    constructor(id: string, name: string, adresse: string, kibigwebid: string, status: string) {
      this.id = id;
      this.name = name;
      this.adresse = adresse;
      this.kibigwebid = kibigwebid;
      this.status = status;
    }
  }
  