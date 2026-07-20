<template>
  <muc-spinner
    v-if="loading"
    size="200px"
    text="Lade Träger ..."
  />
  <div v-else>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card
      v-if="traeger"
      id="traeger-card"
      :title="traeger.name"
      :href="hasLink ? traegerLink : undefined"
      :disabled="!traeger"
    >
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
import { MucCard, MucIcon, MucSpinner } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import TraegerDTO from "@/types/TraegerDTO";

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

function loadTraeger() {
  loading.value = true;
  const service = new StammdatenService();
  service
    .getTraeger(props.token)
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
    })
    .finally(() => {
      loading.value = false;
    });
}

const traegerLink = computed(() => {
  return `${props.stammdatenUrl}/traegerAnzeigen/${traeger.value?.id}`;
});

const hasLink = computed(() => {
  return props.stammdatenUrl && traeger.value;
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
