package org.galatea.starter.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPriceEntity;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.rpsy.IexHistoricalPriceRpsy;
import org.galatea.starter.utils.DateChecker;
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
  @NonNull
  private IexHistoricalPriceRpsy historicalPriceRspy;

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
    List<IexHistoricalPrice> historicalPriceList;
    HashSet<LocalDate> tradingDates;
    String startDate = date == null
        ? getStartDate()
        : date;
    String endDate = date == null
        ? getEndDate(startDate, range)
        : LocalDate.parse(date).minusDays(1).toString();

    //Fetch a list  of prices between the startDate and endDate.
    log.info("Symbol: " + symbol + " startDate: " + startDate + " endDate: " + endDate);
    historicalPriceList = fetchHistoricalPricesDB(symbol, endDate, startDate);
    log.info("Historical Prices retrieved from database: " + historicalPriceList.toString());

    //Get a list of all the trading dates in the range provided.
    tradingDates = getTradingDates(startDate, endDate);
    log.info("Trading Dates for specified range are: " + tradingDates.toString());

    //Compare the two datasets to find the missing dates of data.
    HashSet<LocalDate> missingDates = getMissingDates(historicalPriceList, tradingDates);
    log.info("Missing dates are: " + missingDates.toString());

    // If missingDates is empty, there are no missing dates, return the list from the database.
    if(missingDates.isEmpty()){
      log.info("There are no missing dates");
      return historicalPriceList;
    } else {
      log.info("There are missing dates, beginning formatting of IEX request");
      int missingPriceAmount = missingDates.size();
      LocalDate furthestMissingPrice = getFurthestMissingDate(missingDates);
      return calcBestQuery(symbol, missingPriceAmount, furthestMissingPrice, startDate);
    }
  }

  /**
   * Get the start date for the query based on the current local time, minus one day (because todays
   * trading may not have finished).
   * @return a String of the local date in the format YYYY-MM-DD.
   */
  public String getStartDate() {
    //Using LocalDate.now() here can cause problems down the line
    return LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);
  }

  /**
   * Calculates the end date based on the startDate and the range provided. Will also handle
   * certain edge cases such as the default range, and other input types.
   * @param startDate the startDate for the query.
   * @param range the range that is required to find the end date.
   * @return a LocalDate for the end date that was calculated.
   */
  public String getEndDate(final String startDate, final String range) {
    //Adjust range for edge scenarios
    String adjustedRange = range;
    if (range == null) {
      adjustedRange = "1m";
    } else if (range.equals("ytd")) {
      adjustedRange = "1y";
    } else if (range.equals("max")) {
      adjustedRange = "5y";
    }

    String[] rangeSplit = adjustedRange.split("(?<=\\d)(?=\\D)");
    int dateVal = Integer.parseInt(rangeSplit[0]);
    String dateType = rangeSplit[1];

    String endDate;
    switch (dateType) {
      case "d":
        endDate = LocalDate.now().minusDays(dateVal + 1).format(DateTimeFormatter.ISO_DATE);
        break;
      case "m":
        endDate = LocalDate.now().minusMonths(dateVal).format(DateTimeFormatter.ISO_DATE);
        break;
      case "y":
        endDate = LocalDate.now().minusYears(dateVal).format(DateTimeFormatter.ISO_DATE);
        break;
      default:
        log.error("Error: invalid dateType: " + dateType);
        endDate = startDate;
        break;
    }
    return endDate;
  }

  /**
   * Attempt to fetch historical prices for the required range from the database.
   * @param symbol the symbol for the stock that is being queried for.
   * @param startDate the start date of the range that is required.
   * @param endDate the end date of the range that is required.
   * @return returns a List of all the prices that are in the database for the range selected.
   */
  public List<IexHistoricalPrice> fetchHistoricalPricesDB(
      final String symbol,
      final String startDate,
      final String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);

    List<IexHistoricalPriceEntity> dbEntityResult;
    dbEntityResult = historicalPriceRspy.findBySymbolIgnoreCaseAndDateBetween(symbol, start, end);

    List<IexHistoricalPrice> dbAdjustedResult = new ArrayList<IexHistoricalPrice>();
    for (IexHistoricalPriceEntity entity : dbEntityResult) {
      IexHistoricalPrice price = new IexHistoricalPrice(
          entity.getClose(),
          entity.getHigh(),
          entity.getLow(),
          entity.getOpen(),
          entity.getSymbol(),
          entity.getVolume(),
          entity.getDate().toString()
      );
      dbAdjustedResult.add(price);
    }
    return dbAdjustedResult;
  }

  /**
   * Get all the valid trading dates for the range provided. This will remove exchange holidays
   * and weekends as there will be no data on those days.
   * @param startDate is the first day in the range of the query requested.
   * @param endDate is the last day in the range of the query requested.
   * @return a HashSet of all the trading days between the two ranges.
   */
  public HashSet<LocalDate> getTradingDates(final String startDate, final String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);
    HashSet<LocalDate> allDates = new HashSet<>();
    log.info("Getting trading dates for specified range");
    while (start.isAfter(end)) {
      log.info("Checking if trading day: " + start);
      if(DateChecker.isTradingDay(start)) {
        log.info("Is a trading day: " + start);
        allDates.add(start);
      }
      start = start.minusDays(1);
    }
    return allDates;
  }

  /**
   * Compares the dates stored in the database to the dates that are needed. And calculates the
   * difference between these two.
   * @param pricesInDB a List of all the IexHistoricalPrice objects in the database within the
   * specified date range.
   * @param datesNeeded a HashSet of LocalDate of all the dates that are required by  the query.
   * @return a HashSet of LocalDates containing the dates that are not in the database but are
   * required for the query.
   */
  public HashSet<LocalDate> getMissingDates(
      final List<IexHistoricalPrice> pricesInDB,
      HashSet<LocalDate> datesNeeded) {
    for ( IexHistoricalPrice pricePoint : pricesInDB) {
      LocalDate date = LocalDate.parse(pricePoint.getDate());
      datesNeeded.remove(date);
    }
    return datesNeeded;
  }

  /**
   * Finds the furthest missing date from the current day.
   * @param missingDates is a set of dates that are required for the query, that are trading days,
   * that are not available in the database.
   * @return returns the LocalDate of the date furthest back in time
   */
  public LocalDate getFurthestMissingDate(final HashSet<LocalDate> missingDates) {
    LocalDate furthest = null;
    for ( LocalDate date : missingDates ) {
      if(furthest == null || date.isBefore(furthest)){
        furthest = date;
      }
    }
    return furthest;
  }

  /**
   * Calculates the best query that will minimise the amount of repeat data fetched from the IEX api.
   * Note: this method can be further optimised if the cost weights of different queries can be
   * identified.
   * @param symbol of the stock being queried for.
   * @param missingPriceAmount is the amount of missing price dates that exist.
   * @param furthestMissingDate is the furthest missing date.
   * @param startDate the start date of the range query.
   * @return returns a List of IexHistoricalPrices that have been fetched from the Iex API.
   */
  public List<IexHistoricalPrice> calcBestQuery(
      final String symbol,
      int missingPriceAmount,
      LocalDate furthestMissingDate,
      String startDate) {
    if(missingPriceAmount == 1) {
      log.info("Only one day is missing, creating date query");
      return fetchHistoricalPrices(symbol, null, furthestMissingDate.toString());
    } else {
      LocalDate start = LocalDate.parse(startDate);
      long daysBetween = ChronoUnit.DAYS.between(start, furthestMissingDate.minusDays(1));
      String bestRange = Math.abs(daysBetween) + "d";
      log.info("Range of dates are missing, range query is: " + bestRange);
      return fetchHistoricalPrices(symbol, bestRange, null);
    }
  }

  /**
   * Get the historical price for a specified Symbol for a certain range starting at date.
   * @param symbol the symbol to get historical prices for.
   * @param date the date to begin getting historical prices at.
   * @param range the range to get historical prices at.
   * @return a list of historical prices for the specified symbol and time frame.
   */
  public List<IexHistoricalPrice> fetchHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {
    List<IexHistoricalPrice> fetchedHistoricalPrices;
    if (date == null) {
      fetchedHistoricalPrices = iexCloudClient.getHistoricalPricesRange(symbol, range);
    } else {
      String formattedDate = date.replace("-", "");
      log.info("Formatted Date is: " + formattedDate);
      fetchedHistoricalPrices = iexCloudClient.getHistoricalPricesDate(symbol, formattedDate);
    }
    addHistoricalPricesToDB(fetchedHistoricalPrices);
    return fetchedHistoricalPrices;
  }

  /**
   * Convert each historical price to an entity and add to the database.
   * @param historicalPriceList a list of Historical Prices fetched from the IEX API
   */
  public void addHistoricalPricesToDB(final List<IexHistoricalPrice> historicalPriceList) {
    List<IexHistoricalPriceEntity> priceEntityList = new ArrayList<IexHistoricalPriceEntity>();
    for (IexHistoricalPrice price : historicalPriceList) {
      IexHistoricalPriceEntity entity = new IexHistoricalPriceEntity(
          price.getSymbol(),
          LocalDate.parse(price.getDate()),
          price.getClose(),
          price.getHigh(),
          price.getLow(),
          price.getOpen(),
          price.getVolume());
      priceEntityList.add(entity);
      log.info("Adding to database: " + entity.toString());
    }
    log.info("Saving all historical prices to database");
    historicalPriceRspy.saveAll(priceEntityList);
  }
}
