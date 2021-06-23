package org.galatea.starter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
@Builder
@IdClass(IexHistoricalPriceId.class)
@Entity
@Table(name = "HISTORICAL_PRICE")
public class IexHistoricalPriceEntity {

  @Id
  @NonNull
  private String symbol;

  @Id
  @NonNull
  private LocalDate date;

  @NonNull
  private BigDecimal close;

  @NonNull
  private BigDecimal high;

  @NonNull
  private BigDecimal low;

  @NonNull
  private BigDecimal open;

  @NonNull
  private Integer volume;

}