package org.galatea.starter.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DateChecker {
  static Set<String> holidayDates = Stream.of("2021-01-01",
      "2021-01-18",
      "2021-02-15",
      "2021-04-2",
      "2021-05-31",
      "2021-07-04",
      "2021-09-06",
      "2021-11-25",
      "2021-12-24",
      "2020-01-01",
      "2020-01-20",
      "2020-02-17",
      "2020-04-10",
      "2020-05-25",
      "2020-07-04",
      "2020-09-07",
      "2020-11-26",
      "2020-12-25",
      "2019-01-01",
      "2019-01-21",
      "2019-02-18",
      "2019-03-19",
      "2019-04-27",
      "2019-07-04",
      "2019-09-02",
      "2019-11-28",
      "2019-12-25",
      "2018-01-01",
      "2018-01-15",
      "2018-02-19",
      "2018-03-30",
      "2018-04-28",
      "2018-07-04",
      "2018-09-03",
      "2018-11-22",
      "2018-12-25",
      "2017-01-02",
      "2017-01-16",
      "2017-02-20",
      "2017-03-14",
      "2017-04-29",
      "2017-07-04",
      "2017-09-04",
      "2017-11-23",
      "2017-12-25",
      "2016-01-01",
      "2016-01-18",
      "2016-02-15",
      "2016-03-25",
      "2016-04-30",
      "2016-07-04",
      "2016-09-05",
      "2016-11-24",
      "2016-12-26",
      "2015-01-01",
      "2015-01-19",
      "2015-02-16",
      "2015-03-03",
      "2015-04-25",
      "2015-07-03",
      "2015-09-07",
      "2015-11-26",
      "2015-12-25")
      .collect(Collectors.toUnmodifiableSet());

  /**
   * Returns true if the day is not a exchange holiday or a weekend
   *
   */
  public static boolean isTradingDay(final LocalDate day) {
    return !(isExchangeHoliday(day) || isWeekend(day));
  }


  /**
   * Checks the date to see if it is a Weekend, returns true if it is, false otherwise.
   *
   */
  public static boolean isWeekend(final LocalDate day) {
    DayOfWeek dow = day.getDayOfWeek();
    return dow.getValue() == 6 || dow.getValue() == 7;
  }

  /**
   * Checks if the date provided is in the set of trading holidays.
   *
   */
  public static boolean isExchangeHoliday(final LocalDate day) {
    return holidayDates.contains(day.toString());
  }
}
