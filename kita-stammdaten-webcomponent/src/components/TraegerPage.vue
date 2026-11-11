<template>
  <traegerportal-page :title="traeger?.name" :tagline="tagline" :include-footer="includeFooter">
    <template #content>
      <slot name="content" :traeger="traeger" />
    </template>
    <template #intro-content>
      <slot name="intro-content" :traeger="traeger" />
    </template>
    <template #footer>
      <h2>Zuständiges RBS-Team</h2>
      <div>
        <muc-icon icon="account" /><b>{{
          textOrFallback(traeger?.team?.name)
        }}</b>
      </div>
      <a :href="`mailto:${traeger?.team?.postfach}`">
        <muc-icon icon="mail" /><b>{{
          textOrFallback(traeger?.team?.postfach)
        }}</b>
      </a>
    </template>
  </traegerportal-page>  
</template>

<script setup lang="ts" generic="T extends object">
import StammdatenService from '@/api/einrichtungsverwaltung/StammdatenService';
import type TraegerDTO from '@/types/TraegerDTO';
import { textOrFallback } from '@/util/format';
import { ref, watch } from 'vue';
import { MucIcon } from '@muenchen/muc-patternlab-vue';
import TraegerportalPage from './TraegerportalPage.vue';

const traeger = ref<TraegerDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();
const emit = defineEmits(["loadingError", "unknownTraeger"]);

const props = defineProps({
  token: {
    type: String,
    default: null,
  },
  tagline: {
    type: String,
    default: "Mein Träger",
  },
  includeFooter: {
    type: Boolean,
    default: true
  }
});

function loadTraeger() {
  console.debug("Loading Träger...");

  if (!props.token) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

  loading.value = true;
  const service = new StammdatenService();
  service
    .getTraeger(props.token)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: TraegerDTO) => {
          traeger.value = response;
          dataLoadingError.value = false;
        });
      } else {
        resp.text().then((errBody) => {
          dataLoadingError.value = true;

          if (
            resp.status == 422 &&
            errBody.includes("Unternehmenskonto-ID wurde nicht gefunden")
          ) {
            emit("unknownTraeger");
          } else {
            emit("loadingError");
            throw Error(errBody);
          }
        });
      }
    })
    .catch((error) => {
      dataLoadingError.value = true;
      emit("loadingError");
      console.debug(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

watch(
  () => props.token,
  (newToken, oldToken) => {
    console.debug("newToken: " + newToken);
    if (newToken !== oldToken) {
      loadTraeger();
    }
  },
  { immediate: true }
);
</script>

<style scoped>
</style>
```
