<template>
  <div class="pagination">
    <muc-button
      :disabled="isFirstPage"
      icon="arrow-left"
      variant="icon"
      @click="goToPage(currentPage - 1)"
    />
    <muc-button
      v-for="page in totalPages"
      :key="page"
      :variant="currentPage === page - 1 ? 'primary' : 'secondary'"
      @click="goToPage(page - 1)"
    >
      {{ page }}
    </muc-button>
    <muc-button
      :disabled="isLastPage"
      icon="arrow-right"
      variant="icon"
      @click="goToPage(currentPage + 1)"
    />
  </div>
</template>

<script lang="ts" setup>
import { MucButton } from "@muenchen/muc-patternlab-vue";
import { computed, onMounted } from "vue";

// Zugriff auf Props
const props = defineProps({
  totalItems: {
    type: Number,
    required: true,
  },
  itemsPerPage: {
    type: Number,
    default: 3,
  },
  modelValue: {
    type: Number,
    default: 0,
  },
  cookieName: {
    type: String,
    default: "kita-stammdaten-webcomponent-pagination",
  },
  cookieExpirationSeconds: {
    type: Number,
    default: 600
  }
});

// Emit Funktion
const emit = defineEmits(["update:modelValue"]);

// Berechnete Gesamtseitenzahl
const totalPages = computed(() => {
  return Math.ceil(props.totalItems / props.itemsPerPage);
});

const isFirstPage = computed(() => {
  return currentPage.value == 0;
});
const isLastPage = computed(() => {
  return currentPage.value == totalPages.value - 1;
});

// Computed für die aktuelle Seite mit Setter
const currentPage = computed({
  get() {
    return props.modelValue ?? 0;
  },
  set(value: number) {
    emit("update:modelValue", value);
  },
});

function setCurrentPageCookie(page: number) {
  // default: 1 hour
  const expires = new Date(Date.now() + props.cookieExpirationSeconds * 1000).toUTCString();
  document.cookie = `${props.cookieName}=${encodeURIComponent(page)}; expires=${expires}; path=/`;
}

function getCurrentPageFromCookie(): number | null {
  const nameLenPlus = props.cookieName.length + 1;
  return (
    document.cookie
      .split(";")
      .map((c) => c.trim())
      .filter((cookie) => {
        return cookie.substring(0, nameLenPlus) === `${props.cookieName}=`;
      })
      .map((cookie) => {
        return parseInt(decodeURIComponent(cookie.substring(nameLenPlus)));
      })[0] || null
  );
}

// Funktion zum Navigieren zu einer Seite
function goToPage(page: number) {
  if (page < 0) {
    page = 0;
  }
  if (page > totalPages.value - 1) {
    page = totalPages.value - 1;
  }
  currentPage.value = page;

  setCurrentPageCookie(page);
}

onMounted(() => {
  const cookieValue = getCurrentPageFromCookie();

  if (cookieValue != null) {
    goToPage(cookieValue);
  }
});
</script>

<style scoped>
.pagination {
  display: flex;
  gap: 8px;
  align-items: center;
}
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
