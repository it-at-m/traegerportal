import type EinrichtungDTO from "@/types/EinrichtungDTO.ts";

export class VorgangsstatusDTO {
  status: string;
  bemerkung: string;
  gruende: string[];

  constructor(
    status: string,
    bemerkung: string,
    gruende: string[]
  ) {
    this.status = status;
    this.bemerkung = bemerkung;
    this.gruende = gruende;
  }
}

export default class VorgaengeDTO {
  aktenzeichen: string;
  einrichtung: EinrichtungDTO;
  vorgangsart: string;
  status: VorgangsstatusDTO;
  letzteAenderung: Date;

  constructor(
    aktenzeichen: string,
    einrichtung: EinrichtungDTO,
    vorgangsart: string,
    status: VorgangsstatusDTO,
    letzteAenderung: Date
  ) {
    this.aktenzeichen = aktenzeichen;
    this.einrichtung = einrichtung;
    this.vorgangsart = vorgangsart;
    this.status = status;
    this.letzteAenderung = letzteAenderung;
  }
}
