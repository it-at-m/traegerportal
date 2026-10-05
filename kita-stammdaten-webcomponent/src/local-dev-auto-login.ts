import { AUTH_REFRESH_EVENT_NAME } from "./composables/DBSLoginWebcomponentPlugin.ts";
import AuthorizationEventDetails, { KEYCLOAK_AUTH_LEVEL4 } from "./types/AuthorizationEventDetails.ts";

const response = await fetch(
  `http://keycloak:8100/auth/realms/local_realm/protocol/openid-connect/token`,
  {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "password",
      client_id: "local",
      client_secret: "client_secret",
      username: "writer",
      password: "writer",
      scope: "profile local_audience email openid",
    }),
  }
);

const tokenResponse = await response.json();
console.debug(tokenResponse);

const eventDetails = new AuthorizationEventDetails(
  "Vorname Nachname",
  "vorname.nachname@muenchen.test",
  "example",
  KEYCLOAK_AUTH_LEVEL4,
  tokenResponse.access_token
);

document.dispatchEvent(
  new CustomEvent(AUTH_REFRESH_EVENT_NAME, {
    detail: eventDetails,
  })
);