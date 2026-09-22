export default class AdresseDTO {
  telefon: string;
  fax: string;
  email: string;
  homepage: string;
  notfallTelefon: string;

  constructor(
    telefon: string,
    fax: string,
    email: string,
    homepage: string,
    notfallTelefon: string
  ) {
    this.telefon = telefon;
    this.fax = fax;
    this.email = email;
    this.homepage = homepage;
    this.notfallTelefon = notfallTelefon;
  }
}
