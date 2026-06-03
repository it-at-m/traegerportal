<template>
  <div>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-input class="einrichtung-suche" type="text" placeholder="Einrichtung suchen">
      <template #prefix><muc-icon icon="search" /></template>
    </muc-input>
    <muc-accordion multiple>
      <muc-accordion-item v-for="einrichtung in einrichtungen" :key="einrichtung.id" :header="einrichtung.name">
        <template #subtitle>
          {{ einrichtung.adresse }}
          <muc-button variant="ghost" @click.stop="test()">Einrichtungsdetails<muc-icon icon="arrow-right" /></muc-button>
        </template>
        <template #content>
          <span class="einrichtung-attribute"><b>Kibigweb-ID:</b> {{ einrichtung.kibigwebid }}</span>
          <span class="einrichtung-attribute"><b>Status:</b> {{ einrichtung.status }}</span>
        </template>
      </muc-accordion-item>
    </muc-accordion>
  </div>
</template>

<script setup lang="ts">
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";

import { MucButton, MucIcon, MucAccordion, MucAccordionItem, MucInput } from "@muenchen/muc-patternlab-vue";

import EinrichtungDTO from '@/types/EinrichtungDTO';

function test() : void {
  console.debug("Test");
}

const props = withDefaults(
    defineProps<{
      /**
       * DTO representing a Traeger
       */
      einrichtungen?: EinrichtungDTO[];
    }>(),
    { einrichtungen: () => [
      new EinrichtungDTO("123", "Testeinrichtung 1", "Teststraße 1, 80331 München", "1620011000", "In Betrieb"),
      new EinrichtungDTO("124", "Testeinrichtung 2", "Teststraße 2, 80331 München", "1620011001", "Vorplanung")
    ]}
);
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
