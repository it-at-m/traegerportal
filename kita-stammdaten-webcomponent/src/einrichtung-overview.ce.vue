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
      <h2 class="traegerportal-ueberschrift">
        <muc-icon icon="home" />Meine Einrichtungen ({{ einrichtungen?.length }}
        gesamt)
      </h2>
      <muc-card-container id="einrichtung-container">
        <einrichtung-card
          v-for="einrichtung in currentPage"
          :key="einrichtung.id"
          :einrichtung="einrichtung"
          :einrichtung-link="detailsUrl"
        />
      </muc-card-container>
      <muc-button
        v-if="morePages"
        class="action-button"
        @click="displayMore"
      >
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
  MucCardContainer,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import EinrichtungCard from "./components/EinrichtungCard.vue";
import { normalizeForSearch } from "./util/format";

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
  searchText: {
    type: String,
    default: null,
  },
});

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

const pageNumber = ref<number>(0);

const filteredResults = computed(() => {
  return einrichtungen.value instanceof Array
    ? einrichtungen.value.filter((v) => {
        return normalizeForSearch(v).includes(props.searchText?.toLowerCase());
      })
    : [];
});

const currentPage = computed(() => {
  return filteredResults.value.slice(
    0,
    (pageNumber.value + 1) * props.pageSize
  );
});

function displayMore() {
  pageNumber.value++;
}

const morePages = computed(() => {
  if (filteredResults.value) {
    return (
      props.pageSize * (pageNumber.value + 1) < filteredResults.value.length
    );
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

watch(
  () => props.searchText,
  () => {
    pageNumber.value = 0;
  }
);
</script>

<style>
@import "../public/style.css";

.no-einrichtungen-callout {
  padding-left: 0;
  margin-top: 2.5rem !important;
}
</style>
