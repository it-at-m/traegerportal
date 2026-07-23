import type AdresseDTO from "@/types/AdresseDTO";
import type EinrichtungDTO from "@/types/EinrichtungDTO";
import type { EinrichtungsstatusDTO } from "@/types/EinrichtungDTO";

export function getPreceededStringOrEmptyString(stringParameter: string) {
  return stringParameter ? " " + stringParameter : "";
}

export function formatStrasse(adresse: AdresseDTO): string {
  if (!adresse) {
    return "-";
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
    return "-";
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
