package com.github.storytime.function;

import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.utils.DateService;
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
import static java.util.stream.Collectors.toUnmodifiableSet;

@Component
public class PbSyncLambdaHolder {

    public UnaryOperator<List<List<Statement>>> getRegularSyncMapper(
            final Function<Set<Statement>, Predicate<Statement>> filterFk,
            final Set<Statement> alreadyPushedToZen) {

        return pbTrLists -> pbTrLists.stream()
                .map(pbTrList -> flatAndFilterTransactions(filterFk, alreadyPushedToZen, pbTrList))
                .filter(not(List::isEmpty))
                .collect(toUnmodifiableList());
    }


    private List<Statement> flatAndFilterTransactions(final Function<Set<Statement>, Predicate<Statement>> filterFk,
                                                      final Set<Statement> alreadyPushedToZen,
                                                      final List<Statement> pbTrList) {
        return pbTrList.stream().filter(filterFk.apply(alreadyPushedToZen)).collect(toUnmodifiableList());
    }

    public Function<Set<Statement>, Predicate<Statement>> ifWasMapped() {
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
    public BiConsumer<List<List<Statement>>, List<MerchantInfo>> onRegularSyncSuccess(final Set<Statement> alreadyMappedPbZenTransaction,
                                                                                      final MerchantService merchantService) {
        return (pushedByNotCached, merchantsDbList) -> {
            merchantService.saveAll(merchantsDbList);
            alreadyMappedPbZenTransaction.addAll(pushedByNotCached.stream().flatMap(Collection::stream).collect(toUnmodifiableSet()));
        };
    }

}
