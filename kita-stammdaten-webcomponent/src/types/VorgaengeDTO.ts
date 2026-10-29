import type FallbearbeitungEinrichtungDTO from "@/types/FallbearbeitungEinrichtungDTO.ts";
import type FallbearbeitungTraegerDTO from "@/types/FallbearbeitungTraegerDTO.ts";

export class VorgangsstatusDTO {
  status: string;
  bemerkung: string;
  gruende: string[];

  constructor(status: string, bemerkung: string, gruende: string[]) {
    this.status = status;
    this.bemerkung = bemerkung;
    this.gruende = gruende;
  }
}

export default class VorgaengeDTO {
  aktenzeichen: string;
  traeger: FallbearbeitungTraegerDTO;
  einrichtung: FallbearbeitungEinrichtungDTO;
  vorgangsart: string;
  status: VorgangsstatusDTO;
  letzteAenderung: Date;
  changedSinceLastExternalRead: boolean;

  constructor(
    aktenzeichen: string,
    traeger: FallbearbeitungTraegerDTO,
    einrichtung: FallbearbeitungEinrichtungDTO,
    vorgangsart: string,
    status: VorgangsstatusDTO,
    letzteAenderung: Date,
    changedSinceLastExternalRead: boolean
  ) {
    this.aktenzeichen = aktenzeichen;
    this.traeger = traeger;
    this.einrichtung = einrichtung;
    this.vorgangsart = vorgangsart;
    this.status = status;
    this.letzteAenderung = letzteAenderung;
    this.changedSinceLastExternalRead = changedSinceLastExternalRead;
  }
}
