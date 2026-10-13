import { defineCustomElement } from "vue";

import VorgangOverviewCe from "@/vorgang-overview.ce.vue";

// convert into custom element constructor
const VorgangOverviewWebcomponent = defineCustomElement(
  VorgangOverviewCe
);

// register
customElements.define("vorgang-overview", VorgangOverviewWebcomponent);
