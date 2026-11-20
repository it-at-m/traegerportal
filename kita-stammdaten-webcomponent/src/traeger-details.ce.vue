<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="mucIconsSprite" />
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div v-html="customIconsSprite" />
  <div v-if="loggedIn">
    <muc-callout
      v-if="dataLoadingError"
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
      <traeger-page
        :token="token"
        @loading-error="dataLoadingError = true"
      >
        <template #intro-content="{ traeger }">
          <dl class="m-descriptionlist">
            <muc-label-value-display
              label="Träger-ID"
              :value="traeger?.id"
            />
            <muc-label-value-display
              label="Adresse"
              :value="formatAdresse(traeger?.adresse)"
            />
            <muc-label-value-display
              label="Rechtsform"
              :value="traeger?.traegerform"
            />
          </dl>
          <a :href="traegerBearbeitenLink">
            <muc-button class="link-button">
              Trägerdaten ändern
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
        </template>
        <template #content="{ traeger }">
          <h2>Kontaktdaten</h2>
          <dl class="m-descriptionlist">
            <muc-descriptionlist-element
              term="Telefonnummer"
              :description="traeger?.kontaktdaten?.telefon"
            />
            <muc-descriptionlist-element
              term="Faxnummer"
              :description="traeger?.kontaktdaten?.fax"
            />
            <muc-descriptionlist-element
              term="E-Mail-Adresse"
              :description="traeger?.kontaktdaten?.email"
            />
            <muc-descriptionlist-element
              term="Webseite"
              :description="traeger?.kontaktdaten?.homepageUrl"
            />
            <muc-descriptionlist-element
              term="Notfall-Telefonnummer"
              :description="traeger?.kontaktdaten?.notfallTelefon"
            />
          </dl>
          <muc-divider />
          <h2>Ansprechpersonen</h2>
          <a :href="ansprechpartnerBearbeitenLink(null)">
            <muc-button class="link-button">
              Neue Ansprechperson erfassen
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
          <muc-accordion
            id="ansprechpartner"
            multiple
            :detail-page-margin="false"
          >
            <template #default>
              <muc-accordion-item
                v-for="ansprechpartner in traeger?.ansprechpartner"
                id="accordion-item-1"
                :key="ansprechpartner.id"
                :header="formatAnsprechpartnerFullname(ansprechpartner)"
              >
                <template #content>
                  <dl class="m-descriptionlist">
                    <muc-descriptionlist-element
                      term="Anrede"
                      :description="formatAnrede(ansprechpartner.anrede)"
                    />
                    <muc-descriptionlist-element
                      term="Telefonnummer"
                      :description="ansprechpartner.telefon"
                    />
                    <muc-descriptionlist-element
                      term="E-Mail-Adresse"
                      :description="ansprechpartner.email"
                    />
                    <muc-descriptionlist-element
                      term="Rollen"
                      :description="formatTraegerRollen(ansprechpartner)"
                    />
                    <a :href="ansprechpartnerBearbeitenLink(ansprechpartner)">
                      <muc-button class="link-button">
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
import {
  MucAccordion,
  MucAccordionItem,
  MucButton,
  MucCallout,
  MucDivider,
  MucIcon,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref } from "vue";

import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import MucDescriptionlistElement from "./components/MucDescriptionlistElement.vue";
import MucLabelValueDisplay from "./components/MucLabelValueDisplay.vue";
import TraegerPage from "./components/TraegerPage.vue";
import { AnsprechpartnerDTO } from "./types/TraegerDTO.ts";
import { AuthService } from "./util/AuthService.ts";
import {
  formatAdresse,
  formatAnrede,
  formatAnsprechpartnerFullname,
  formatTraegerRollen,
} from "./util/format";

const props = defineProps({
  bearbeitenUrl: {
    type: String,
    default: null,
  },
  ansprechpartnerBearbeitenUrl: {
    type: String,
    default: null,
  },
});

const { token, loggedIn } = new AuthService();
const dataLoadingError = ref<boolean>();

const traegerBearbeitenLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.bearbeitenUrl);
});

const ansprechpartnerBearbeitenLink = function (ap?: AnsprechpartnerDTO) {
  return getUrlWithOriginAsBaseAndDefaultParams(
    props.ansprechpartnerBearbeitenUrl,
    {
      ansprechpartnerId: String(ap?.id ?? ""),
    }
  );
};
</script>

<style>
@import "../public/style.css";
</style>
