<template>
  <div class="bordered-area">
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
        <div class="flex-container full-width">
          <muc-callout
            v-if="loadingErrorTraeger"
            type="error"
            style="width: 50%"
          >
            <template #content>
              <p>
                Die Trägerdaten können derzeit nicht geladen werden. Bitte
                versuchen Sie es zu einem späteren Zeitpunkt erneut.
              </p>
            </template>
          </muc-callout>
          <traeger-overview-vue-component
            v-else
            class="flex-area full-width"
            :details-url="traegerDetailsUrl"
            :token="token"
            @unknown-traeger="unknownTraeger = true"
            @loading-error="loadingErrorTraeger = true"
          />
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
          <vorgaenge-overview-vue-component
            v-else
            class="flex-area full-width"
            :details-url="vorgangDetailsUrl"
            :token="token"
            @loading-error="loadingErrorVorgaenge = true"
          />
        </div>
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
          @loading-error="loadingErrorEinrichtungen = true"
        />
      </div>
    </div>
    <div v-else>
      <muc-callout type="info">
        <template #content>
          <p>Um diese Inhalte anzuzeigen, müssen Sie sich anmelden.</p>
        </template>
      </muc-callout>
    </div>
  </div>
</template>

<script setup lang="ts">
import type AuthorizationEventDetails from "@/types/AuthorizationEventDetails.ts";

import { MucCallout } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref } from "vue";

import { useDBSLoginWebcomponentPlugin } from "@/composables/DBSLoginWebcomponentPlugin.ts";
import EinrichtungOverviewVueComponent from "@/einrichtung-overview.ce.vue";
import TraegerOverviewVueComponent from "@/traeger-overview.ce.vue";
import { setAccessToken } from "@/util/constants";
import VorgaengeOverviewVueComponent from "@/vorgaenge-overview.ce.vue";

const { loggedIn } = useDBSLoginWebcomponentPlugin(_authChangedCallback);

function _authChangedCallback(authEventDetails?: AuthorizationEventDetails) {
  if (authEventDetails && authEventDetails.accessToken) {
    console.debug("Receiving new authevent...");

    setAccessToken(authEventDetails.accessToken);
    token.value = authEventDetails.accessToken;
  }
}

const token = ref<string | undefined>();
const unknownTraeger = ref<boolean>(false);
const loadingErrorTraeger = ref<boolean>(false);
const loadingErrorEinrichtungen = ref<boolean>(false);
const loadingErrorVorgaenge = ref<boolean>(false);

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
  vorgangDetailsUrl: {
    type: String,
    default: null,
  },
  pageSize: {
    type: Number,
    default: 5,
  },
});
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.m-callout {
  padding-top: 1.5rem;
}

.flex-container {
  display: flex;
  margin-bottom: 1rem;
  gap: 2rem;
  align-items: stretch;
}

.full-width {
  width: 100%;
}

/* Each top area */
.flex-area {
  flex: 1;
  display: flex;
  justify-content: left;
}

.bordered-area {
  padding-left: 1.5rem;
  padding-right: 1.5rem;
}

.bottom-area {
  width: 100%;
}
</style>
