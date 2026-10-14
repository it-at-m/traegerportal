import { defineCustomElement } from "vue";

import VorgangListVueComponent from "@/vorgang-list.ce.vue";

// convert into custom element constructor
const VorgangListWebcomponent = defineCustomElement(VorgangListVueComponent);

// register
customElements.define("vorgang-list", VorgangListWebcomponent);
