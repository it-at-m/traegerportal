export default class AnsprechpartnerDTO {
  name: string;
  postfach: string;

  constructor(name: string, postfach: string) {
    this.name = name;
    this.postfach = postfach;
  }
}
