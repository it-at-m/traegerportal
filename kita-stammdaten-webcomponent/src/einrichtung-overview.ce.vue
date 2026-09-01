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
    <muc-accordion
      v-if="einrichtungenVorhanden"
      id="einrichtung-accordion"
      multiple
    >
      <muc-accordion-item
        v-for="einrichtung in currentPage"
        :id="'einrichtung-accordion-item-' + einrichtung.id"
        :key="einrichtung.id"
        :header="formatEinrichtungTitle(einrichtung)"
      >
        <template #subtitle>
          <a :href="getDetailsUrl(einrichtung.id)"
            ><muc-button variant="ghost"
              >Einrichtungsdetails<muc-icon icon="arrow-right" /></muc-button
          ></a>
        </template>
        <template #content>
          <div>
            <span class="einrichtung-attribute"
              ><b>KIGIB.web-Nummer:</b>
              {{ einrichtung.merkmale.kibigWebId }}</span
            >
          </div>
          <div>
            <span class="einrichtung-attribute"
              ><b>Status:</b>
              {{
                formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus)
              }}</span
            >
          </div>
          <div>
            <span class="einrichtung-attribute"
              ><b>Adresse:</b> {{ formatAdresse(einrichtung.adresse) }}</span
            >
          </div>
        </template>
      </muc-accordion-item>
    </muc-accordion>
    <muc-callout
      v-else
      type="info"
    >
      <template #content>
        <p>Keine Einrichtungen hinterlegt</p>
      </template>
    </muc-callout>
    <simple-pagination
      v-if="!!einrichtungen && multiplePages"
      v-model="pageNumber"
      :total-items="einrichtungen.length"
      :items-per-page="pageSize"
    />
  </div>
</template>

<script setup lang="ts">
import {
  MucAccordion,
  MucAccordionItem,
  MucButton,
  MucCallout,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import SimplePagination from "@/components/SimplePagination.vue";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import {
  formatAdresse,
  formatEinrichtungsstatus,
  formatEinrichtungTitle,
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
  authLoading: {
    type: Boolean,
    default: false,
  },
});

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function getDetailsUrl(einrichtungId: string): string {
  return `${props.detailsUrl}?einrichtungId=${einrichtungId}&lg-idphint=ELSTER_NEZO`;
}

const einrichtungen = ref<EinrichtungDTO[]>();

function loadEinrichtungen() {
  console.debug("Loading einrichtungen...");

  loading.value = true;
  const service = new StammdatenService();
  service
    .searchEinrichtungen(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response) => {
          einrichtungen.value = response.content as EinrichtungDTO[];
          einrichtungen.value.sort((a, b) =>
            formatEinrichtungTitle(a).localeCompare(formatEinrichtungTitle(b))
          );
          dataLoadingError.value = false;
        });
      } else {
        resp.text().then((errBody) => {
          dataLoadingError.value = true;
          throw Error(errBody);
        });
      }
    })
    .catch((error) => {
      console.debug(error);
      dataLoadingError.value = true;
    })
    .finally(() => {
      loading.value = false;
    });
}

const pageNumber = ref<number>(0);
const currentPage = computed(() => {
  return einrichtungen.value instanceof Array
    ? einrichtungen.value.slice(
        pageNumber.value * props.pageSize,
        (pageNumber.value + 1) * props.pageSize
      )
    : [];
});
const multiplePages = computed(() => {
  if (einrichtungen.value) {
    return props.pageSize < einrichtungen.value.length;
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

.m-component-accordion {
  padding-top: 0 !important;
  padding-bottom: 0 !important;
  padding-right: 3rem !important;
}

.m-component-accordion .container {
  padding-left: 0;
  padding-right: 0;
  max-width: 100%;
}

.m-accordion__section {
  border: solid 1px var(--color-neutrals-blue);
  border-bottom: solid 5px var(--color-brand-main-blue) !important;
  padding-left: 1rem;
  padding-right: 1rem;
  margin-bottom: 16px;
}

.m-accordion__section-button {
  padding-top: 16px;
  padding-bottom: 16px;
}

.einrichtung-attribute {
  padding-right: 1rem;
}

.content-width {
  width: 100% !important;
}
</style>
