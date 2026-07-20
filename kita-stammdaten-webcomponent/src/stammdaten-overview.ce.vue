<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <div class="wide-container">
      <traeger-overview-vue-component
        class="flex-area"
        :stammdaten-url="stammdatenUrl"
        :token="token"
      />
      <div class="flex-area">Vorgänge</div>
    </div>
    <einrichtung-overview-vue-component
      :stammdaten-url="stammdatenUrl"
      :page-size="pageSize"
      :token="token"
    />
  </div>
  <div v-else>
    <muc-callout type="info">
      <template #content>
        <p>Um diese Inhalte anzuzeigen, müssen Sie sich anmelden.</p>
      </template>
    </muc-callout>
    <button @click="dummyLogin">Dummy-Login</button>
  </div>
</template>

<script setup lang="ts">
import type AuthorizationEventDetails from "@/types/AuthorizationEventDetails.ts";

import { MucCallout } from "@muenchen/muc-patternlab-vue";
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
    setAccessToken(authEventDetails.accessToken);
    token.value = authEventDetails.accessToken;
  }
}

function dummyLogin() {
  token.value = "test";
  loggedIn.value = true;
}

const token = ref<string | undefined>();

defineProps({
  stammdatenUrl: {
    type: String,
    default: null,
  },
  pageSize: {
    type: Number,
    default: 10,
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

.wide-container {
  display: flex;
  width: 100%;
}

/* Each top area */
.flex-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-area {
  width: 100%;
}
</style>
