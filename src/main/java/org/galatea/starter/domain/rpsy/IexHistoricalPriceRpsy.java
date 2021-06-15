package org.galatea.starter.domain.rpsy;

import org.galatea.starter.domain.IexHistoricalPriceEntity;
import org.galatea.starter.domain.IexHistoricalPriceId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IexHistoricalPriceRpsy extends CrudRepository<
    IexHistoricalPriceEntity,
    IexHistoricalPriceId> {
}
