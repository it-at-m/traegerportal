<template>
  <div>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="mucIconsSprite" />
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="customIconsSprite" />
    <muc-accordion
      id="einrichtung-accordion"
      multiple
    >
      <muc-accordion-item
        v-for="einrichtung in currentPage"
        :id="'einrichtung-accordion-item-' + einrichtung.id"
        :key="einrichtung.id"
        :header="einrichtung.title()"
      >
        <template #subtitle>
          <a :href="bearbeitenFormularUrl(einrichtung.id)"
            ><muc-button variant="ghost"
              >Einrichtungsdetails<muc-icon icon="arrow-right" /></muc-button
          ></a>
        </template>
        <template #content>
          <div>
            <span class="einrichtung-attribute"
              ><b>Kibigweb-ID:</b> {{ einrichtung.kibigwebid }}</span
            >
            <span class="einrichtung-attribute"
              ><b>Status:</b> {{ einrichtung.status }}</span
            >
          </div>
          <div>
            <span class="einrichtung-attribute"
              ><b>Adresse:</b> {{ einrichtung.adresse() }}</span
            >
          </div>
        </template>
      </muc-accordion-item>
    </muc-accordion>
    <simple-pagination
      v-if="!!einrichtungen"
      v-model="pageNumber"
      :total-items="einrichtungen.length"
      style="padding-left: 24px"
      :cookie-name="traegerUkId"
    />
  </div>
</template>

<script setup lang="ts">
import {
  MucAccordion,
  MucAccordionItem,
  MucButton,
  MucIcon,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, onMounted, ref } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService";
import SimplePagination from "@/components/SimplePagination.vue";
import EinrichtungDTO from "@/types/EinrichtungDTO";

const traegerUkId = "dummy-Value-for-now";

const props = defineProps<{
  stammdatenUrl: string;
}>();

function bearbeitenFormularUrl(einrichtungId: string): string {
  return `${props.stammdatenUrl}/traegerAnzeigen/${einrichtungId}`;
}

const einrichtungen = ref<EinrichtungDTO[]>();

function loadEinrichtungen() {
  const service = new StammdatenService();
  service
    .searchEinrichtungen(traegerUkId)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: EinrichtungDTO[]) => {
          einrichtungen.value = response;
        });
      } else {
        resp.text().then((errBody) => {
          throw Error(errBody);
        });
      }
    })
    .catch((error) => {
      console.debug(error);
    });
}

const pageNumber = ref<number>(0);
const pageSize = 3;
const currentPage = computed(() => {
  return einrichtungen.value instanceof Array
    ? einrichtungen.value.slice(
        pageNumber.value * pageSize,
        (pageNumber.value + 1) * pageSize
      )
    : [];
});

onMounted(() => {
  loadEinrichtungen();
});
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.m-accordion__section {
  border: solid 1px var(--color-neutrals-blue);
  border-bottom: solid 5px var(--color-brand-main-blue);
  padding-left: 1rem;
  padding-right: 1rem;
}

.einrichtung-attribute {
  padding-right: 1rem;
}

.content-width {
  width: 100% !important;
}
</style>
