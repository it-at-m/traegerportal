<template>
  <muc-card
    :id="'einrichtung-item-' + einrichtung.id"
    :key="einrichtung.id"
    :title="einrichtung.name"
    :href="fullEinrichtungLink"
    :disabled="false"
    :tagline="formatEinrichtungsart(einrichtung.merkmale?.einrichtungsartLhm)"
  >
    <template #content>
      <div>
        <b
          :class="`einrichtung-status ${einrichtung.aktuellGueltigerStatus?.status}`"
        >
          {{ formatEinrichtungsstatus(einrichtung.aktuellGueltigerStatus) }}
        </b>
      </div>
      <muc-label-value-display
        label="Adresse"
        :value="formatStrasse(einrichtung.adresse)"
      />
      <muc-label-value-display
        label="KIGIB.web-Nummer"
        :value="einrichtung.merkmale.kibigWebId"
      />
    </template>
  </muc-card>
</template>

<script setup lang="ts" generic="T extends object">
import type EinrichtungDTO from "@/types/EinrichtungDTO";

import { MucCard } from "@muenchen/muc-patternlab-vue";
import { computed } from "vue";

import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import {
  formatEinrichtungsart,
  formatEinrichtungsstatus,
  formatStrasse,
} from "../util/format";
import MucLabelValueDisplay from "./MucLabelValueDisplay.vue";

const props = defineProps<{
  einrichtung: EinrichtungDTO;
  einrichtungLink: string;
}>();

const fullEinrichtungLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.einrichtungLink, {
    einrichtungId: props.einrichtung.id,
  });
});
</script>

<style scoped></style>
```
