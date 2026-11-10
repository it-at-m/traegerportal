<template>
  <muc-spinner
    v-if="loading"
    size="200px"
    text="Lade Einrichtungen ..."
  />
  <div v-else-if="!dataLoadingError">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <div v-if="einrichtungenVorhanden">
      <h2 class="einrichtungen-ueberschrift"><muc-icon icon="home" />Meine Einrichtungen ({{ einrichtungen?.length }})</h2>
      <muc-card-container id="einrichtung-container">
        <muc-card
          v-for="einrichtung in currentPage"
          :id="'einrichtung-item-' + einrichtung.id"
          :key="einrichtung.id"
          :title="einrichtung.name"
          :href="getDetailsUrl(einrichtung.id)"
          :tagline="formatEinrichtungsart(einrichtung.merkmale?.einrichtungsartLhm)"
        >
          <template #content>
            <div>
              <b :class="`einrichtung-status ${einrichtung.aktuellGueltigerStatus?.status}`">
                {{
                  formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus)
                }}
              </b>
            </div>
            <div>
              <span class="einrichtung-attribute"
                ><b>Adresse:</b> {{ formatStrasse(einrichtung.adresse) }}</span
              >
            </div>
            <div>
              <span class="einrichtung-attribute"
                ><b>KIGIB.web-Nummer:</b>
                {{ einrichtung.merkmale.kibigWebId }}</span
              >
            </div>
          </template>
        </muc-card>
      </muc-card-container>
      <muc-button v-if="morePages" class="action-button" @click="displayMore">
        Mehr anzeigen
        <muc-icon icon="arrow-down" />
      </muc-button>
    </div>
    <muc-callout
      v-else
      type="info"
      class="no-einrichtungen-callout"
    >
      <template #content>
        <p>Keine Einrichtungen hinterlegt</p>
      </template>
    </muc-callout>
  </div>
</template>

<script setup lang="ts">
import {
  MucButton,
  MucCallout,
  MucCard,
  MucCardContainer,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import {
  formatStrasse,
  formatEinrichtungsart,
  formatEinrichtungsstatus,
} from "./util/format";

const props = defineProps({
  detailsUrl: {
    type: String,
    default: null,
  },
  pageSize: {
    type: Number,
    default: 5,
  },
  token: {
    type: String,
    default: null,
  },
});

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function getDetailsUrl(einrichtungId: string): string | undefined {
  return getUrlWithOriginAsBaseAndDefaultParams(props.detailsUrl, {
    einrichtungId: einrichtungId,
  });
}

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
          einrichtungen.value.sort((a, b) =>
            a.name.localeCompare(b.name)
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
      console.debug(error);
      dataLoadingError.value = true;
      emit("loadingError");
    })
    .finally(() => {
      loading.value = false;
    });
}

const pageNumber = ref<number>(0);

const currentPage = computed(() => {
  return einrichtungen.value instanceof Array
    ? einrichtungen.value.slice(
        0,
        (pageNumber.value + 1) * props.pageSize
      )
    : [];
});

function displayMore() {
  pageNumber.value++;
}
const morePages = computed(() => {
  if (einrichtungen.value) {
    return props.pageSize * (pageNumber.value + 1) < einrichtungen.value.length;
  } else {
    return false;
  }
});

const einrichtungenVorhanden = computed(() => {
  if (einrichtungen.value) {
    return einrichtungen.value.length > 0;
  } else {
    return false;
  }
});

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

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.einrichtungen-ueberschrift {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  margin-top: 1rem;
}

.action-button {
  margin-top: 2rem;
}

.einrichtung-attribute {
  padding-right: 1rem;
}

.einrichtung-status {
  display: inline-block;
  padding: 0.25em 0.75em;
  background-color: #d3d3d3;
  border-radius: 12px;
  white-space: nowrap;
  user-select: none;
}

.einrichtung-status.VORPLANUNG {
  background-color: #f9e79f; /* light yellow */
}

.einrichtung-status.VOR_BETRIEB {
  background-color: #aed6f1; /* light blue */
}

.einrichtung-status.IN_BETRIEB {
  background-color: #a9dfbf; /* light green */
}

.content-width {
  width: 100% !important;
}

.no-einrichtungen-callout {
  padding-left: 0;
  margin-top: 2.5rem !important;
}
</style>
