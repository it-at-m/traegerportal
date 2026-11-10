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
      <muc-intro
        :title="traeger?.name ?? ''"
        tagline="Mein Träger"
        :divider="false"
      >
        <template #default>
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
          <a :href="hasLink ? traegerBearbeitenLink : undefined">
            <muc-button>
              Trägerdaten ändern
              <muc-icon icon="ext-link" />
            </muc-button>
          </a>
        </template>
      </muc-intro>
      <div class="page-body">
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
        <h2>Ansprechpersonen
          
          <a :href="hasLink ? traegerBearbeitenLink : undefined">
            <muc-button>
              Neue Ansprechperson erfassen
              <muc-icon icon="ext-link" />
            </muc-button>
          </a></h2>
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
      </div>
      <div
        v-if="traeger?.team"
        class="kita-intro"
      >
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
      </div>
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
  MucIntro,
  MucAccordion,
  MucAccordionItem,
  MucSpinner,
} from "@muenchen/muc-patternlab-vue";
import customIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/custom-icons.svg?raw";
import mucIconsSprite from "@muenchen/muc-patternlab-vue/assets/icons/muc-icons.svg?raw";
import { computed, ref, watch } from "vue";

import StammdatenService from "@/api/einrichtungsverwaltung/StammdatenService.ts";
import { getUrlWithOriginAsBaseAndDefaultParams } from "@/util/constants.ts";
import TraegerDTO from "./types/TraegerDTO";
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

const traeger = ref<TraegerDTO>();

const loading = ref<boolean>();
const dataLoadingError = ref<boolean>();

function loadTraeger() {
  console.debug("Loading traeger data...");

  if (!token.value) {
    console.debug("Skipping, because no token is known yet.");
    return;
  }

  loading.value = true;
  const service = new StammdatenService();
  service
    .getTraeger(token.value)
    .then((resp) => {
      if (resp.ok) {
        resp.json().then((response: TraegerDTO) => {
          traeger.value = response;
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
      loadTraeger();
    }
  },
  { immediate: true }
);

const traegerBearbeitenLink = computed(() => {
  return getUrlWithOriginAsBaseAndDefaultParams(props.bearbeitenUrl);
});

const hasLink = computed(() => {
  return props.bearbeitenUrl && traeger.value;
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

h2 {
  padding-bottom: 1rem;
  align-items: center;
  gap: 2rem;
  display: flex;
}
</style>
