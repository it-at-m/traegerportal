<template>
  <div class="label-value-attribute">
    <span class="label-value-label">{{ label }}:</span>
    <span class="label-value-value">{{ value }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  value?: string;
}>();
</script>

<style scoped>
.label-value-attribute {
  padding-right: 1rem;
  display: flex;
  gap: 0.5rem;
  font-size: 1rem;
}
.label-value-label {
  font-weight: 700;
  word-break: keep-all;
}
.label-value-value:empty::before {
  content: "Keine Daten";
}
</style>
