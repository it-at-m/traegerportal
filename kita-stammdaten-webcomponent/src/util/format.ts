import type AdresseDTO from "@/types/AdresseDTO";
import type EinrichtungDTO from "@/types/EinrichtungDTO";
import type { EinrichtungsstatusDTO } from "@/types/EinrichtungDTO";
import type { AnsprechpartnerDTO, TeamDTO } from "@/types/TraegerDTO";
import type VorgaengeDTO from "@/types/VorgaengeDTO.ts";
import type { VorgangsstatusDTO } from "@/types/VorgaengeDTO.ts";

import {
  anrede,
  ansprechpartnerRollen,
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
  if (!dto.vorgangsart) return noValueFallback;
  const _vorgangsart = vorgangsart.get(dto.vorgangsart);
  if (!_vorgangsart) return dto.vorgangsart;
  return _vorgangsart;
}

export function formatVorgangsstatus(dto: VorgangsstatusDTO) {
  if (!dto.status) return noValueFallback;
  const _status = vorgangsstatus.get(dto.status);
  if (!_status) return dto.status;
  return _status;
}

export function formatEinrichtungsstatus(dto: EinrichtungsstatusDTO) {
  if (!dto.status) return noValueFallback;
  const _status = einrichtungsstatus.get(dto.status);
  if (!_status) return dto.status;
  return _status;
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
  return [...ansprechpartner.rollen]
    .sort((a, b) => a.localeCompare(b))
    .filter((rolle) => rolle != "SAEULE1")
    .map((rolle) => {
      const _rolle = formatRolle(rolle);

      if (rolle == "SONSTIGE" && ansprechpartner.bemerkung) {
        return _rolle + " (" + ansprechpartner.bemerkung + ")";
      }

      return _rolle;
    })
    .join(", ");
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

export function formatRolle(value: string) {
  if (!value) return undefined;
  const _rolle = ansprechpartnerRollen.get(value);
  if (!_rolle) return value;
  return _rolle;
}

export function formatAnrede(value: string) {
  if (!value) return noValueFallback;
  const _anrede = anrede.get(value);
  if (!_anrede) return value;
  return _anrede;
}

export function formatBetreff(dto?: VorgaengeDTO): string | undefined {
  if (!dto) {
    return undefined;
  }

  if (dto.vorgangsart === "EINRICHTUNGSDATEN_AENDERN") {
    return concatIfNotBlank(
      ", ",
      dto.einrichtung.name,
      formatAdresse(dto.einrichtung.adresse),
      dto.einrichtung.kibigWebId
    );
  }

  if (dto.vorgangsart === "TRAEGERDATEN_AENDERN") {
    return concatIfNotBlank(
      ", ",
      dto.traeger.name,
      formatAdresse(dto.traeger.adresse)
    );
  }

  return undefined;
}

export function concatIfNotBlank(
  separator: string,
  ...strings: (string | null | undefined)[]
): string {
  return strings.filter((s) => s != null && s.trim() !== "").join(separator);
}
