<template>
  <div class="m-descriptionlist__group">
    <dt class="m-descriptionlist__term">{{ term }}</dt>
    <dd class="m-descriptionlist__description">{{ description }}</dd>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  term?: string;
  description?: string | number;
}>();
</script>

<style scoped>
.m-descriptionlist__description:empty::before {
  content: "Keine Daten";
}
</style>
