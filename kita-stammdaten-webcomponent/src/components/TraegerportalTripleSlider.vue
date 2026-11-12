<template>
  <muc-slider>
    <muc-slider-item v-for="(page, index) in tripleElements" :key="index">
      <muc-card-container>
        <muc-card
            v-for="(element, elementIndex) in page"
            :key="elementIndex"
            title="Lorem Ipsum"
            tagline="Dolor"
            href="https://www.muenchen.de"
        >
          <template #content>
            {{ element }}
          </template>
        </muc-card>
      </muc-card-container>
    </muc-slider-item>
  </muc-slider>
</template>

<script setup lang="ts" generic="T extends object">

import {
  MucCard,
  MucCardContainer,
  MucSlider,
  MucSliderItem,
} from "@muenchen/muc-patternlab-vue";
import { computed } from "vue";


const props = defineProps<{
  values: string[]
}>();

const tripleElements = computed(() => {
  const chunks: string[][] = [];

  for (let i = 0; i < props.values.length; i += 3) {
    chunks.push(props.values.slice(i, i + 3));
  }

  return chunks;
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
