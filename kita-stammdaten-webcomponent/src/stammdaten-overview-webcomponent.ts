import { defineCustomElement } from "vue";

import StammdatenOverviewVueComponent from "@/stammdaten-overview.ce.vue";

// convert into custom element constructor
const StammdatenOverviewWebComponent = defineCustomElement(
  StammdatenOverviewVueComponent
);

// register
customElements.define("stammdaten-overview", StammdatenOverviewWebComponent);
