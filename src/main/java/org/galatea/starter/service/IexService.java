package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;
  @NonNull
  private IexCloudClient iexCloudClient;

  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols();
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]));
    }
  }

  /**
   * Get the historical price for a specified Symbol for a certain range starting at date.
   * @param symbol the symbol to get historical prices for.
   * @param date the date to begin getting historical prices at.
   * @param range the range to get historical prices at.
   * @return a list of historical prices for the specified symbol and time frame.
   */
  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {
    if (date == null && range == null) {
      return iexCloudClient.getHistoricalPricesSymbol(symbol);
    } else if (range == null) {
      return iexCloudClient.getHistoricalPricesDate(symbol, date);
    } else if (date == null) {
      return iexCloudClient.getHistoricalPricesRange(symbol, range);
    } else {
      return iexCloudClient.getHistoricalPrices(symbol, range, date);
    }
  }
}
