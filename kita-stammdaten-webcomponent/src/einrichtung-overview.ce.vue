<template>
  <div>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-input
      class="einrichtung-suche"
      type="text"
      placeholder="Einrichtung suchen"
    >
      <template #prefix><muc-icon icon="search" /></template>
    </muc-input>
    <muc-accordion id="einrichtung-accordion" multiple>
      <muc-accordion-item
        v-for="einrichtung in einrichtungen"
        :id="'einrichtung-accordion-item-' + einrichtung.id"
        :key="einrichtung.id"
        :header="einrichtung.name"
      >
        <template #subtitle>
          {{ einrichtung.adresse }}
          <a :href="bearbeitenFormularUrl(einrichtung.id)"
            ><muc-button variant="ghost"
              >Einrichtungsdetails<muc-icon icon="arrow-right" /></muc-button
          ></a>
        </template>
        <template #content>
          <span class="einrichtung-attribute"
            ><b>Kibigweb-ID:</b> {{ einrichtung.kibigwebid }}</span
          >
          <span class="einrichtung-attribute"
            ><b>Status:</b> {{ einrichtung.status }}</span
          >
        </template>
      </muc-accordion-item>
    </muc-accordion>
  </div>
</template>

<script setup lang="ts">
import {
  MucAccordion,
  MucAccordionItem,
  MucButton,
  MucIcon,
  MucInput,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { onMounted, ref } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import EinrichtungDTO from "@/types/EinrichtungDTO";

const props = defineProps<{
  stammdatenUrl: string;
}>();

function bearbeitenFormularUrl(einrichtungId: string): string {
  return `${props.stammdatenUrl}/traegerAnzeigen/${einrichtungId}`;
}

const einrichtungen = ref<EinrichtungDTO[]>();

function loadEinrichtungen() {
  const service = new StammdatenService();
  service
    .searchEinrichtungen()
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: EinrichtungDTO[]) => {
          einrichtungen.value = response;
        });
      } else {
        resp.text().then((errBody) => {
          throw Error(errBody);
        });
      }
    })
    .catch((error) => {
      console.debug(error);
    });
}

onMounted(() => {
  loadEinrichtungen();
});
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.einrichtung-suche {
  padding-left: 1.5rem;
}

.m-accordion__section {
  border: solid 1px var(--color-neutrals-blue);
  border-bottom: solid 5px var(--color-brand-main-blue);
  padding-left: 1rem;
  padding-right: 1rem;
}

.m-accordion__section-content {
  padding-bottom: 1rem;
}

.einrichtung-attribute {
  padding-right: 1rem;
}

.content-width {
  width: 100% !important;
}
</style>
