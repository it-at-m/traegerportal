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

    <h2 class="vorgaenge-ueberschrift">
      <span>
        <muc-icon icon="file" /> Meine Vorgänge ({{ vorgangCount }}/{{vorgaenge?.length}})
      </span>
      <a :href="vorgangLink">
        <muc-button variant="ghost"
          >Alle Vorgänge anzeigen<muc-icon icon="arrow-right"
        /></muc-button>
      </a>
    </h2>

    <muc-card-container>
      <vorgang-card
        v-for="vorgang in vorgaenge?.slice(0, 3)"
        :key="vorgang.aktenzeichen"
        :vorgang="vorgang"
        :vorgang-link="detailsUrl"
      />
    </muc-card-container>
  </div>
</template>

<script setup lang="ts">
import {
  MucButton,
  MucCardContainer,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import VorgangCard from "./components/VorgangCard.vue";
import VorgaengeDTO from "./types/VorgaengeDTO";

const vorgangCount = ref<number>(0);
const vorgaenge = ref<VorgaengeDTO[]>();

const props = defineProps({
  listUrl: {
    type: String,
    default: null,
  },
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

function loadData() {
  console.debug("Loading Vorgang count...");

  if (!props.token) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

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

  service
    .getVorgaenge(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response) => {
          vorgaenge.value = response.content as VorgaengeDTO[];
          // Assigne den Typen VorgaengeDTO, damit Methoden verwendet werden können
          vorgaenge.value.forEach((item: VorgaengeDTO) => {
            Object.setPrototypeOf(item, VorgaengeDTO.prototype);
          });
          // Sortiere neueste zuerst
          // wenn ungelesen werden 20 Jahre abgezogen
          vorgaenge.value.sort(
            (a, b) =>
              new Date(b.letzteAenderung).getTime() -
              (b.changedSinceLastExternalRead ? 0 : 630720000000) -
              (new Date(a.letzteAenderung).getTime() -
                (a.changedSinceLastExternalRead ? 0 : 630720000000))
          );
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

const vorgangLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.listUrl);
});

watch(
  () => props.token,
  (newToken, oldToken) => {
    if (newToken !== oldToken) {
      loadData();
    }
  },
  { immediate: true }
);
</script>

<style scoped>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.vorgaenge-ueberschrift {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  margin-top: 1rem;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-content {
  padding: 1rem !important;
}

.card-action-button {
  position: absolute;
  bottom: 1rem;
  right: 1.5rem;
}

#vorgaenge-card {
  position: relative;
}
</style>
