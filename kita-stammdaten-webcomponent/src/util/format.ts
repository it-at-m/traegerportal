import type AdresseDTO from "@/types/AdresseDTO";
import type EinrichtungDTO from "@/types/EinrichtungDTO";
import type { EinrichtungsstatusDTO } from "@/types/EinrichtungDTO";
import type { AnsprechpartnerDTO, TeamDTO } from "@/types/TraegerDTO";
import type VorgaengeDTO from "@/types/VorgaengeDTO.ts";
import type { VorgangsstatusDTO } from "@/types/VorgaengeDTO.ts";

import {
  anrede,
  ansprechpartnerRollen,
  einrichtungsartenLhm,
  einrichtungsstatus,
  vorgangsart,
  vorgangsstatus,
} from "@/util/enums.ts";

export const noValueFallback = "Keine Daten";

/**
 * recursively transforms the given object into a searchable string.
 * All keys are omitted and all parsable values included in localized form whereever possible.
 */
export function normalizeForSearch(value: unknown): string {
  const formatValue = (currentValue: unknown): unknown => {
    if (currentValue instanceof Date) {
      return currentValue.toLocaleDateString();
    }

    if (typeof currentValue === "string") {
      return currentValue
        .replace(
          /\b(\d{4})-(\d{2})-(\d{2})(?:T\d{2}:\d{2}(?::\d{2}(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?)?\b/g,
          (isoValue, year, month, day) => {
            // Treat date-only ISO values as local dates (avoids timezone shifts).
            const date = isoValue.includes("T")
              ? new Date(isoValue)
              : new Date(Number(year), Number(month) - 1, Number(day));

            return Number.isNaN(date.getTime())
              ? isoValue
              : date.toLocaleDateString();
          }
        )
        .toLowerCase()
        .replace(/[_]/g, " ");
    }

    if (Array.isArray(currentValue)) {
      return currentValue.map(formatValue);
    }

    if (currentValue !== null && typeof currentValue === "object") {
      return Object.values(currentValue)
        .map((entryValue) => formatValue(entryValue))
        .join(" ");
    }

    return currentValue;
  };

  return JSON.stringify(formatValue(value));
}

export function getPreceededStringOrEmptyString(stringParameter: string) {
  return stringParameter ? " " + stringParameter : "";
}

export function formatEinrichtungsart(art: string | undefined): string {
  if (!art) {
    return "";
  }
  const resolved = einrichtungsartenLhm.get(art) ?? "";
  return resolved;
}

export function formatStrasse(adresse: AdresseDTO | null | undefined): string {
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

export function formatAdresse(adresse: AdresseDTO | null | undefined): string {
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

export function formatEinrichtungsstatus(
  dto: EinrichtungsstatusDTO | null | undefined
) {
  if (!dto || !dto.status) return noValueFallback;
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

export function formatAnsprechpartnerFullname(value: AnsprechpartnerDTO) {
  const fullname = `${value.vorname ?? ""} ${value.nachname ?? ""}`;
  return fullname.trim();
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
