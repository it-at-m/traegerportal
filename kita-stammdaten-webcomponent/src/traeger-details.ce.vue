<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-spinner
      v-if="loading"
      size="200px"
      text="Lade Träger ..."
    />
    <muc-callout
      v-else-if="dataLoadingError"
      type="error"
    >
      <template #content>
        <p>
          Die Trägerdaten können derzeit nicht geladen werden. Bitte versuchen
          Sie es zu einem späteren Zeitpunkt erneut.
        </p>
      </template>
    </muc-callout>
    <div
      v-else
      style="width: 100%"
    >
      <traeger-page :token="token">
        <template #intro-content="{ traeger }">
          <dl class="m-descriptionlist">
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Träger-ID</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.id) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Adresse</dt>
              <dd class="m-descriptionlist__description">{{ formatAdresse(traeger?.adresse) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Rechtsform</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.traegerform) }}</dd>
            </div>
          </dl>
          <a :href="traegerBearbeitenLink">
            <muc-button>
              Trägerdaten ändern
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
        </template>
        <template #content="{ traeger }">
          <h2>Kontaktdaten</h2>
          <dl class="m-descriptionlist">
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Telefonnummer</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.kontaktdaten?.telefon) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Faxnummer</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.kontaktdaten?.fax) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">E-Mail-Adresse</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.kontaktdaten?.email) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Webseite</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.kontaktdaten?.homepageUrl) }}</dd>
            </div>
            <div class="m-descriptionlist__group">
              <dt class="m-descriptionlist__term">Notfall-Telefonnummer</dt>
              <dd class="m-descriptionlist__description">{{ textOrFallback(traeger?.kontaktdaten?.notfallTelefon) }}</dd>
            </div>
          </dl>
          <muc-divider />
          <h2>Ansprechpersonen</h2>
          <a :href="hasLink ? traegerBearbeitenLink : undefined">
            <muc-button>
              Neue Ansprechperson erfassen
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
          <muc-accordion
            id="ansprechpartner"
            multiple
            :detail-page-margin="false"
          >
            <template #default="{onOpen, onClose, activeItems}">
              <muc-accordion-item
                v-for="ansprechpartner in traeger?.ansprechpartner"
                id="accordion-item-1"
                :key="ansprechpartner.id"
                :header="formatAnsprechpartnerFullname(ansprechpartner)"
                :active-items="activeItems"
                @open="onOpen"
                @close="onClose"
              >
                <template #content>
                  <dl class="m-descriptionlist">
                    <div class="m-descriptionlist__group">
                      <dt class="m-descriptionlist__term">Telefonnummer</dt>
                      <dd class="m-descriptionlist__description">{{ textOrFallback(ansprechpartner.telefon) }}</dd>
                    </div>
                    <div class="m-descriptionlist__group">
                      <dt class="m-descriptionlist__term">E-Mail-Adresse</dt>
                      <dd class="m-descriptionlist__description">{{ textOrFallback(ansprechpartner.email) }}</dd>
                    </div>
                    <div class="m-descriptionlist__group">
                      <dt class="m-descriptionlist__term">Rollen</dt>
                      <dd class="m-descriptionlist__description">{{ formatTraegerRollen(ansprechpartner) }}</dd>
                    </div>
                    <div class="m-descriptionlist__group">
                      <dt class="m-descriptionlist__term">Anrede</dt>
                      <dd class="m-descriptionlist__description">{{ formatAnrede(ansprechpartner.anrede) }}</dd>
                    </div>
                    <a :href="hasLink ? traegerBearbeitenLink : undefined">
                      <muc-button>
                        Ansprechperson bearbeiten
                        <muc-icon icon="ext-link" />
                      </muc-button>
                    </a>
                  </dl>
                </template>
              </muc-accordion-item>
            </template>
          </muc-accordion>
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
import TraegerPage from "./components/TraegerPage.vue";

import {
  MucButton,
  MucCallout,
  MucDivider,
  MucIcon,
  MucAccordion,
  MucAccordionItem,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed } from "vue";

import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import { AuthService } from "./util/AuthService.ts";
import {
  formatAdresse,
  formatAnrede,
  formatAnsprechpartnerFullname,
  formatTraegerRollen,
  textOrFallback,
} from "./util/format";

const props = defineProps({
  bearbeitenUrl: {
    type: String,
    default: null,
  },
});

const { token, loggedIn } = new AuthService();

const traegerBearbeitenLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.bearbeitenUrl);
});
</script>

<style>
@import url("https://assets.muenchen.de/mde/1.1.19/css/style.css");
@import "@muenchen/muc-patternlab-vue/assets/css/custom-style.css";
@import "@muenchen/muc-patternlab-vue/style.css";

.m-descriptionlist {
  margin-bottom: 1rem;
}

.muc-divider {
  margin-bottom: 1rem;
}

.m-component-accordion {
  padding-top: 0;
}

.m-accordion__section {
  margin-bottom: 0;
  --accordion-border-color: #bdd4ea;
}

.m-accordion__section-button {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
}

.m-component-accordion .container {
  padding-left: 0;
  padding-right: 0;
}

.m-intro-summary-text__text p {
  margin-bottom: 0;
}

.m-intro-vertical__tagline {
  margin-bottom: 0;
}
</style>
