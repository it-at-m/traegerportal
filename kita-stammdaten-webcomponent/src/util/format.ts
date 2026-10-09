import type AdresseDTO from "@/types/AdresseDTO";
import type EinrichtungDTO from "@/types/EinrichtungDTO";
import type { EinrichtungsstatusDTO } from "@/types/EinrichtungDTO";
import type { AnsprechpartnerDTO, TeamDTO } from "@/types/TraegerDTO";
import type VorgaengeDTO from "@/types/VorgaengeDTO.ts";
import type { VorgangsstatusDTO } from "@/types/VorgaengeDTO.ts";

import {
  einrichtungsstatus,
  vorgangsart,
  vorgangsstatus,
} from "@/util/enums.ts";

export const noValueFallback = "Keine Daten hinterlegt";

export function getPreceededStringOrEmptyString(stringParameter: string) {
  return stringParameter ? " " + stringParameter : "";
}

export function formatStrasse(adresse: AdresseDTO): string {
  if (!adresse) {
    return noValueFallback;
  } else {
    const strasse = adresse.strasse ? adresse.strasse : "";
    const hausnummer = getPreceededStringOrEmptyString(adresse.hausnummer);
    const adresszusatz = getPreceededStringOrEmptyString(adresse.adresszusatz);
    const adresszusatz2 = getPreceededStringOrEmptyString(
      adresse.adresszusatz2
    );
    return `${strasse}${hausnummer}${adresszusatz}${adresszusatz2}`;
  }
}

export function formatAdresse(adresse: AdresseDTO): string {
  if (!adresse) {
    return noValueFallback;
  } else {
    const formattedStrasse = formatStrasse(adresse);
    const plz = "," + getPreceededStringOrEmptyString(adresse.plz);
    const ort = getPreceededStringOrEmptyString(adresse.ort);
    return `${formattedStrasse}${plz}${ort}`;
  }
}

export function formatVorgangsart(dto: VorgaengeDTO) {
  return vorgangsart.get(dto.vorgangsart);
}

export function formatVorgangsstatus(dto: VorgangsstatusDTO) {
  return vorgangsstatus.get(dto.status);
}

export function formatEinrichtungsstatus(dto: EinrichtungsstatusDTO) {
  return einrichtungsstatus.get(dto.status);
}

export function formatEinrichtungTitle(einrichtung: EinrichtungDTO) {
  return `${formatStrasse(einrichtung.adresse)} / ${einrichtung.name}`;
}

export function formatTraegerTeam(team: TeamDTO) {
  if (!team) {
    return noValueFallback;
  }
  return `${team.name} (${team.postfach})`;
}

export function formatTraegerRollen(
  ansprechpartner: AnsprechpartnerDTO | undefined
) {
  if (
    !ansprechpartner ||
    !ansprechpartner.rollen ||
    ansprechpartner.rollen.length == 0
  ) {
    return noValueFallback;
  }
  return ansprechpartner.rollen.join(", ");
}

export function textOrFallback(text: string | undefined | null | number) {
  return text ? text.toString() : noValueFallback;
}

export function formatDate(value?: string | number | Date | null) {
  if (!value) return noValueFallback;

  const date = new Date(value);
  return isNaN(date.getTime())
    ? noValueFallback
    : date.toLocaleDateString("de-DE");
}