<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-spinner
      v-if="loading"
      size="200px"
      text="Lade Einrichtung ..."
    />
    <muc-callout
      v-else-if="dataLoadingError"
      type="error"
    >
      <template #content>
        <p>
          Die Einrichtungsdaten können derzeit nicht geladen werden. Bitte
          versuchen Sie es zu einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div
      v-else
      style="width: 100%"
    >
      <traegerportal-page :title="einrichtung?.name ?? ''" :tagline="formatEinrichtungsart(einrichtung?.merkmale?.einrichtungsartLhm)">
        <template #content>
          <h2>Kontaktdaten</h2>
          <dl class="m-descriptionlist">
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Telefonnummer</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.kontaktdaten?.telefon) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Faxnummer</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.kontaktdaten?.fax) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">E-Mail-Adresse</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.kontaktdaten?.email) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Webseite</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.kontaktdaten?.homepageUrl) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Notfall-Telefonnummer</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.kontaktdaten?.notfallTelefon) }}
              </dd>
            </div>
          </dl>
          <muc-divider />
          <h2>Platzstruktur</h2>
          <dl class="m-descriptionlist">
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Plätze Gesamt</dt>
              <dd class="m-descriptionlist__description">
                {{
                  textOrFallback(
                    einrichtung?.aktuellGueltigeBetriebserlaubnis
                      ?.aktuellGueltigeSollbelegung?.plaetzeGesamt
                  )
                }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Plätze Kinderkrippe</dt>
              <dd class="m-descriptionlist__description">
                {{
                  textOrFallback(
                    einrichtung?.aktuellGueltigeBetriebserlaubnis
                      ?.aktuellGueltigeSollbelegung?.plaetzeKinderkrippe
                  )
                }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Plätze Kindergarten</dt>
              <dd class="m-descriptionlist__description">
                {{
                  textOrFallback(
                    einrichtung?.aktuellGueltigeBetriebserlaubnis
                      ?.aktuellGueltigeSollbelegung?.plaetzeKindergarten
                  )
                }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Plätze Hort</dt>
              <dd class="m-descriptionlist__description">
                {{
                  textOrFallback(
                    einrichtung?.aktuellGueltigeBetriebserlaubnis
                      ?.aktuellGueltigeSollbelegung?.plaetzeHort
                  )
                }}
              </dd>
            </div>
          </dl>
        </template>
        <template #intro-content>
          <div>
            <b
              :class="`einrichtung-status ${einrichtung?.aktuellGueltigerStatus?.status}`"
            >
              {{
                formatEinrichtungsstatus(einrichtung?.aktuellGueltigerStatus)
              }}
            </b>
          </div>
          <dl class="m-descriptionlist">
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">KIBIG.web-Nummer</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.merkmale?.kibigWebId) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Adresse</dt>
              <dd class="m-descriptionlist__description">
                {{ formatAdresse(einrichtung?.adresse) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Stadtbezirk</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.stadtbezirk?.name) }}
              </dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Schulsprengel</dt>
              <dd class="m-descriptionlist__description">
                {{ textOrFallback(einrichtung?.schulsprengel?.schule) }}
              </dd>
            </div>
          </dl>
          <a :href="hasLink ? einrichtungBearbeitenLink : undefined">
            <muc-button>
              Einrichtungsdaten ändern
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
        </template>
      </traegerportal-page>
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
import TraegerportalPage from "./components/TraegerportalPage.vue";

import {
  MucButton,
  MucCallout,
  MucDivider,
  MucIcon,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import EinrichtungDTO from "@/types/EinrichtungDTO";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import { AuthService } from "./util/AuthService.ts";
import {
  formatAdresse,
  formatEinrichtungsart,
  formatEinrichtungsstatus,
  textOrFallback,
} from "./util/format";

const props = defineProps({
  bearbeitenUrl: {
    type: String,
    default: null,
  },
});

const { token, loggedIn } = new AuthService();

const einrichtung = ref<EinrichtungDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function getEinrichtungId() {
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);
  const einrichtungId = urlParams.get("einrichtungId");

  return einrichtungId;
}

function loadEinrichtung(einrichtungId: string | null) {
  console.debug("Loading Einrichtung data for id " + einrichtungId + " ...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }
  if (!einrichtungId) {
    console.debug("Skipping, because no einrichtungId is specified.");
    return;
  }

  loading.value = true;
  const service = new StammdatenService();
  service
    .getEinrichtung(token.value, einrichtungId)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: EinrichtungDTO) => {
          einrichtung.value = response;
          dataLoadingError.value = false;
        });
      } else {
        resp.text().then((errBody) => {
          dataLoadingError.value = true;
          throw Error(errBody);
        });
      }
    })
    .catch((error) => {
      dataLoadingError.value = true;
      console.debug(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

watch(
  token,
  (newToken) => {
    if (newToken) {
      loadEinrichtung(getEinrichtungId());
    }
  },
  { immediate: true }
);

const einrichtungBearbeitenLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.bearbeitenUrl);
});

const hasLink = computed(() => {
  return props.bearbeitenUrl && einrichtung.value;
});
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.m-intro {
  --intro-bg-color: #e1d7ff;
  background: var(--intro-bg-color);
  margin-bottom: 0 !important;
}

.kita-intro {
  --intro-bg-color: #e1d7ff;
  background: var(--intro-bg-color);
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  padding-top: 3rem;
  padding-bottom: 3rem;
  margin-top: 2rem;
}

.page-body {
  margin-left: 1.5rem;
  margin-top: 1rem;
}

.m-descriptionlist {
  margin-bottom: 1rem;
}

.muc-divider {
  margin-bottom: 1rem;
}

h2 {
  padding-bottom: 1rem;
  align-items: center;
  gap: 2rem;
  display: flex;
}

.einrichtung-status {
  display: inline-block;
  padding: 0.25em 0.75em;
  background-color: #d3d3d3;
  border-radius: 12px;
  white-space: nowrap;
  user-select: none;
  border: 1px solid;
  margin-bottom: 1rem;
}

.einrichtung-status.VORPLANUNG {
  background-color: #f9e79f; /* light yellow */
}

.einrichtung-status.VOR_BETRIEB {
  background-color: #aed6f1; /* light blue */
}

.einrichtung-status.IN_BETRIEB {
  background-color: #a9dfbf; /* light green */
}

.m-intro-summary-text__text p {
  margin-bottom: 0;
}

.m-intro-vertical__tagline {
  margin-bottom: 0;
}
</style>
