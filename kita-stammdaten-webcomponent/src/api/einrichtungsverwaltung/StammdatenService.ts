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
}