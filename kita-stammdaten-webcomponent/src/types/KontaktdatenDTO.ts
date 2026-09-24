export default class AdresseDTO {
  telefon: string;
  fax: string;
  email: string;
  homepageUrl: string;
  notfallTelefon: string;

  constructor(
    telefon: string,
    fax: string,
    email: string,
    homepageUrl: string,
    notfallTelefon: string
  ) {
    this.telefon = telefon;
    this.fax = fax;
    this.email = email;
    this.homepageUrl = homepageUrl;
    this.notfallTelefon = notfallTelefon;
  }
}
