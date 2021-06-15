package org.galatea.starter.domain;

import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class IexHistoricalPriceId implements Serializable {

  private String symbol;
  private String date;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IexHistoricalPriceId iexHistoricalPriceId = (IexHistoricalPriceId) o;
    return symbol.equals(iexHistoricalPriceId.symbol) && date.equals(iexHistoricalPriceId.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol, date);
  }

}
