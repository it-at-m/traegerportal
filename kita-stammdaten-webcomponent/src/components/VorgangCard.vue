<template>
  <muc-card
    :id="`vorgaenge-card-${vorgang.aktenzeichen}`"
    :key="vorgang.aktenzeichen"
    :title="formatVorgangsart(vorgang)"
    :tagline="vorgang.getBetreff()"
    :href="fullVorgangLink"
    :disabled="false"
  >
    <template #content>
      <div class="statuszeile">
        <b class="chip vorgang-status">
          {{ formatVorgangsstatus(vorgang.status) }}
        </b>

        <b
          v-if="vorgang.changedSinceLastExternalRead"
          class="chip vorgang-notification"
        >
          ungelesen
        </b>
      </div>
      <muc-label-value-display
        label="Aktenzeichen"
        :value="vorgang.aktenzeichen"
      />
      <muc-label-value-display
        v-if="vorgang.vorgangsart == 'EINRICHTUNGSDATEN_AENDERN'"
        label="Einrichtungsadresse"
        :value="formatStrasse(vorgang.einrichtung?.adresse)"
      />
      <muc-label-value-display
        label="Letzte Änderung"
        :value="formatDate(vorgang.letzteAenderung)"
      />
    </template>
  </muc-card>
</template>

<script setup lang="ts" generic="T extends object">
import type VorgaengeDTO from "@/types/VorgaengeDTO";

import { MucCard } from "@muenchen/muc-patternlab-vue";
import { computed } from "vue";

import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import {
  formatDate,
  formatStrasse,
  formatVorgangsart,
  formatVorgangsstatus,
} from "../util/format";
import MucLabelValueDisplay from "./MucLabelValueDisplay.vue";

const props = defineProps<{
  vorgang: VorgaengeDTO;
  vorgangLink: string;
}>();

const fullVorgangLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.vorgangLink, {
    vorgangId: props.vorgang.aktenzeichen,
  });
});
</script>

<style scoped>
.chip {
  display: inline-block;
  padding: 0.25em 0.75em;
  background-color: #d3d3d3;
  border-radius: 12px;
  white-space: nowrap;
  user-select: none;
}

.vorgang-status {
  background-color: #d3d3d3;
}

.vorgang-notification {
  background-color: #edae7a;
}
.statuszeile {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.5rem;
}
</style>
```
