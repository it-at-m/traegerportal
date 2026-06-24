export const BASE_API_PATH = import.meta.env.VITE_BASE_API_PATH;

export const enum STATUS_INDICATORS {
  SUCCESS = "success",
  INFO = "info",
  WARNING = "warning",
  ERROR = "error",
}

let ACCESS_TOKEN: string | undefined = undefined;

export function getAPIBaseURL(): string {
  if (import.meta.env.VITE_VUE_APP_API_URL) {
    return import.meta.env.VITE_VUE_APP_API_URL;
  } else {
    return new URL(import.meta.url).origin;
  }
}

export function setAccessToken(newAccessToken: string): void {
  ACCESS_TOKEN = newAccessToken;
}

export function getAccessToken(): string | undefined {
  return ACCESS_TOKEN;
}

class Cookie {
  key: string;
  value: string;

  constructor(key: string, value: string) {
    this.key = key;
    this.value = value;
  }
}

/**
 * Get Cookie by its name . Undefined if cookie is not present.
 * @param cookieName Name of the Cookie
 */
export function getCookie(cookieName: string): Cookie | undefined {
  let cookie: Cookie | undefined = undefined;

  document.cookie.split(";").forEach(function (el) {
    const [key, value] = el.split("=");
    if (key && value && key.trim() == cookieName) {
      cookie = new Cookie(key.trim(), value);
    }
  });
  return cookie;
}

export function getXSRFToken() {
  const XSRFToken = getCookie("XSRF-TOKEN");
  if (XSRFToken == undefined) {
    console.debug("XRSF-Token Konnte nicht aus Cookie geholt werden");
    return "";
  }
  return XSRFToken.value;
}

export function getDateInGermanDateFormat(
  date: Date | undefined,
  showTime = true
) {
  if (date) {
    const options: Intl.DateTimeFormatOptions = showTime
      ? {
          year: "numeric",
          month: "numeric",
          day: "numeric",
          hour: "2-digit",
          minute: "2-digit",
        }
      : { year: "numeric", month: "numeric", day: "numeric" };
    return date.toLocaleString("de-DE", options) + (showTime ? " Uhr" : "");
  } else {
    return "-";
  }
}
