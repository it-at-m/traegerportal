<template>
  <h2 class="traegerportal-ueberschrift container">
    <muc-icon icon="home" />Meine weiteren Einrichtungen
  </h2>
  <traegerportal-triple-slider
    v-if="einrichtungen && einrichtungen.length > 0"
    :values="einrichtungen"
  >
    <template #default="{ element }">
      <einrichtung-card
        :key="element.id"
        :einrichtung="element"
        :einrichtung-link="
          getUrlWithOriginAsBaseAndDefaultParams(detailsUrl ?? '', {
            einrichtungId: element.id,
          }) ?? ''
        "
      />
    </template>
  </traegerportal-triple-slider>
</template>

<script setup lang="ts" generic="T extends object">
import type EinrichtungDTO from "@/types/EinrichtungDTO";

import { ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants";
import EinrichtungCard from "./EinrichtungCard.vue";
import TraegerportalTripleSlider from "./TraegerportalTripleSlider.vue";

const props = defineProps<{
  token: string;
  detailsUrl?: string;
}>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

const einrichtungen = ref<EinrichtungDTO[]>();

const emit = defineEmits(["loadingError"]);

function loadEinrichtungen() {
  console.debug("Loading einrichtungen...");

  if (!props.token) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

  loading.value = true;
  const service = new StammdatenService();
  service
    .searchEinrichtungen(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response) => {
          einrichtungen.value = response.content as EinrichtungDTO[];
          einrichtungen.value.sort((a, b) => a.name.localeCompare(b.name));
          dataLoadingError.value = false;
        });
      } else {
        resp.text().then((errBody) => {
          dataLoadingError.value = true;
          emit("loadingError");
          throw Error(errBody);
        });
      }
    })
    .catch((error) => {
      console.debug(error);
      dataLoadingError.value = true;
      emit("loadingError");
    })
    .finally(() => {
      loading.value = false;
    });
}

watch(
  () => props.token,
  (newToken, oldToken) => {
    if (newToken !== oldToken && !!newToken) {
      loadEinrichtungen();
    }
  },
  { immediate: true }
);
</script>

<style scoped></style>
```
