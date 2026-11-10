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
      <div class="statuszeile" :title="vorgang.changedSinceLastExternalRead ? 'Es liegen neue Änderungen vor' : ''">
        <b class="vorgang-status">
          {{ formatVorgangsstatus(vorgang.status) }}
        </b>
        <muc-icon v-if="vorgang.changedSinceLastExternalRead" icon="bell" />
      </div>
      <div>
        <span> <b>Aktenzeichen:</b> {{ vorgang.aktenzeichen }} </span>
      </div>
      <div>
        <span>
          <b>Letzte Änderung:</b> {{ formatDate(vorgang.letzteAenderung) }}
        </span>
      </div>
      <div v-if="vorgang.vorgangsart=='EINRICHTUNGSDATEN_AENDERN'">
        <span>
          <b>Einrichtungsadresse:</b> {{ formatStrasse(vorgang.einrichtung?.adresse) }}
        </span>
      </div>
    </template>
  </muc-card>
</template>

<script setup lang="ts" generic="T extends object">

import {
  MucIcon,
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
.vorgang-status {
  display: inline-block;
  padding: 0.25em 0.75em;
  background-color: #d3d3d3;
  border-radius: 12px;
  white-space: nowrap;
  user-select: none;
}

.statuszeile {
  display: flex;
  align-items: center;
  gap: 1rem;
}
</style>
```
