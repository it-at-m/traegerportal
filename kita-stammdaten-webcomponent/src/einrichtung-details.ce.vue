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
  <div v-else>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card
      v-if="einrichtung"
      id="traeger-card"
      :title="einrichtung.name"
      :disabled="true"
    >
      <template #content>
        <div>
          <muc-icon icon="account" /><b>Einrichtungs-ID:</b>
          {{ einrichtung.id }}
        </div>
        <div><muc-icon icon="home" /><b>Name:</b> {{ einrichtung.name }}</div>
        <div>
          <muc-icon icon="web" /><b>Status:</b>
          {{ formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus) }}
        </div>
        <div>
          <muc-icon icon="map-pin" /><b>Adresse:</b>
          {{ formatAdresse(einrichtung.adresse) }}
        </div>
        <div>
          <muc-icon icon="user-group" /><b>Kibigwebid:</b>
          {{ einrichtung.merkmale.kibigWebId }}
        </div>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import { MucCallout, MucIcon, MucSpinner } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import { formatAdresse, formatEinrichtungsstatus } from "./util/format";

const props = defineProps({
  token: {
    type: String,
    default: null,
  },
  authLoading: {
    type: Boolean,
    default: false,
  },
  einrichtungId: {
    type: String,
    default: null,
  },
});

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

const einrichtung = ref<EinrichtungDTO>();

function loadEinrichtung() {
  loading.value = true;
  const service = new StammdatenService();
  service
    .getEinrichtung(props.token, props.einrichtungId)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response) => {
          einrichtung.value = response as EinrichtungDTO;
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
      console.debug(error);
      dataLoadingError.value = true;
    })
    .finally(() => {
      loading.value = false;
    });
}

watch(
  () => props.token,
  (newToken, oldToken) => {
    if (newToken !== oldToken) {
      loadEinrichtung();
    }
  },
  { immediate: true }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.m-component-accordion {
  padding-top: 0 !important;
}

.m-component-accordion .container {
  padding-left: 0;
  padding-right: 0;
}

.m-accordion__section {
  border: solid 1px var(--color-neutrals-blue);
  border-bottom: solid 5px var(--color-brand-main-blue) !important;
  padding-left: 1rem;
  padding-right: 1rem;
  margin-bottom: 16px;
}

.m-accordion__section-button {
  padding-top: 16px;
  padding-bottom: 16px;
}

.einrichtung-attribute {
  padding-right: 1rem;
}

.content-width {
  width: 100% !important;
}
</style>
