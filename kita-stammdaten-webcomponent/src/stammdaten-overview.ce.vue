<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="!loggedIn">
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

const token = ref<string | undefined>(
  "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJFMEgxUTRVUXpNdHR1WmF2eDRwdG9OcWZ3VFdCWDRfeVBVVC1lS2d3RkhNIn0.eyJleHAiOjE3OTAyNjY5ODgsImlhdCI6MTc5MDIzMDk4OCwianRpIjoib25ydHJvOjRhNzU1M2YxLWI3N2QtNDU1Ny04NDAwLWFjMDFjOGM5YzNjZCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODEwMC9hdXRoL3JlYWxtcy9sb2NhbF9yZWFsbSIsImF1ZCI6WyJsb2NhbCIsImFjY291bnQiXSwic3ViIjoiOTBiYWU1ZDgtZTQ5MS00NTBkLThiMDctYmIzNDA0ZjkwODY3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9jYWwiLCJzaWQiOiIwNzFkMWExZC1hMTYwLTRiZmUtOTA5Yi01MDBiMWMzZGY4ZGYiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8qIiwiaHR0cHM6Ly8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWxvY2FsX3JlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfSwibG9jYWwiOnsicm9sZXMiOlsicmVhZGVyIl19fSwic2NvcGUiOiJsb2NhbF9hdWRpZW5jZSBvcGVuaWQgZW1haWwgcHJvZmlsZSIsImRhdGVudWViZXJtaXR0bGVyUHNldWRvbnltSWQiOiJkdS1kOWExMTg3NDA2YjJmMTQxNDVlYmFmZDllMGJlYTEwMDAwMTAwMDA1IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJyZWFkZXIgcmVhZGVyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoicmVhZGVyIiwiZ2l2ZW5fbmFtZSI6InJlYWRlciIsImZhbWlseV9uYW1lIjoicmVhZGVyIiwiZW1haWwiOiJyZWFkZXJAcmVhZGVyLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfcmVhZGVyIl19.LKPoe39hHOdcCBNrj5ynjqpPRpEC3uyH8kpIjJstL8nOT_5y_zHnj6LtCYdZSoKLMHzaWh03_Qk8NArXMY7Ipu8xYUjlAEtr9RAAvQwZnnMnhlxTzwfmU384hQ0in6nVgxuFh9nKF4d8H2nQRNUhAJvNOEyZXEPGYgjy1GzmDfzPEmd2kOw5-K9bnjPozNj1Y33LGzsBrC_S3qNdNx9n4S2TflzyNrdQFu5qf9UWc4HoemT67x_bl7CTSmAAttCpIsze9iQUvPAmw964B7kBfYPUJy5x0alUnSrJEfWCBilWiwyG2XswZHqELTBMoAtv_PCGDiqhePH0q_mEm5WwdQ"
);
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

.flex-container {
  display: flex;
  margin-bottom: 1rem;
  gap: 1rem;
  margin-left: 1.5rem;
  margin-right: 1.5rem;
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
}

.bordered-area {
  margin-left: 1.5rem;
  margin-right: 1.5rem;
}

.bottom-area {
  width: 100%;
}
</style>
