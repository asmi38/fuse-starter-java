package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * A Feign Declarative REST Client to access endpoints from the Cloud IEX API to get market
 * data. See https://iexcloud.io/docs/api/
 */
@FeignClient(name = "IEXCloud", url = "${spring.rest.iexCloudPath}")
public interface IexCloudClient {

  /**
   * Get the historical price data for each symbol passed in for each date and range passed in. See
   * https://iexcloud.io/docs/api/#historical-prices.
   *
   * @param symbol stock symbols to historical price for.
   * @param date date to get the historical data for in YYYYMMDD format.
   * @return a list containing a IexHistoricalPrice for the specified date.
   */
  @GetMapping("/stock/{symbol}/chart/date/{date}?chartByDay=true&token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesDate(
      @PathVariable(value = "symbol") String symbol,
      @PathVariable(value = "date") String date);

  /**
   * Get the historical price data for each symbol passed in for each date and range passed in. See
   * https://iexcloud.io/docs/api/#historical-prices.
   *
   * @param symbol stock symbols to historical prices for.
   * @param range range of dates from date to get historical prices for.
   * @return a list of IexHistoricalPrice for the symbol for the specified range.
   */
  @GetMapping("/stock/{symbol}/chart/{range}?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesRange(
      @PathVariable(value = "symbol") String symbol,
      @PathVariable(value = "range") String range);
}
