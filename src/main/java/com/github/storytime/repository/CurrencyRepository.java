package com.github.storytime.repository;

import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public interface CurrencyRepository extends JpaRepository<CurrencyRates, Long> {

    Optional<CurrencyRates> findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(final CurrencySource cs,
                                                                                    final CurrencyType ct,
                                                                                    final Long date);

}
