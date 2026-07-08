export default class EinrichtungDTO {
  id: string;
  name: string;
  strasse: string;
  ort: string;
  kibigwebid: string;
  status: string;

  constructor(
    id: string,
    name: string,
    strasse: string,
    ort: string,
    kibigwebid: string,
    status: string
  ) {
    this.id = id;
    this.name = name;
    this.strasse = strasse;
    this.ort = ort;
    this.kibigwebid = kibigwebid;
    this.status = status;
  }

  adresse() {
    return this.strasse + " " + this.ort;
  }

  title() {
    return this.strasse + " / " + this.name;
  }
}
