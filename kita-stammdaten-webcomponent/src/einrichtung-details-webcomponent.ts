import { defineCustomElement } from "vue";

import EinrichtungDetailsVueComponent from "@/einrichtung-details.ce.vue";

// convert into custom element constructor
const EinrichtungDetailsWebComponent = defineCustomElement(
  EinrichtungDetailsVueComponent
);

// register
customElements.define("einrichtung-details", EinrichtungDetailsWebComponent);
