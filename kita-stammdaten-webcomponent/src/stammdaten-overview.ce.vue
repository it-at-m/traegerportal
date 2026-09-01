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
      v-else-if="loadingError"
      type="error"
    >
      <template #content>
        <p>
          Die Schnittstelle ist nicht erreichbar. Bitte versuchen Sie es zu
          einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div
      v-else
      class="flex-container full-width"
    >
      <traeger-overview-vue-component
        class="flex-area full-width"
        :details-url="traegerDetailsUrl"
        :token="token"
        @unknown-traeger="unknownTraeger = true"
        @loading-error="loadingError = true"
      />
      <muc-card
        id="vorgang-anzeige"
        title="Vorgänge"
        :disabled="true"
        class="flex-area"
        ><template #content>TODO</template></muc-card
      >
    </div>
    <einrichtung-overview-vue-component
      :details-url="einrichtungDetailsUrl"
      :page-size="pageSize"
      :token="token"
      class="bordered-area full-width"
    />
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
import type AuthorizationEventDetails from "@/types/AuthorizationEventDetails.ts";

import { MucCallout, MucCard } from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { ref } from "vue";

import { useDBSLoginWebcomponentPlugin } from "@/composables/DBSLoginWebcomponentPlugin.ts";
import EinrichtungOverviewVueComponent from "@/einrichtung-overview.ce.vue";
import TraegerOverviewVueComponent from "@/traeger-overview.ce.vue";
import { setAccessToken } from "@/util/constants";

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
const loadingError = ref<boolean>(false);

defineProps({
  traegerDetailsUrl: {
    type: String,
    default: null,
  },
  einrichtungDetailsUrl: {
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
}

.full-width {
  width: 100%;
}

/* Each top area */
.flex-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: left;
  margin-left: 1.5rem;
  margin-right: 1.5rem;
}

.bordered-area {
  margin-left: 1.5rem;
  margin-right: 1.5rem;
}

.bottom-area {
  width: 100%;
}
</style>
