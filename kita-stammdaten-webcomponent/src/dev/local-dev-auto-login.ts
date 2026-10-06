import { AUTH_REFRESH_EVENT_NAME } from "../composables/DBSLoginWebcomponentPlugin.ts";
import AuthorizationEventDetails from "../types/AuthorizationEventDetails.ts";

// Local development helper that bypasses the login button by retrieving a Keycloak access token and dispatching the authorization refresh event.

const response = await fetch(import.meta.env.VITE_KEYCLOAK_TOKEN_URL, {
  method: "POST",
  headers: {
    "Content-Type": "application/x-www-form-urlencoded",
  },
  body: new URLSearchParams({
    grant_type: "password",
    client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
    client_secret: import.meta.env.VITE_KEYCLOAK_CLIENT_SECRET,
    username: import.meta.env.VITE_KEYCLOAK_USERNAME,
    password: import.meta.env.VITE_KEYCLOAK_PASSWORD,
    scope: import.meta.env.VITE_KEYCLOAK_SCOPE,
  }),
});

const tokenResponse = await response.json();
console.debug(tokenResponse);

const eventDetails = new AuthorizationEventDetails(
  import.meta.env.VITE_AUTOLOGIN_BUERGERNAME,
  import.meta.env.VITE_AUTOLOGIN_BUERGERMAIL,
  import.meta.env.VITE_AUTOLOGIN_LOGINPROVIDER,
  import.meta.env.VITE_AUTOLOGIN_TRUSTLEVEL,
  tokenResponse.access_token
);

document.dispatchEvent(
  new CustomEvent(AUTH_REFRESH_EVENT_NAME, {
    detail: eventDetails,
  })
);