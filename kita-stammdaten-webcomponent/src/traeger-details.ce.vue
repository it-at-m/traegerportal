<template>
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
        Die Schnittstelle ist nicht erreichbar. Bitte versuchen Sie es zu einem
        späteren Zeitpunkt erneut.
      </p>
    </template>
  </muc-callout>
  <div v-else style="width: 100%">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card
      v-if="traeger"
      id="traeger-card-allgemein"
      title="Allgemeines"
      :disabled="false"
    >
      <template #content>
        <div><b>Träger-ID:</b> {{ traeger.id }}</div>
        <div><b>Name:</b> {{ traeger.name }}</div>
        <div><b>Form:</b> {{ traeger.form }}</div>
        <div><b>Adresse:</b>{{ formatAdresse(traeger.adresse) }}</div>
        <div><b>Team:</b> {{ traeger.team }}</div>
      </template>
    </muc-card>
    <muc-card
      v-if="traeger"
      id="traeger-card-kontakt"
      title="Kontaktdaten des Trägers"
      :disabled="false"
    >
      <template #content>
        <div><b>Telefon:</b> {{ traeger.telefon }}</div>
        <div><b>Fax:</b> {{ traeger.fax }}</div>
        <div><b>Email:</b> {{ traeger.email }}</div>
        <div><b>Homepage:</b> {{ traeger.homepage }}</div>
      </template>
    </muc-card>
    <muc-card
      v-if="traeger"
      id="traeger-card-ansprechpartner"
      title="Ansprechpartner"
      :disabled="false"
    >
      <template #content>
        <div><b>Anrede:</b> {{ firstAnsprechpartner?.anrede }}</div>
        <div><b>Vorname:</b> {{ firstAnsprechpartner?.vorname }}</div>
        <div><b>Nachname:</b> {{ firstAnsprechpartner?.nachname }}</div>
        <div><b>Telefon:</b> {{ firstAnsprechpartner?.telefon }}</div>
        <div><b>E-Mail:</b> {{ firstAnsprechpartner?.email }}</div>
        <div><b>Rolle(n):</b> {{ firstAnsprechpartner?.rollen }}</div>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import {
  MucCallout,
  MucCard,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import TraegerDTO from "@/types/TraegerDTO";
import { formatAdresse } from "./util/format";

const traeger = ref<TraegerDTO>();

const props = defineProps({
  stammdatenUrl: {
    type: String,
    default: null,
  },
  token: {
    type: String,
    default: null,
  },
  authLoading: {
    type: Boolean,
    default: false,
  },
});

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function loadTraeger() {
  loading.value = true;
  const service = new StammdatenService();
  service
    .getTraeger(props.token)
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

const firstAnsprechpartner = computed(() => {
  return traeger.value?.ansprechpartner?.at(0);
});

watch(
  () => props.token,
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
</style>
