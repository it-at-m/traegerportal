<template>
  <div class="intro-wrapper">
    <muc-intro
      :title="title"
      :tagline="tagline"
      :divider="false"
    >
      <template #default>
        <slot name="intro-content" />
      </template>
    </muc-intro>
  </div>
  <div class="page-body">
    <div class="body-content container">
      <slot name="content" />
    </div>
  </div>
  <div
    v-if="includeFooter"
    class="kita-banner"
  >
    <div class="kita-banner-content container">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends object">
import { MucIntro } from "@muenchen/muc-patternlab-vue";

defineProps({
  title: {
    type: String,
    default: null,
  },
  tagline: {
    type: String,
    default: null,
  },
  includeFooter: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
@import "../../public/style.css";
</style>
```
