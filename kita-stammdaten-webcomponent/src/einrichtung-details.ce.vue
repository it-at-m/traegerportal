<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-spinner
      v-if="loading"
      size="200px"
      text="Lade Einrichtung ..."
    />
    <muc-callout
      v-else-if="dataLoadingError"
      type="error"
    >
      <template #content>
        <p>
          Die Einrichtungsdaten können derzeit nicht geladen werden. Bitte
          versuchen Sie es zu einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div
      v-else
      style="width: 100%"
    >
      <h2 v-if="einrichtung">
        {{ textOrFallback(einrichtung.name) }}
      </h2>
      <muc-link
        label="Einrichtungsdaten ändern"
        href="https://test81-k2.muenchen.de/intelliform/forms/integration/05/05/2277/index?eid=2"
        prepend-icon="pencil"
      />
      <muc-card
        v-if="einrichtung"
        id="einrichtung-card-allgemein"
        title="Allgemeines"
        :disabled="false"
      >
        <template #content>
          <div><b>Name:</b> {{ textOrFallback(einrichtung.name) }}</div>
          <div>
            <b>KIGIB.web-Nummer:</b> {{ textOrFallback(einrichtung.merkmale?.kibigWebId) }}
          </div>
          <div><b>Adresse: </b>{{ formatAdresse(einrichtung.adresse) }}</div>
          <div>
            <b>Stadtbezirk: </b
            >{{ textOrFallback(einrichtung.stadtbezirk?.name) }}
          </div>
          <div>
            <b>Schulsprengel: </b
            >{{ textOrFallback(einrichtung.schulsprengel?.schule) }}
          </div>
        </template>
      </muc-card>
      <muc-card
        v-if="einrichtung"
        id="einrichtung-card-kontakt"
        title="Kontaktdaten der Einrichtung"
        :disabled="false"
      >
        <template #content>
          <div>
            <b>Telefon:</b>
            {{ textOrFallback(einrichtung.kontaktdaten?.telefon) }}
          </div>
          <div>
            <b>Fax:</b> {{ textOrFallback(einrichtung.kontaktdaten?.fax) }}
          </div>
          <div>
            <b>Email:</b> {{ textOrFallback(einrichtung.kontaktdaten?.email) }}
          </div>
          <div>
            <b>Homepage:</b>
            {{ textOrFallback(einrichtung.kontaktdaten?.homepageUrl) }}
          </div>
        </template>
      </muc-card>
      <muc-card
        v-if="einrichtung"
        id="einrichtung-card-plaetze"
        title="Platzstruktur der Einrichtung"
        :disabled="true"
      >
        <template #content>
          <div>
            <b>Gesamt: </b>
            {{
              textOrFallback(
                einrichtung.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeGesamt
              )
            }}
          </div>
          <div>
            <b>Kinderkrippe: </b>
            {{
              textOrFallback(
                einrichtung.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeKinderkrippe
              )
            }}
          </div>
          <div>
            <b>kindergarten: </b>
            {{
              textOrFallback(
                einrichtung.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeKindergarten
              )
            }}
          </div>
          <div>
            <b>Hort: </b>
            {{
              textOrFallback(
                einrichtung.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeHort
              )
            }}
          </div>
        </template>
      </muc-card>
    </div>
  </div>
  <div v-else>
    <muc-callout type="info">
      <template #content>
        <p>Um diese Inhalte anzuzeigen, müssen Sie sich anmelden.</p>
      </template>
    </muc-callout>
  </div>
</template>

<script setup lang="ts">
import type AuthorizationEventDetails from "@/types/AuthorizationEventDetails.ts";

import {
  MucCallout,
  MucCard,
  MucLink,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import { useDBSLoginWebcomponentPlugin } from "@/composables/DBSLoginWebcomponentPlugin.ts";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import { setAccessToken } from "@/util/constants";
import { formatAdresse, textOrFallback } from "./util/format";

const { loggedIn } = useDBSLoginWebcomponentPlugin(_authChangedCallback);

function _authChangedCallback(authEventDetails?: AuthorizationEventDetails) {
  if (authEventDetails && authEventDetails.accessToken) {
    console.debug("Receiving new authevent...");

    setAccessToken(authEventDetails.accessToken);
    token.value = authEventDetails.accessToken;
  }
}

const token = ref<string>();

const einrichtung = ref<EinrichtungDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function getEinrichtungId() {
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const einrichtungId = urlParams.get("einrichtungId");

  return einrichtungId;
}

function loadEinrichtung(einrichtungId: string | null) {
  console.debug("Loading Einrichtung data for id " + einrichtungId + " ...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  } else if (!einrichtungId) {
    console.debug("Skipping, because no einrichtungId is specified.");
    return;
  } else {
    loading.value = true;
    const service = new StammdatenService();
    service
      .getEinrichtung(token.value, einrichtungId)
      .then((resp) => {
        if (resp.ok) {
          resp.json().then((response: EinrichtungDTO) => {
            einrichtung.value = response;
            dataLoadingError.value = false;
          });
        } else {
          resp.text().then((errBody) => {
            dataLoadingError.value = true;
            throw Error(errBody);
          });
        }
      })
      .catch((error) => {
        dataLoadingError.value = true;
        console.debug(error);
      })
      .finally(() => {
        loading.value = false;
      });
  }
}

watch(
  () => token.value,
  (newToken, oldToken) => {
    if (newToken !== oldToken) {
      loadEinrichtung(getEinrichtungId());
    }
  },
  { immediate: true }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.card {
  margin-bottom: 1rem;
}
</style>
