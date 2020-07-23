package com.github.storytime.function;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.DateService;
import com.github.storytime.service.access.MerchantService;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.*;

import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class PbSyncLambdaHolder {

    public Function<List<List<Statement>>, List<ExpiredPbStatement>> getRegularSyncMapper(
            final Function<Set<ExpiredPbStatement>, Predicate<ExpiredPbStatement>> function,
            final Set<ExpiredPbStatement> set) {
        return (pbTrList) -> pbTrList
                .stream()
                .flatMap(Collection::stream)
                .map(ExpiredPbStatement::new)
                .filter(function.apply(set))
                .collect(toUnmodifiableList());
    }

    public Function<Set<ExpiredPbStatement>, Predicate<ExpiredPbStatement>> getRegularSyncPredicate() {
        return s -> not(s::contains);
    }

    public BiFunction<AppUser, MerchantInfo, ZonedDateTime> getStartDate(final DateService dateService) {
        return (user, merchant) -> dateService.millisUserDate(merchant.getSyncStartDate(), user);
    }

    public TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> getEndDate() {
        return (appUser, merchantInfo, startDate) -> {
            final var period = ofMillis(merchantInfo.getSyncPeriod());
            final var now = now().withZoneSameInstant(of(appUser.getTimeZone()));
            return between(startDate, now).toMillis() < merchantInfo.getSyncPeriod() ? now : startDate.plus(period);
        };
    }

    /**
     * In case if push tr to zen, we need to update merchant time sync time and add handled tr to tmp storage
     */
    public BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onRegularSyncSuccess(final MerchantService merchantService,
                                                                                         final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction) {
        return (pushed, merchantsDbList) -> {
            merchantService.saveAll(merchantsDbList);
            alreadyMappedPbZenTransaction.addAll(pushed);
        };
    }

    public Consumer<List<MerchantInfo>> onEmptyFilter(final MerchantService ms) {
        return ms::saveAll;
    }
}
