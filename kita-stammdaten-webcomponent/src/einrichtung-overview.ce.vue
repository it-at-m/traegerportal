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
    <div class="traegerportal-einrichtungen-list">
      <h3>Einrichtungen Ihres Trägers</h3>
      <table style="width: 100%;">
        <tr>
          <th>Name</th>
          <th>Adresse</th>
          <th>Status</th>
          <th>Kibigwebid</th>
        </tr>
        <tr
          v-for="einrichtung in currentPage"
          :id="`einrichtung-row-${einrichtung.id}`"
          :key="einrichtung.id"
          :href="getDetailsUrl(einrichtung.id)"
        >
          <td>{{ einrichtung.name }}</td>
          <td>{{ formatAdresse(einrichtung.adresse) }}</td>
          <td>{{ formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus) }}</td>
          <td>{{ einrichtung.merkmale?.kibigWebId }}</td>
        </tr>
      </table>
      <!--a
        v-for="einrichtung in currentPage"
        :id="`einrichtung-display-${einrichtung.id}`"
        :key="einrichtung.id"
        :title="`KIGIB.web-Nummer: ${einrichtung.merkmale.kibigWebId}`"
        style="width: 100%"
        class="einrichtung-element nolink"
        :href="getDetailsUrl(einrichtung.id)"
      >
        <div class="traegerportal-row">
          <h3>{{ formatEinrichtungTitle(einrichtung) }}</h3>
          <div class="chip" :class="`status-${einrichtung.aktuellGueltigerStatus?.status}`">
            {{ formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus) }}
          </div>
        </div>
        <div class="traegerportal-row">
          <b>KIGIB.web-Nummer:</b><div>{{ einrichtung.merkmale.kibigWebId }}</div>
          <b>Adresse:</b><div>{{ formatAdresse(einrichtung.adresse) }}</div>
        </div>
      </a-->
      <div style="display: flex; justify-content: center;">
        <simple-pagination
          v-if="!!einrichtungen && multiplePages"
          v-model="pageNumber"
          class="traegerportal-paging"
          :total-items="einrichtungen.length"
          :items-per-page="pageSize"
        />
      </div>
    </div>
    <muc-callout
      v-if="!einrichtungenVorhanden"
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
import { MucCallout, MucSpinner, MucCard } from "@muenchen/muc-patternlab-vue";
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

table,
th,
td {
  border: 1px solid var(--color-neutrals-blue);
  border-collapse: collapse;
}

th,
td {
  padding-left: 0.4rem;
  padding-right: 0.4rem;
}

.traegerportal-einrichtungen-list {
  border: solid 1px var(--color-neutrals-blue);
  border-bottom: solid 5px var(--color-brand-main-blue) !important;
  padding: 1rem;
}

.traegerportal-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.chip {
  display: inline-block;
  padding: 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 16px;
  transition: background-color 0.3s ease-in-out;
  user-select: none;
  cursor: default;
}

.status-IN_BETRIEB {
  background-color: #bdf09c;
}

.nolink {
  text-decoration: none;
  color: var(--mde-color-neutral-grey);
}

.einrichtung-element {
  border: solid 1px var(--color-neutrals-blue);
  padding-left: 1rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  padding-right: 1rem;
  cursor: pointer;
  transition: background-color ease-in 0.15s;
  display: block;
}

.einrichtung-element:hover {
  background-color: var(--mde-color-neutral-beau-blue-x-light);
}

.no-einrichtungen-callout {
  padding-left: 0;
  padding-top: 3rem;
}

.traegerportal-paging {
  margin-top: 1rem;
}
</style>
