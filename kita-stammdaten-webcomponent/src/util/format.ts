import type AdresseDTO from "@/types/AdresseDTO";
import type EinrichtungDTO from "@/types/EinrichtungDTO";
import type { EinrichtungsstatusDTO } from "@/types/EinrichtungDTO";
import type { AnsprechpartnerDTO, TeamDTO } from "@/types/TraegerDTO";

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

export function formatEinrichtungsstatus(status: EinrichtungsstatusDTO) {
  const values = new Map<string, string>([
    ["VORPLANUNG", "Vorplanung"],
    ["VOR_BETRIEB", "Vor Betrieb"],
    ["IN_BETRIEB", "In Betrieb"],
  ]);
  return values.get(status.status);
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

export function textOrFallback(text: string | undefined | null) {
  return text ? text : noValueFallback;
}
