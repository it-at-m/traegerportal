import { getAPIBaseURL } from "@/util/constants";

export default class StammdatenService {
  getTraeger(token: string): Promise<Response> {
    const url = getAPIBaseURL() + "/meintraeger";

    return fetch(url, {
      method: "GET",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json",
      },
      credentials: "include",
    });
  }

  searchEinrichtungen(token: string): Promise<Response> {
    // ignore paging for now
    const url = getAPIBaseURL() + "/meintraeger/einrichtungen?size=999";

    return fetch(url, {
      method: "GET",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json",
      },
      credentials: "include",
    });
  }
}
