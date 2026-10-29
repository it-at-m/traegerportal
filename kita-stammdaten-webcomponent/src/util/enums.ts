export const anrede = new Map<string, string>([
  ["HERR", "Herr"],
  ["FRAU", "Frau"],
  ["DIVERS", "Divers"],
  ["KEINE_ANGABE", "keine Angabe"],
]);

export const einrichtungsstatus = new Map<string, string>([
  ["VORPLANUNG", "Vorplanung"],
  ["VOR_BETRIEB", "Vor Betrieb"],
  ["IN_BETRIEB", "In Betrieb"],
]);

export const vorgangsart = new Map<string, string>([
  ["ERTEILUNG_BE", "Erteilung einer BE"],
  ["BEANTRAGUNG_BE", "Beantragung einer BE ST"],
  ["BEANTRAGUNG_BE_A4", "Beantragung einer BE A4"],
  ["PERMA", "Personalmangel"],
  ["UNTERNEHMENSKONTO", "Unternehmenskonto"],
  ["EINRICHTUNGSDATEN_AENDERN", "Einrichtungsdaten ändern"],
  ["TRAEGERDATEN_AENDERN", "Trägerdaten ändern"],
]);

export const vorgangsstatus = new Map<string, string>([
  ["BERATUNG_OFFEN", "Beratung offen"],
  ["BERATUNG_GESCHLOSSEN", "Beratung geschlossen"],
  ["ANTRAG_OFFEN", "Antrag offen"],
  ["ANTRAG_GENEHMIGT", "Antrag genehmigt"],
  ["ANTRAG_ABGELEHNT", "Antrag abgelehnt"],
  ["ANTRAG_GENEHMIGT_MIT_AUFLAGEN", "Antrag genehmigt mit Auflagen"],
  ["ANTRAG_WIDERSPRUCH", "Antrag Widerspruch"],
  ["ANTRAG_KLAGE", "Antrag Klage"],
  ["IN_BEARBEITUNG", "In Bearbeitung"],
  ["RUECKFRAGE", "Informationen angefragt"],
]);

export const ansprechpartnerRollen = new Map<string, string>([
  ["GESCHAEFTSFUEHRUNG", "Geschäftsführung"],
  ["VORSTAND", "Vorstand"],
  ["FACHBERATUNG", "Fachberatung"],
  ["SAEULE1", "(Ansprechpartner*in für) Säule 1"],
  [
    "BELEGUNG_DER_EINRICHTUNG",
    "(Ansprechpartner*in für) Belegung der Einrichtung",
  ],
  ["SONSTIGE", "Sonstige"],
]);
