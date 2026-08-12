<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
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
      <muc-intro
        v-if="traeger"
        variant="overview"
        :title="textOrFallback(traeger.name)"
      >
        <template #default>
          <muc-link
            label="Trägerdaten ändern"
            href="https://test81-k2.muenchen.de/intelliform/forms/integration/05/05/2277/index?eid=2"
            prepend-icon="pencil"
          />
        </template>
      </muc-intro>
      <muc-card
        v-if="traeger"
        id="traeger-card-allgemein"
        title="Allgemeines"
        :disabled="false"
      >
        <template #content>
          <div><b>Träger-ID:</b> {{ textOrFallback(traeger.id) }}</div>
          <div><b>Name:</b> {{ textOrFallback(traeger.name) }}</div>
          <div><b>Form:</b> {{ textOrFallback(traeger.traegerform) }}</div>
          <div><b>Adresse: </b>{{ formatAdresse(traeger.adresse) }}</div>
          <div><b>Team:</b> {{ formatTraegerTeam(traeger.team) }}</div>
        </template>
      </muc-card>
      <muc-card
        v-if="traeger"
        id="traeger-card-kontakt"
        title="Kontaktdaten des Trägers"
        :disabled="false"
      >
        <template #content>
          <div><b>Telefon:</b> {{ textOrFallback(traeger.telefon) }}</div>
          <div><b>Fax:</b> {{ textOrFallback(traeger.fax) }}</div>
          <div><b>Email:</b> {{ textOrFallback(traeger.email) }}</div>
          <div><b>Homepage:</b> {{ textOrFallback(traeger.homepage) }}</div>
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
                <th>Telefon</th>
                <th>E-Mail</th>
                <th>Rolle(n)</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="ansprechpartner in traeger.ansprechpartner"
                :key="ansprechpartner.id"
              >
                <td>{{ textOrFallback(ansprechpartner.anrede) }}</td>
                <td>{{ textOrFallback(ansprechpartner.vorname) }}</td>
                <td>{{ textOrFallback(ansprechpartner.nachname) }}</td>
                <td>{{ textOrFallback(ansprechpartner.telefon) }}</td>
                <td>{{ textOrFallback(ansprechpartner.email) }}</td>
                <td>{{ formatTraegerRollen(ansprechpartner) }}</td>
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
  MucIntro,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import { useDBSLoginWebcomponentPlugin } from "@/composables/DBSLoginWebcomponentPlugin.ts";
import TraegerDTO from "@/types/TraegerDTO";
import { setAccessToken } from "@/util/constants";
import {
  formatAdresse,
  formatTraegerRollen,
  formatTraegerTeam,
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

const token = ref<string>();

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
</style>
