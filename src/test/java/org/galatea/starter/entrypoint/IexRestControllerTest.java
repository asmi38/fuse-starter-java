package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@RequiredArgsConstructor
@Slf4j
// We need to do a full application start up for this one, since we want the feign clients to be instantiated.
// It's possible we could do a narrower slice of beans, but it wouldn't save that much test run time.
@SpringBootTest
// this gives us the MockMvc variable
@AutoConfigureMockMvc
// we previously used WireMockClassRule for consistency with ASpringTest, but when moving to a dynamic port
// to prevent test failures in concurrent builds, the wiremock server was created too late and feign was
// already expecting it to be running somewhere else, resulting in a connection refused
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class IexRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testGetSymbolsEndpoint() throws Exception {
    MvcResult result = this.mvc.perform(
        // note that we were are testing the fuse REST end point here, not the IEX end point.
        // the fuse end point in turn calls the IEX end point, which is WireMocked for this test.
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/iex/symbols")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        // some simple validations, in practice I would expect these to be much more comprehensive.
        .andExpect(jsonPath("$[0].symbol", is("A")))
        .andExpect(jsonPath("$[1].symbol", is("AA")))
        .andExpect(jsonPath("$[2].symbol", is("AAAU")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPrice() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?symbols=AAPL")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-lastTradedPrice.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].price").value(new BigDecimal("186.34")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPriceEmpty() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?symbols=")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(Collections.emptyList())))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
          .get("/iex/historicalPrice?symbol=twtr")
          .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("51.81")))
        .andExpect(jsonPath("$[0].high").value(new BigDecimal("53.08")))
        .andExpect(jsonPath("$[0].low").value(new BigDecimal("51.62")))
        .andExpect(jsonPath("$[0].open").value(new BigDecimal("53.01")))
        .andExpect(jsonPath("$[0].symbol", is("TWTR")))
        .andExpect(jsonPath("$[0].volume").value(17362305))
        .andExpect(jsonPath("$[0].date", is("2021-05-10")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceSymbolDate() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrice?symbol=twtr&date=20210405")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("64.24")))
        .andExpect(jsonPath("$[0].high").value(new BigDecimal("64.34")))
        .andExpect(jsonPath("$[0].low").value(new BigDecimal("61.81")))
        .andExpect(jsonPath("$[0].open").value(new BigDecimal("64.14")))
        .andExpect(jsonPath("$[0].symbol", is("TWTR")))
        .andExpect(jsonPath("$[0].volume").value(15502130))
        .andExpect(jsonPath("$[0].date", is("2021-04-05")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceSymbolRange() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrice?symbol=twtr&range=3d")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("57.01")))
        .andExpect(jsonPath("$[0].high").value(new BigDecimal("58.67")))
        .andExpect(jsonPath("$[0].low").value(new BigDecimal("55.83")))
        .andExpect(jsonPath("$[0].open").value(new BigDecimal("56.96")))
        .andExpect(jsonPath("$[0].symbol", is("TWTR")))
        .andExpect(jsonPath("$[0].volume").value(21985673))
        .andExpect(jsonPath("$[0].date", is("2021-06-03")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceSymbolRangeDate() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrice?symbol=twtr&range=1m&date=20210210")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("67.77")))
        .andExpect(jsonPath("$[0].high").value(new BigDecimal("69.25")))
        .andExpect(jsonPath("$[0].low").value(new BigDecimal("63.2")))
        .andExpect(jsonPath("$[0].open").value(new BigDecimal("65.8")))
        .andExpect(jsonPath("$[0].symbol", is("TWTR")))
        .andExpect(jsonPath("$[0].volume").value(73649183))
        .andExpect(jsonPath("$[0].date", is("2021-02-10")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceEmpty() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrice?symbol=")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(Collections.emptyList())))
        .andReturn();
  }
}
