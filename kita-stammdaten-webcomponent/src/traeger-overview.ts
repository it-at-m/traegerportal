import { defineCustomElement } from "vue";

import RefarchHelloWorldVueComponent from "@/traeger-overview.ce.vue";

// convert into custom element constructor
const RefarchHelloWorldWebComponent = defineCustomElement(
  RefarchHelloWorldVueComponent
);

// register
customElements.define(
  "traeger-overview",
  RefarchHelloWorldWebComponent
);
