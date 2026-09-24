<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="!loggedIn">
    <muc-spinner
      v-if="loading"
      size="200px"
      text="Lade Träger ..."
    />
    <muc-callout
      v-else-if="dataLoadingError"
      type="error"
    >
      <template #content>
        <p>
          Die Trägerdaten können derzeit nicht geladen werden. Bitte versuchen
          Sie es zu einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div
      v-else
      style="width: 100%"
    >
      <h2 v-if="traeger">
        {{ textOrFallback(traeger.name) }}
      </h2>
      <muc-link
        label="Trägerdaten ändern"
        href="https://test81-k2.muenchen.de/intelliform/forms/integration/05/05/2277/index?eid=2"
        prepend-icon="pencil"
      />
      <muc-card
        v-if="traeger"
        id="traeger-card-allgemein"
        title="Allgemeines"
        :disabled="false"
      >
        <template #content>
          <div><b>Träger-ID:</b> {{ textOrFallback(traeger.id) }}</div>
          <div><b>Name:</b> {{ textOrFallback(traeger.name) }}</div>
          <div>
            <b>Rechtsform:</b> {{ textOrFallback(traeger.traegerform) }}
          </div>
          <div><b>Adresse: </b>{{ formatAdresse(traeger.adresse) }}</div>
          <div>
            <b>Zugeordnetes Team:</b> {{ formatTraegerTeam(traeger.team) }}
          </div>
        </template>
      </muc-card>
      <muc-card
        v-if="traeger"
        id="traeger-card-kontakt"
        title="Kontaktdaten des Trägers"
        :disabled="false"
      >
        <template #content>
          <div>
            <b>Telefonnummer:</b>
            {{ textOrFallback(traeger.kontaktdaten?.telefon) }}
          </div>
          <div>
            <b>Faxnummer:</b> {{ textOrFallback(traeger.kontaktdaten?.fax) }}
          </div>
          <div>
            <b>E-Mail-Adresse:</b>
            {{ textOrFallback(traeger.kontaktdaten?.email) }}
          </div>
          <div>
            <b>Homepage:</b>
            {{ textOrFallback(traeger.kontaktdaten?.homepageUrl) }}
          </div>
          <div>
            <b>Notfall-Telefonnummer:</b>
            {{ textOrFallback(traeger.kontaktdaten?.notfallTelefon) }}
          </div>
        </template>
      </muc-card>
      <muc-card
        v-if="traeger"
        id="traeger-card-ansprechpartner"
        title="Ansprechpersonen"
        :disabled="false"
      >
        <template #content>
          <table>
            <thead>
              <tr>
                <th>Anrede</th>
                <th>Vorname</th>
                <th>Nachname</th>
                <th>Telefonnummer</th>
                <th>E-Mail-Adresse</th>
                <th>Rolle</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="ansprechpartner in traeger.ansprechpartner"
                :key="ansprechpartner.id"
              >
                <td>
                  {{ textOrFallback(anrede.get(ansprechpartner.anrede)) }}
                </td>
                <td>{{ textOrFallback(ansprechpartner.vorname) }}</td>
                <td>{{ textOrFallback(ansprechpartner.nachname) }}</td>
                <td>{{ textOrFallback(ansprechpartner.telefon) }}</td>
                <td>{{ textOrFallback(ansprechpartner.email) }}</td>
                <td>{{ formatTraegerRollen(ansprechpartner) }}</td>
              </tr>
              <tr
                v-if="
                  !traeger.ansprechpartner ||
                  traeger.ansprechpartner.length == 0
                "
              >
                <td colspan="6">{{ noValueFallback }}</td>
              </tr>
            </tbody>
          </table>
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
import TraegerDTO from "@/types/TraegerDTO";
import { setAccessToken } from "@/util/constants";
import { anrede } from "@/util/enums";
import {
  formatAdresse,
  formatTraegerRollen,
  formatTraegerTeam,
  noValueFallback,
  textOrFallback,
} from "./util/format";

const { loggedIn } = useDBSLoginWebcomponentPlugin(_authChangedCallback);

function _authChangedCallback(authEventDetails?: AuthorizationEventDetails) {
  if (authEventDetails && authEventDetails.accessToken) {
    console.debug("Receiving new authevent...");

    setAccessToken(authEventDetails.accessToken);
    token.value = authEventDetails.accessToken;
  }
}

const token = ref<string>(
  "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJFMEgxUTRVUXpNdHR1WmF2eDRwdG9OcWZ3VFdCWDRfeVBVVC1lS2d3RkhNIn0.eyJleHAiOjE3OTAyNjY5ODgsImlhdCI6MTc5MDIzMDk4OCwianRpIjoib25ydHJvOjRhNzU1M2YxLWI3N2QtNDU1Ny04NDAwLWFjMDFjOGM5YzNjZCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODEwMC9hdXRoL3JlYWxtcy9sb2NhbF9yZWFsbSIsImF1ZCI6WyJsb2NhbCIsImFjY291bnQiXSwic3ViIjoiOTBiYWU1ZDgtZTQ5MS00NTBkLThiMDctYmIzNDA0ZjkwODY3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9jYWwiLCJzaWQiOiIwNzFkMWExZC1hMTYwLTRiZmUtOTA5Yi01MDBiMWMzZGY4ZGYiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8qIiwiaHR0cHM6Ly8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWxvY2FsX3JlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfSwibG9jYWwiOnsicm9sZXMiOlsicmVhZGVyIl19fSwic2NvcGUiOiJsb2NhbF9hdWRpZW5jZSBvcGVuaWQgZW1haWwgcHJvZmlsZSIsImRhdGVudWViZXJtaXR0bGVyUHNldWRvbnltSWQiOiJkdS1kOWExMTg3NDA2YjJmMTQxNDVlYmFmZDllMGJlYTEwMDAwMTAwMDA1IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJyZWFkZXIgcmVhZGVyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoicmVhZGVyIiwiZ2l2ZW5fbmFtZSI6InJlYWRlciIsImZhbWlseV9uYW1lIjoicmVhZGVyIiwiZW1haWwiOiJyZWFkZXJAcmVhZGVyLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfcmVhZGVyIl19.LKPoe39hHOdcCBNrj5ynjqpPRpEC3uyH8kpIjJstL8nOT_5y_zHnj6LtCYdZSoKLMHzaWh03_Qk8NArXMY7Ipu8xYUjlAEtr9RAAvQwZnnMnhlxTzwfmU384hQ0in6nVgxuFh9nKF4d8H2nQRNUhAJvNOEyZXEPGYgjy1GzmDfzPEmd2kOw5-K9bnjPozNj1Y33LGzsBrC_S3qNdNx9n4S2TflzyNrdQFu5qf9UWc4HoemT67x_bl7CTSmAAttCpIsze9iQUvPAmw964B7kBfYPUJy5x0alUnSrJEfWCBilWiwyG2XswZHqELTBMoAtv_PCGDiqhePH0q_mEm5WwdQ"
);

const traeger = ref<TraegerDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function loadTraeger() {
  console.debug("Loading traeger data...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  } else {
    loading.value = true;
    const service = new StammdatenService();
    service
      .getTraeger(token.value)
      .then((resp) => {
        if (resp.ok) {
          resp.json().then((response: TraegerDTO) => {
            traeger.value = response;
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
      loadTraeger();
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

table,
th,
td {
  border: 1px solid var(--color-neutrals-blue);
  border-collapse: collapse;
}

th,
td {
  padding-left: 0.4rem;
  padding-right: 0.4rem;
}
</style>
