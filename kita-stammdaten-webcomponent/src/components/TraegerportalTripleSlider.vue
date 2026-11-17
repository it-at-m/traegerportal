<template>
  <muc-slider>
    <muc-slider-item
      v-for="(page, index) in tripleElements"
      :key="index"
    >
      <muc-card-container>
        <slot
          v-for="(element, elementIndex) in page"
          :key="elementIndex"
          :element="element"
        />
      </muc-card-container>
    </muc-slider-item>
  </muc-slider>
</template>

<script setup lang="ts" generic="T extends object">
import {
  MucCardContainer,
  MucSlider,
  MucSliderItem,
} from "@muenchen/muc-patternlab-vue";
import { computed } from "vue";

const props = defineProps<{
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  values: any[];
  title?: string;
  tagline?: string;
  linkId?: string;
  linkIdName?: string;
  linkBase?: string;
}>();

const tripleElements = computed(() => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const chunks: any[][] = [];

  if (props.values) {
    for (let i = 0; i < props.values.length; i += 3) {
      chunks.push(props.values.slice(i, i + 3));
    }
  }

  return chunks;
});
</script>

<style scoped>
.m-component {
  padding: 0;
}
</style>
```
