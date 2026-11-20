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
      <traegerportal-page
        :title="einrichtung?.name ?? ''"
        :tagline="
          formatEinrichtungsart(einrichtung?.merkmale?.einrichtungsartLhm)
        "
        include-footer
      >
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
          <muc-label-value-display
            label="KIBIG.web-Nummer"
            :value="einrichtung?.merkmale?.kibigWebId"
          />
          <muc-label-value-display
            label="Adresse"
            :value="formatAdresse(einrichtung?.adresse)"
          />
          <muc-label-value-display
            label="Stadtbezirk"
            :value="einrichtung?.stadtbezirk?.name"
          />
          <muc-label-value-display
            label="Schulsprengel"
            :value="einrichtung?.schulsprengel?.schule"
          />
          <a :href="hasLink ? einrichtungBearbeitenLink : undefined">
            <muc-button class="link-button">
              Einrichtungsdaten ändern
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
        </template>
        <template #content>
          <h2>Kontaktdaten</h2>
          <dl class="m-descriptionlist">
            <muc-descriptionlist-element
              term="Telefonnummer"
              :description="einrichtung?.kontaktdaten?.telefon"
            />
            <muc-descriptionlist-element
              term="Faxnummer"
              :description="einrichtung?.kontaktdaten?.fax"
            />
            <muc-descriptionlist-element
              term="E-Mail-Adresse"
              :description="einrichtung?.kontaktdaten?.email"
            />
            <muc-descriptionlist-element
              term="Webseite"
              :description="einrichtung?.kontaktdaten?.homepageUrl"
            />
            <muc-descriptionlist-element
              term="Notfall-Telefonnummer"
              :description="einrichtung?.kontaktdaten?.notfallTelefon"
            />
          </dl>
          <muc-divider />
          <h2>Platzstruktur</h2>
          <dl class="m-descriptionlist">
            <muc-descriptionlist-element
              term="Plätze Gesamt"
              :description="
                einrichtung?.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeGesamt
              "
            />
            <muc-descriptionlist-element
              term="Plätze Kinderkrippe"
              :description="
                einrichtung?.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeKinderkrippe
              "
            />
            <muc-descriptionlist-element
              term="Plätze Kindergarten"
              :description="
                einrichtung?.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeKindergarten
              "
            />
            <muc-descriptionlist-element
              term="Plätze Hort"
              :description="
                einrichtung?.aktuellGueltigeBetriebserlaubnis
                  ?.aktuellGueltigeSollbelegung?.plaetzeHort
              "
            />
          </dl>
        </template>
        <template #footer>
          <einrichtungen-slider :token="token ?? ''"></einrichtungen-slider>
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
import EinrichtungenSlider from "./components/EinrichtungenSlider.vue";
import MucDescriptionlistElement from "./components/MucDescriptionlistElement.vue";
import MucLabelValueDisplay from "./components/MucLabelValueDisplay.vue";
import TraegerportalPage from "./components/TraegerportalPage.vue";
import { AuthService } from "./util/AuthService.ts";
import {
  formatAdresse,
  formatEinrichtungsart,
  formatEinrichtungsstatus,
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
@import "../public/style.css";

.einrichtung-status {
  border: 1px solid;
}

.kita-banner-content {
  width: 100%;
  max-width: 100%;
}
</style>
