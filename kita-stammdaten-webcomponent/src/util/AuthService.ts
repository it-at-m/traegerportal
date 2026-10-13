import type AuthorizationEventDetails from "@/types/AuthorizationEventDetails.ts";

import { ref, watch } from "vue";

import { useDBSLoginWebcomponentPlugin } from "@/composables/DBSLoginWebcomponentPlugin.ts";

export class AuthService {
  readonly token = ref<string>();
  readonly loggedIn: ReturnType<
    typeof useDBSLoginWebcomponentPlugin
  >["loggedIn"];

  constructor() {
    const { loggedIn } = useDBSLoginWebcomponentPlugin(
      this.authChangedCallback
    );

    this.loggedIn = loggedIn;

    watch(loggedIn, (isLoggedIn) => {
      if (!isLoggedIn) {
        this.token.value = undefined;
      }
    });
  }

  readonly authChangedCallback = (
    authEventDetails?: AuthorizationEventDetails
  ): void => {
    if (authEventDetails?.accessToken) {
      this.token.value = authEventDetails.accessToken;
    }
  };
}
