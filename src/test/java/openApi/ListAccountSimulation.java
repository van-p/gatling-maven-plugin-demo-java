package openApi;

// required for Gatling core structure DSL

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ListAccountSimulation extends Simulation
{
    @Override
    public void before()
    {
        System.out.println("Simulation is about to start!");
    }

    HttpProtocolBuilder httpProtocol = http.baseUrl("https://qa-1-api.staging.aspireapp.com/public");

    ChainBuilder getToken = exec(http("Get Token").post("/v1/login")
            .header("Content-Type", "application/json")
            .body(RawFileBody("testData/GetToken.json"))
            .check(status().is(200))
            .check(jsonPath("$.access_token").saveAs("token")));

    ChainBuilder getListAccount = exec(http("Get List Account").get("/v1/accounts")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${token}")
            .check(status().is(200))
            .check(jsonPath("$.data[0].id").saveAs("accountId")));

    ScenarioBuilder getTokenScn = scenario("Get Token").exec(getToken).exec(session -> {
                System.out.println(session.getString("responseBody"));
                return session;
            }
    );
    ScenarioBuilder getListAccountScn = scenario("Get List Account").exec(getToken, getListAccount).exec(session -> {
        System.out.println(session.getString("accountId"));
        return session;
    });

    // HttpProtocol configured globally
    {
        setUp(
            //getTokenScn.injectOpen(atOnceUsers(1)),
            getListAccountScn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }

    @Override
    public void after()
    {
        System.out.println("Simulation is finished!");
    }
}
