export default class AdresseDTO {
  strasse: string;
  hausnummer: string;
  adresszusatz: string;
  adresszusatz2: string;
  plz: string;
  ort: string;

  constructor(
    strasse: string,
    hausnummer: string,
    adresszusatz: string,
    adresszusatz2: string,
    plz: string,
    ort: string
  ) {
    this.strasse = strasse;
    this.hausnummer = hausnummer;
    this.adresszusatz = adresszusatz;
    this.adresszusatz2 = adresszusatz2;
    this.plz = plz;
    this.ort = ort;
  }
}
