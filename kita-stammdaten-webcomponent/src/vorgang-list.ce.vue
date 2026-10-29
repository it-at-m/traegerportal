<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-spinner
      v-if="loading"
      size="200px"
      text="Lade Vorgänge ..."
    />
    <muc-callout
      v-else-if="dataLoadingError"
      type="error"
    >
      <template #content>
        <p>Die Vorgänge können derzeit nicht geladen werden.</p>
      </template>
    </muc-callout>
    <div
      v-else
      style="width: 100%"
    >
      <muc-card
        id="vorgaenge-card-details"
        title="Vorgänge zum Träger"
        :disabled="false"
      >
        <template #content>
          <data-table
            :items="vorgaenge"
            :columns="columns"
            row-key="aktenzeichen"
            :page-size="pageSize"
            :page-size-options="pageSizeOptions"
            default-sort-key="letzteAenderung"
            default-sort-direction="desc"
            caption="Vorgänge zum Träger"
            empty-message="Es liegen aktuell keine Vorgänge vor."
          >
            <template #cell-aktenzeichen="{ row }">
              {{ row.aktenzeichen }}
            </template>

            <template #cell-betreff="{ row }">
              {{ formatBetreff(row) }}
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
              <div class="actions">
                <div class="actions-cols">
                  <muc-icon
                    v-if="row.changedSinceLastExternalRead"
                    icon="bell"
                    aria-label="Aktualisierungen"
                  />
                </div>
                <div class="actions-cols">
                  <a
                    class="details-link"
                    :href="getDetailsUrl(row.aktenzeichen)"
                    aria-label="Vorgangsdetails"
                  >
                    <muc-icon
                      icon="arrow-right"
                      aria-hidden="true"
                    />
                  </a>
                </div>
              </div>
            </template>
          </data-table>
        </template>
      </muc-card>
    </div>
  </div>
  <div v-else>
    <muc-callout type="info">
      <template #content>
        <p>Um diese Inhalte anzuzeigen, müssen Sie sich anmelden.</p>
      </template>
    </muc-callout>
  </div>
</template>

<script setup lang="ts">
import type VorgaengeDTO from "@/types/VorgaengeDTO";
import type { PropType } from "vue";

import {
  MucCallout,
  MucCard,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import type { TableColumn } from "./components/data-table/types.ts";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import DataTable from "./components/DataTable.vue";
import { AuthService } from "./util/AuthService.ts";
import {
  formatBetreff,
  formatDate,
  formatVorgangsart,
  formatVorgangsstatus,
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
  pageSizeOptions: {
    type: Array as PropType<number[]>,
    default: () => [10, 20, 30, 50],
  },
});

const columns = computed<TableColumn<VorgaengeDTO>[]>(() => [
  {
    key: "aktenzeichen",
    label: "Aktenzeichen",
  },
  {
    key: "betreff",
    label: "Betreff",
    value: (vorgang) => formatBetreff(vorgang),
  },
  {
    key: "vorgangsart",
    label: "Vorgangsart",
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

const { token, loggedIn } = new AuthService();

const vorgaenge = ref<VorgaengeDTO[]>([]);

const loading = ref(false);
const dataLoadingError = ref(false);

function getDetailsUrl(aktenzeichen: string): string | undefined {
  return getUrlWithOriginAsBaseAndDefaultParams(props.detailsUrl, {
    id: aktenzeichen,
  });
}

function loadVorgaenge() {
  console.debug("Loading Vorgänge...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

  loading.value = true;
  const service = new StammdatenService();
  service
    .getVorgaenge(token.value)
    .then((resp) => {
      if (resp.ok) {
        return resp.json().then((response) => {
          vorgaenge.value = response.content as VorgaengeDTO[];
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

watch(
  token,
  (newToken) => {
    if (newToken) {
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

.actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 8px;
  width: 100%;
  height: 100%;
}

.actions-cols {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 100%;
}

.no-vorgaenge-callout {
  margin-top: 1rem;
}
</style>
