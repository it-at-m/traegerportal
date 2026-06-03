<template>
  <div>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card v-if="traeger" :title="traeger.name">
      <template #content>
        <div><muc-icon icon="account" /><b>Träger-ID:</b> {{ traeger.id }}</div>
        <div><muc-icon icon="home" /><b>Name:</b> {{ traeger.name }}</div>
        <div><muc-icon icon="web" /><b>Form:</b> {{ traeger.form }}</div>
        <div>
          <muc-icon icon="map-pin" /><b>Adresse:</b> {{ traeger.adresse }}
        </div>
        <div><muc-icon icon="user-group" /><b>Team:</b> {{ traeger.team }}</div>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import { MucCard, MucIcon } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";

import { ref } from "vue";
import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import TraegerDTO from "@/types/TraegerDTO";
import { onMounted } from "vue";

const traeger = ref<TraegerDTO>();

const props = defineProps<{
  detailUrl: string;
}>();

function loadTraeger() {
  const service = new StammdatenService();
  service
    .getTraeger()
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: TraegerDTO) => {
          traeger.value = response;
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
  loadTraeger();
})
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";
</style>
