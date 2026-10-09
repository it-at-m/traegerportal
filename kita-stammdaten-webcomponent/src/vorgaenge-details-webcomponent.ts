import { defineCustomElement } from "vue";

import VorgaengeDetailsVueComponent from "@/vorgaenge-details.ce.vue";

// convert into custom element constructor
const VorgaengeDetailsWebcomponent = defineCustomElement(
  VorgaengeDetailsVueComponent
);

// register
customElements.define("vorgaenge-details", VorgaengeDetailsWebcomponent);
