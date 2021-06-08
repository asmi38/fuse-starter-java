package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Feign Declarative REST Client to access endpoints from the Cloud IEX API to get market
 * data. See https://iexcloud.io/docs/api/
 */
@FeignClient(name = "IEXCloud", url = "${spring.rest.iexCloudPath}")
public interface IexCloudClient {

  /**
   * Get the historical price data for the symbol passed in.
   * See https://iexcloud.io/docs/api/#historical-prices.
   *
   * @param symbol stock symbols to historical prices for.
   * @return a list of daily IexHistoricalPrices for the past month for the symbol specified.
   */
  @GetMapping("/stock/{symbol}/chart?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesSymbol(
      @PathVariable(value = "symbol") String symbol);

  /**
   * Get the historical price data for each symbol passed in for each date and range passed in.
   * See https://iexcloud.io/docs/api/#historical-prices.
   *
   * @param symbol stock symbols to historical price for.
   * @param date date to get the historical data for
   * @return a list containing a IexHistoricalPrice for the specified date.
   */
  @GetMapping("/stock/{symbol}/chart/date/{date}?chartByDate=true&token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesDate(
      @PathVariable(value = "symbol") String symbol,
      @PathVariable(value = "date") String date);

  /**
   * Get the historical price data for each symbol passed in for each date and range passed in.
   * See https://iexcloud.io/docs/api/#historical-prices.
   *
   * @param symbol stock symbols to historical prices for.
   * @param range range of dates from date to get historical prices for.
   * @return a list of IexHistoricalPrice for the symbol for the specified range.
   */
  @GetMapping("/stock/{symbol}/chart/{range}?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesRange(
      @PathVariable(value = "symbol") String symbol,
      @PathVariable(value = "range") String range);

  /**
   * Get the historical price data for each symbol passed in for each date and range passed in.
   * See https://iexcloud.io/docs/api/#historical-prices.
   * Note: if date is specified it will nullify the range input and will only return
   * the price of the exact day.
   *
   * @param symbol stock symbols to historical prices for.
   * @param date date to begin getting historical prices for.
   * @param range range of dates from date to get historical prices for.
   * @return a list of IexHistoricalPrice for the specified date and symbol.
   */
  @GetMapping("/stock/{symbol}/chart/{range}/{date}?chartByDay=true&token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPrices(
      @PathVariable(value = "symbol") String symbol,
      @PathVariable(value = "range") String range,
      @PathVariable(value = "date") String date);
}
