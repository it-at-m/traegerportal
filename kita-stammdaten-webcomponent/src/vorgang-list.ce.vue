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
      <traeger-page tagline="Meine Vorgänge" :token="token" :include-footer="false">
        <template #content>
          <h2 class="vorgaenge-ueberschrift"><muc-icon icon="file" />Meine Vorgänge ({{ vorgaenge?.length }})</h2>
          <muc-input
            id="vorgang-search-input"
            v-model="searchText"
            class="search-button"
            name="Suche"
            placeholder="Suche nach Vorgängen"
            type="search"
            suffix-icon="search">
          </muc-input>
          <muc-card-container>
            <vorgang-card
              v-for="vorgang in currentPage"
              :key="vorgang.aktenzeichen"
              :vorgang="vorgang"
              :vorgang-link="detailsUrl"
            />
          </muc-card-container>
          <muc-button v-if="morePages" class="action-button" @click="displayMore">
            Mehr anzeigen
            <muc-icon icon="arrow-down" />
          </muc-button>
        </template>
      </traeger-page>
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
import VorgaengeDTO from "@/types/VorgaengeDTO";
import TraegerPage from "./components/TraegerPage.vue";

import {
  MucButton,
  MucCallout,
  MucCardContainer,
  MucIcon,
  MucInput,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import {  computed, ref, watch } from "vue";

import VorgangCard from "./components/VorgangCard.vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import { AuthService } from "./util/AuthService.ts";

const props = defineProps({
  detailsUrl: {
    type: String,
    default: null,
  },
  pageSize: {
    type: Number,
    default: 9,
  },
});

const { token, loggedIn } = new AuthService();

const vorgaenge = ref<VorgaengeDTO[]>([]);

const loading = ref(false);
const dataLoadingError = ref(false);

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
          // Assigne den Typen VorgaengeDTO, damit Methoden verwendet werden können
          vorgaenge.value.forEach((item: VorgaengeDTO) => {
            Object.setPrototypeOf(item, VorgaengeDTO.prototype);
          });
          // Sortiere neueste zuerst
          vorgaenge.value.sort(
            (a, b) => new Date(b.letzteAenderung).getTime() - new Date(a.letzteAenderung).getTime()
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

watch(
  token,
  (newToken) => {
    if (newToken) {
      loadVorgaenge();
    }
  },
  { immediate: true }
);

const filteredVorgaenge = computed(() => {
  return vorgaenge.value.filter(v => {
    // TODO: robustere Suche einbauen
    return Object.values(v).some(value => JSON.stringify(value).toLowerCase().includes(searchText.value?.toLowerCase() ?? ''));
  });
});

const pageNumber = ref<number>(0);

const currentPage = computed(() => {
  return filteredVorgaenge.value instanceof Array
    ? filteredVorgaenge.value.slice(
        0,
        (pageNumber.value + 1) * props.pageSize
      )
    : [];
});

function displayMore() {
  pageNumber.value++;
}

const morePages = computed(() => {
  if (filteredVorgaenge.value) {
    return props.pageSize * (pageNumber.value + 1) < filteredVorgaenge.value.length;
  } else {
    return false;
  }
});

const searchText = ref<string>();
watch(
  searchText,
  (newValue) => {
    const _url = new URL(window.location.href);
    if(newValue) {
      _url.searchParams.set("q", newValue);
    } else {
      _url.searchParams.delete("q");
    }
    window.history.replaceState({}, '', _url);
    pageNumber.value = 0;
  },
  { immediate: true }
);
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.vorgaenge-ueberschrift {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  margin-top: 1rem;
}

.action-button {
  margin-top: 2rem;
}

.no-vorgaenge-callout {
  margin-top: 1rem;
}

.m-intro-summary-text__text p, .m-intro, .m-intro-summary-text__title, .m-intro-vertical__tagline {
  margin-bottom: 0;
}

.search-button {
  margin-bottom: 2rem;
}
</style>
