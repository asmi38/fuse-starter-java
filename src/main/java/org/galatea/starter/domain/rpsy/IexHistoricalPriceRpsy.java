package org.galatea.starter.domain.rpsy;

import java.time.LocalDate;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceEntity;
import org.galatea.starter.domain.IexHistoricalPriceId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IexHistoricalPriceRpsy extends CrudRepository<
    IexHistoricalPriceEntity,
    IexHistoricalPriceId> {

  List<IexHistoricalPriceEntity> findBySymbolIgnoreCaseAndDateBetween(String symbol, LocalDate endDate, LocalDate startDate);
}
