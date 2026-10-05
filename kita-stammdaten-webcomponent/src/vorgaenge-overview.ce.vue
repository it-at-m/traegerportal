<template>
  <muc-spinner
    v-if="loading"
    size="200px"
    text="Lade Vorgänge ..."
  />
  <div v-else-if="!dataLoadingError">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-card
      id="vorgaenge-card"
      title="Vorgänge"
      :href="traegerLink"
      :disabled="!traegerLink"
      style="width: 100%"
    >
      <template #content>
        <div>
          <muc-icon icon="file" />
          <b>{{ vorgangCount }} Vorgänge mit neuen Änderungen</b>
        </div>
        <muc-button variant="ghost" class="card-action-button">Alle Vorgänge<muc-icon icon="arrow-right" /></muc-button>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import { MucButton, MucCard, MucSpinner, MucIcon } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";

const vorgangCount = ref<number>(0);

const props = defineProps({
  detailsUrl: {
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
const emit = defineEmits(["loadingError", "unknownTraeger"]);

function loadVorgangCount() {
  loading.value = true;
  const service = new StammdatenService();
  service
    .getVorgangCount(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: number) => {
          vorgangCount.value = response;
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
      dataLoadingError.value = true;
      console.debug(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

const traegerLink = computed(() => {
  return `${props.detailsUrl}?&lg-idphint=ELSTER_NEZO`;
});

watch(
  () => props.token,
  (newToken, oldToken) => {
    if (newToken !== oldToken) {
      loadVorgangCount();
    }
  },
  { immediate: true }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.card-content {
  padding: 1rem !important;
}

.card-action-button {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
}

#vorgaenge-card {
  position: relative;
}
</style>
