import EinrichtungDTO from "@/types/EinrichtungDTO";

export default class StammdatenService {
  getTraeger(): Promise<Response> {
    const responseData = {
      id: "123",
      name: "Testname",
      form: "TestForm",
      adresse: "Teststraße 1, 80331 München",
      team: "Team 1",
    };

    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    const mockResponse: Response = {
      status: 200,
      statusText: "OK",
      headers: headers,
      ok: true,
      redirected: false,
      body: new Blob([JSON.stringify(responseData)]).stream(),
      bodyUsed: false,

      json: () => Promise.resolve(responseData),
    };

    return new Promise((resolve) => resolve(mockResponse));
  }

  searchEinrichtungen(): Promise<Response> {
    /* TODO: use service
    const url = getAPIBaseURL() + "/clients/api/einrichtungsverwaltung-backend/external/traegerportal/traeger";

    return fetch(url, {
      method: "GET",
      headers: {
        Authorization: "Bearer " + getAccessToken(),
        "Content-Type": "application/json",
      },
      credentials: "include",
    });*/

    const responseData = [
      new EinrichtungDTO(
        "123",
        "Testeinrichtung 1",
        "Teststraße 1, 80331 München",
        "1620011000",
        "In Betrieb"
      ),
      new EinrichtungDTO(
        "124",
        "Testeinrichtung 2",
        "Teststraße 2, 80331 München",
        "1620011001",
        "Vorplanung"
      ),
    ];
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    const mockResponse: Response = {
      status: 200,
      statusText: "OK",
      headers: headers,
      ok: true,
      redirected: false,
      body: new Blob([JSON.stringify(responseData)]).stream(),
      bodyUsed: false,

      json: () => Promise.resolve(responseData),
    };

    return new Promise((resolve) => resolve(mockResponse));
  }
}
