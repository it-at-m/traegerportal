import EinrichtungDTO from "@/types/EinrichtungDTO";

export default class StammdatenService {
  getTraeger(traegerUkId: string): Promise<Response> {
    console.debug("Mocking traeger data: " + traegerUkId);
    const responseData = {
      id: "123",
      name: "Testname",
      form: "TestForm",
      adresse: "Teststraße 1, 80331 München",
      team: "Team 1",
    };

    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    // @ts-expect-error Ignore for now, as test data will be removed later
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

  searchEinrichtungen(traegerUkId: string): Promise<Response> {
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
    console.debug("Mocking einrichtungen for traeger: " + traegerUkId);

    const responseData = [
      new EinrichtungDTO(
        "123",
        "Testeinrichtung 1",
        "Teststraße 1",
        "80331 München",
        "1620011000",
        "In Betrieb"
      ),
      new EinrichtungDTO(
        "124",
        "Testeinrichtung 2",
        "Teststraße 2",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "125",
        "Testeinrichtung 3",
        "Teststraße 3",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "126",
        "Testeinrichtung 4",
        "Teststraße 4",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "127",
        "Testeinrichtung 5",
        "Teststraße 5",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "128",
        "Testeinrichtung 6",
        "Teststraße 6",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "129",
        "Testeinrichtung 7",
        "Teststraße 7",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "130",
        "Testeinrichtung 8",
        "Teststraße 8",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "131",
        "Testeinrichtung 9",
        "Teststraße 9",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "131",
        "Testeinrichtung 10",
        "Teststraße 10",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
      new EinrichtungDTO(
        "131",
        "Testeinrichtung 11",
        "Teststraße 11",
        "80331 München",
        "1620011001",
        "Vorplanung"
      ),
    ];
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    // @ts-expect-error Ignore for now, as test data will be removed later
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
