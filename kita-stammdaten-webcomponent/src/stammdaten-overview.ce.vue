<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-callout
      v-if="unknownTraeger"
      type="info"
    >
      <template #content>
        <p>
          Ihr Träger ist bei der Landeshauptstadt München noch nicht gemeldet.
          Bitte registrieren Sie sich mit diesem Unternehmenskonto.
        </p>
      </template>
    </muc-callout>
    <muc-callout
      v-if="loadingErrorAll"
      type="error"
    >
      <template #content>
        <p>
          Die Schnittstelle ist nicht erreichbar. Bitte versuchen Sie es zu
          einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div v-else>
      <traeger-page
        :token="token"
        @unknown-traeger="unknownTraeger = true"
        @loading-error="loadingErrorTraeger = true"
      >
        <template #intro-content>
          <muc-callout v-if="loadingErrorTraeger">
            <template #content>
              <p>
                Die Trägerdaten können derzeit nicht geladen werden. Bitte
                versuchen Sie es zu einem späteren Zeitpunkt erneut.
              </p>
            </template>
          </muc-callout>
          <a :href="traegerDetailsUrl">
            <muc-button>
              Trägerdaten ansehen
              <muc-icon icon="arrow-right" />
            </muc-button>
          </a>
        </template>
        <template #content>
          <search-button v-model:search-text="searchText" />
          <muc-callout
            v-if="loadingErrorVorgaenge"
            type="error"
            style="width: 50%"
          >
            <template #content>
              <p>
                Die Vorgänge können derzeit nicht geladen werden. Bitte
                versuchen Sie es zu einem späteren Zeitpunkt erneut.
              </p>
            </template>
          </muc-callout>
          <vorgang-overview-vue-component
            v-else
            class="full-width"
            :list-url="vorgangListUrl"
            :details-url="vorgangDetailsUrl"
            :token="token"
            :search-text="searchText"
            @loading-error="loadingErrorVorgaenge = true"
          />
          <muc-callout
            v-if="loadingErrorEinrichtungen"
            type="error"
            style="margin-top: 2.5rem"
          >
            <template #content>
              <p>
                Die Einrichtungen können derzeit nicht geladen werden. Bitte
                versuchen Sie es zu einem späteren Zeitpunkt erneut.
              </p>
            </template>
          </muc-callout>
          <einrichtung-overview-vue-component
            v-else
            :details-url="einrichtungDetailsUrl"
            :page-size="pageSize"
            :token="token"
            class="full-width"
            :search-text="searchText"
            @loading-error="loadingErrorEinrichtungen = true"
          />
        </template>
      </traeger-page>
    </div>
  </div>
  <div v-else>
    <muc-callout type="info">
      <template #content>
        <p>Um diese Inhalte anzuzeigen, müssen Sie sich anmelden.</p>
      </template>
    </muc-callout>
  </div>
</template>

<script setup lang="ts">
import { MucButton, MucCallout, MucIcon } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref } from "vue";

import EinrichtungOverviewVueComponent from "@/einrichtung-overview.ce.vue";
import SearchButton from "./components/SearchButton.vue";
import TraegerPage from "./components/TraegerPage.vue";
import { AuthService } from "./util/AuthService.ts";
import vorgangOverviewVueComponent from "./vorgang-overview.ce.vue";

const { token, loggedIn } = new AuthService();

const unknownTraeger = ref<boolean>(false);
const loadingErrorTraeger = ref<boolean>(false);
const loadingErrorEinrichtungen = ref<boolean>(false);
const loadingErrorVorgaenge = ref<boolean>(false);
const searchText = ref<string>("");

const loadingErrorAll = computed(() => {
  return (
    loadingErrorTraeger.value &&
    loadingErrorEinrichtungen.value &&
    loadingErrorVorgaenge.value
  );
});

defineProps({
  traegerDetailsUrl: {
    type: String,
    default: null,
  },
  einrichtungDetailsUrl: {
    type: String,
    default: null,
  },
  vorgangListUrl: {
    type: String,
    default: null,
  },
  vorgangDetailsUrl: {
    type: String,
    default: null,
  },
  pageSize: {
    type: Number,
    default: 6,
  },
});
</script>

<style>
@import "../public/style.css";
</style>
