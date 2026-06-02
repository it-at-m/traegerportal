<template>
  <div>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card :title="traeger.name">
      <template #content>
        <div><muc-icon icon="account" /><b>Träger-ID:</b> {{ traeger.id }}</div>
        <div><muc-icon icon="office-building" /><b>Name:</b> {{ traeger.name }}</div>
        <div><muc-icon icon="web" /><b>Form:</b> {{ traeger.form }}</div>
        <div><muc-icon icon="map-marker" /><b>Adresse:</b> {{ traeger.adresse }}</div>
        <div><muc-icon icon="account-multiple" /><b>Team:</b> {{ traeger.team }}</div>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";

import { MucCard } from "@muenchen/muc-patternlab-vue";
import { MucIcon } from "@muenchen/muc-patternlab-vue";

import TraegerDTO from '@/types/TraegerDTO';

const props = withDefaults(
    defineProps<{
      /**
       * DTO representing a Traeger
       */
      traeger?: TraegerDTO;
    }>(),
    { traeger: new TraegerDTO("123", "AWO München", "gemeinnützige GmbH", "Dachauer Straße 12, 80331 München", "Team 1") }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";
</style>
