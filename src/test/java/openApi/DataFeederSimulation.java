package openApi;

// required for Gatling core structure DSL

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class DataFeederSimulation extends Simulation
{
    @Override
    public void before()
    {
        System.out.println("Simulation is about to start!");
    }

    HttpProtocolBuilder httpProtocol = http.baseUrl("https://qa-1-api.staging.aspireapp.com");

    FeederBuilder<String> feeder = csv("testData/UserDetails.csv").random();

    ChainBuilder getData = exec(feed(feeder),http("Get Token").post("/v1/auth/token")
            .header("x-aspire-application", "CNSING")
            .body(ElFileBody("testData/AuthTokenRequest.json"))
            //.body(StringBody("{\"email\": \"#{username}\",\"length\": 4}"))
            .check(status().is(200)));

    ScenarioBuilder getDataScn = scenario("Get Data From CSV").exec(getData).exec(session -> {
                System.out.println(LocalDateTime.now()+": "+session.getString("username"));
                return session;
            }
    );

    {
        setUp(
            getDataScn.injectOpen(constantUsersPerSec(2).during(Duration.ofSeconds(5)))
        ).protocols(httpProtocol);
    }

    @Override
    public void after()
    {
        System.out.println("Simulation is finished!");
    }
}
