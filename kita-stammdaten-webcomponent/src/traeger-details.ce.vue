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
          <data-table
            :items="traeger.ansprechpartner"
            :columns="columns"
            row-key="id"
            :pagination="false"
            caption="Ansprechpersonen"
          >
            <template #cell-anrede="{ row }">
              {{ formatAnrede(row.anrede) }}
            </template>
            <template #cell-rollen="{ row }">
              {{ formatTraegerRollen(row) }}
            </template>
          </data-table>
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
import {
  MucCallout,
  MucCard,
  MucLink,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import type { TableColumn } from "./components/DataTable.vue";
import type { AnsprechpartnerDTO } from "./types/TraegerDTO";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import TraegerDTO from "./types/TraegerDTO";
import DataTable from "./components/DataTable.vue";
import { AuthService } from "./util/AuthService.ts";
import {
  formatAdresse,
  formatAnrede,
  formatTraegerRollen,
  formatTraegerTeam,
  textOrFallback,
} from "./util/format";

const columns = computed<TableColumn<AnsprechpartnerDTO>[]>(() => [
  {
    key: "anrede",
    label: "Anrede",
  },
  {
    key: "vorname",
    label: "Vorname",
  },
  {
    key: "nachname",
    label: "Nachname",
  },
  {
    key: "telefon",
    label: "Telefonnummer",
  },
  {
    key: "email",
    label: "E-Mail-Adresse",
  },
  {
    key: "rollen",
    label: "Rollen",
    value: (ansprechperson) => formatTraegerRollen(ansprechperson),
  },
]);

const { token, loggedIn } = new AuthService();

const traeger = ref<TraegerDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function loadTraeger() {
  console.debug("Loading traeger data...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

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

watch(
  token,
  (newToken) => {
    if (newToken) {
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
