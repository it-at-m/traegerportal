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
      id="vorgaenge-card-details"
      title="Vorgänge zum Träger"
      :disabled="false"
    >
      <template #content>
        <vorgaenge-table
          v-if="vorgaenge.length > 0"
          :items="vorgaenge"
          :columns="columns"
          row-key="aktenzeichen"
          :page-size="pageSize"
          :page-size-options="pageSizeOptions"
          caption="Vorgänge zum Träger"
        >
          <template #cell-einrichtungsadresse="{ row }">
            <div>{{ row.einrichtung?.name }}</div>
            <div>{{ formatAdresse(row.einrichtung?.adresse) }}</div>
          </template>

          <template #cell-vorgangsart="{ row }">
            {{ formatVorgangsart(row) }}
          </template>

          <template #cell-status="{ row }">
            {{ formatVorgangsstatus(row.status) }}
          </template>

          <template #cell-letzteAenderung="{ row }">
            {{ formatDate(row.letzteAenderung) }}
          </template>

          <template #cell-aktionen="{ row }">
            <a
              class="details-link"
              :href="getDetailsUrl(row.aktenzeichen)"
            >
              <span hidden>Vorgangsdetails</span>
              <muc-icon
                icon="arrow-right"
                aria-hidden="true"
              />
            </a>
          </template>
        </vorgaenge-table>
        <muc-callout
          v-else
          type="info"
          class="no-vorgaenge-callout"
        >
          <template #content>
            <p>Es liegen aktuell keine Vorgänge vor.</p>
          </template>
        </muc-callout>
      </template>
    </muc-card>
  </div>
</template>

<script setup lang="ts">
import type VorgaengeDTO from "@/types/VorgaengeDTO";

import {
  MucCallout,
  MucCard,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import type { TableColumn } from "./components/VorgaengeTable.vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import VorgaengeTable from "./components/VorgaengeTable.vue";
import {
  formatAdresse,
  formatDate,
  formatVorgangsart,
  formatVorgangsstatus,
} from "./util/format";

const props = withDefaults(
  defineProps<{
    detailsUrl?: string | null;
    pageSize?: number;
    pageSizeOptions?: number[];
    token?: string | null;
    authLoading?: boolean;
  }>(),
  {
    detailsUrl: null,
    pageSize: 5,
    pageSizeOptions: () => [10, 20, 30, 50],
    token: null,
    authLoading: false,
  }
);

const loading = ref(false);
const dataLoadingError = ref(false);

function getDetailsUrl(aktenzeichen: string): string {
  return `${props.detailsUrl}?id=${aktenzeichen}&lg-idphint=ELSTER_NEZO`;
}

const vorgaenge = ref<VorgaengeDTO[]>([]);

const emit = defineEmits(["loadingError"]);

const columns = computed<TableColumn<VorgaengeDTO>[]>(() => [
  {
    key: "aktenzeichen",
    label: "Aktenzeichen",
  },
  {
    key: "einrichtungsadresse",
    label: "Einrichtungsadresse",
    value: (vorgang) => vorgang.einrichtung?.name ?? "",
  },
  {
    key: "vorgangsart",
    label: "Vorgangsart",
  },
  {
    key: "kibigWebId",
    label: "KiBiG.web-Nummer",
    value: (vorgang) => vorgang.einrichtung?.merkmale?.kibigWebId ?? "",
  },
  {
    key: "status",
    label: "Status",
    value: (vorgang) => vorgang.status?.status ?? "",
  },
  {
    key: "letzteAenderung",
    label: "Aktualisiert",
  },
  {
    key: "aktionen",
    label: "",
    sortable: false,
  },
]);

function loadVorgaenge() {
  console.debug("Loading Vorgänge...");
  if (!props.token) return "";

  loading.value = true;
  const service = new StammdatenService();
  service
    .getVorgaenge(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response) => {
          vorgaenge.value = response.content as VorgaengeDTO[];
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
      loadVorgaenge();
    }
  },
  { immediate: true }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.content-width {
  width: 100% !important;
}

.details-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.details-link:focus-visible {
  outline: 2px solid currentColor;
  outline-offset: 3px;
}

.no-vorgaenge-callout {
  margin-top: 1rem;
}
</style>
