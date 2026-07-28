import { defineCustomElement } from "vue";

import TraegerDetailsVueComponent from "@/traeger-details.ce.vue";

// convert into custom element constructor
const TraegerDetailsWebComponent = defineCustomElement(
  TraegerDetailsVueComponent
);

// register
customElements.define("traeger-details", TraegerDetailsWebComponent);
