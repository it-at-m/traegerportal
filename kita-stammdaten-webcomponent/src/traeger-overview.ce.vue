<template>
  <muc-spinner
    v-if="loading"
    size="200px"
    text="Lade Träger ..."
  />
  <div v-else-if="!dataLoadingError">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-intro
      :title="traeger?.name ?? ''"
      tagline="Mein Bereich"
      :divider="false"
    >
      <template #default>
        <a :href="hasLink ? traegerLink : undefined">
          <muc-button >
            Trägerdaten ansehen
            <muc-icon icon="arrow-right" />
          </muc-button>
        </a>
      </template>
    </muc-intro>
    <slot />
    <div
      v-if="traeger?.team"
      class="kita-intro"
    >
      <h2>Zuständiges RBS-Team</h2>
      <div>
        <muc-icon icon="account" /><b>{{ textOrFallback(traeger?.team?.name) }}</b>
      </div>
      <a :href="`mailto:${traeger?.team?.postfach}`">
        <muc-icon icon="mail" /><b>{{ textOrFallback(traeger?.team?.postfach) }}</b>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  MucButton,
  MucIcon,
  MucIntro,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import TraegerDTO from "@/types/TraegerDTO";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import { textOrFallback } from "./util/format";

const traeger = ref<TraegerDTO>();

const props = defineProps({
  detailsUrl: {
    type: String,
    default: null,
  },
  token: {
    type: String,
    default: null,
  },
});

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();
const emit = defineEmits(["loadingError", "unknownTraeger"]);

function loadTraeger() {
  console.debug("Loading Träger...");

  if (!props.token) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

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

          if (
            resp.status == 422 &&
            errBody.includes("Unternehmenskonto-ID wurde nicht gefunden")
          ) {
            emit("unknownTraeger");
          } else {
            emit("loadingError");
            throw Error(errBody);
          }
        });
      }
    })
    .catch((error) => {
      dataLoadingError.value = true;
      emit("loadingError");
      console.debug(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

const traegerLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.detailsUrl);
});

const hasLink = computed(() => {
  return props.detailsUrl && traeger.value;
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

.m-intro {
  --intro-bg-color: #E1D7FF !important;
  background: var(--intro-bg-color);
  margin-bottom: 0 !important;
}

.kita-intro {
  --intro-bg-color: #E1D7FF !important;
  background: var(--intro-bg-color);
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  padding-top: 3rem;
  padding-bottom: 3rem;
  margin-top: 2rem;
}
</style>
