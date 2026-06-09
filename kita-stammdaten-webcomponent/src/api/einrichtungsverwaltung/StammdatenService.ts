import EinrichtungDTO from '@/types/EinrichtungDTO';

export default class StammdatenService {
  getTraeger(): Promise<Response> {
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

    const responseData = { id: "123", name:  "Testname", form: "TestForm", adresse: "Teststraße 1, 80331 München", team: "Team 1"};
    const mockResponse: Response = {
      status: 200,
      statusText: 'OK',
      headers: new Headers(),
      ok: true,
      redirected: false,
      body: responseData,
      bodyUsed: false,

      json: () => Promise.resolve(responseData)
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
      new EinrichtungDTO("123", "Testeinrichtung 1", "Teststraße 1, 80331 München", "1620011000", "In Betrieb"),
      new EinrichtungDTO("124", "Testeinrichtung 2", "Teststraße 2, 80331 München", "1620011001", "Vorplanung")
    ];
    const mockResponse: Response = {
      status: 200,
      statusText: 'OK',
      headers: new Headers(),
      ok: true,
      redirected: false,
      body: responseData,
      bodyUsed: false,

      json: () => Promise.resolve(responseData)
    };

    return new Promise((resolve) => resolve(mockResponse));
  }
}