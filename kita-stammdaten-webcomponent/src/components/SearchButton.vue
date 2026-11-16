<!-- VorgangSearch.vue -->
<template>
  <muc-input
    id="vorgang-search-input"
    v-model="searchTextModel"
    class="search-button"
    name="Suche"
    :placeholder="placeholder ?? 'Suche nach Vorgängen'"
    type="search"
    suffix-icon="search"
  />
</template>

<script setup lang="ts">
import { MucInput } from "@muenchen/muc-patternlab-vue";
import { computed, watch } from "vue";

const props = defineProps<{
  searchText?: string;
  placeholder?: string;
}>();

const emit = defineEmits<(event: "update:searchText", value: string) => void>();

const searchTextModel = computed({
  get: () => props.searchText ?? "",
  set: (value: string) => emit("update:searchText", value),
});

watch(
  () => props.searchText,
  (newValue) => {
    const url = new URL(window.location.href);

    if (newValue) {
      url.searchParams.set("q", newValue);
    } else {
      url.searchParams.delete("q");
    }

    window.history.replaceState({}, "", url);
  },
  { immediate: true }
);
</script>

<style scoped>
.search-button {
  margin-bottom: 1rem;
  margin-top: 1rem;
}
</style>
