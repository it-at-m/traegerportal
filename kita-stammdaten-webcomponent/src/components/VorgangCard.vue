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

        <b v-if="vorgang.changedSinceLastExternalRead" class="chip vorgang-notification">
          ungelesen
        </b>
      </div>
      <div>
        <span> <b>Aktenzeichen:</b> {{ vorgang.aktenzeichen }} </span>
      </div>
      <div v-if="vorgang.vorgangsart=='EINRICHTUNGSDATEN_AENDERN'">
        <span>
          <b>Einrichtungsadresse:</b> {{ formatStrasse(vorgang.einrichtung?.adresse) }}
        </span>
      </div>
      <div>
        <span>
          <b>Letzte Änderung:</b> {{ formatDate(vorgang.letzteAenderung) }}
        </span>
      </div>
    </template>
  </muc-card>
</template>

<script setup lang="ts" generic="T extends object">

import {
  MucCard
} from "@muenchen/muc-patternlab-vue";
import type VorgaengeDTO from "@/types/VorgaengeDTO";
import {
  formatDate,
  formatStrasse,
  formatVorgangsart,
  formatVorgangsstatus,
} from "../util/format";
import { computed } from "vue";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";


const props = defineProps<{
  vorgang: VorgaengeDTO,
  vorgangLink: string
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
