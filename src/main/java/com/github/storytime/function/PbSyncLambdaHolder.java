package com.github.storytime.function;

import com.github.storytime.mapper.PbStatementsToDynamoDbMapper;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.async.StatementAsyncService;
import com.github.storytime.service.misc.DateService;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;

@Component
public class PbSyncLambdaHolder {

    public BiFunction<AppUser, PbMerchant, ZonedDateTime> getAwsStartDate(final DateService dateService) {
        return (user, merchant) -> dateService.millisAwsUserDate(merchant.getSyncStartDate(), user);
    }

    public TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> getAwsEndDate() {
        return (appUser, merchantInfo, startDate) -> {
            final var period = ofMillis(merchantInfo.getSyncPeriod());
            final var now = now().withZoneSameInstant(of(appUser.getTimeZone()));
            return between(startDate, now).toMillis() < merchantInfo.getSyncPeriod() ? now : startDate.plus(period);
        };
    }

    public BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onAwsDbRegularSyncSuccess(final StatementAsyncService statementAsyncService) {

        return (pushedByNotCached, userId) -> statementAsyncService.getAllStatementsByUser(userId)
                .thenCompose(dfStatements -> {
                            final Set<String> pushedByNotCachedMapped = pushedByNotCached
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .collect(toUnmodifiableSet())
                                    .stream()
                                    .map(PbStatementsToDynamoDbMapper::generateUniqString)
                                    .collect(toUnmodifiableSet());

                            final Set<String> allNewStatements = concat(pushedByNotCachedMapped.stream(), dfStatements.getAlreadyPushed()
                                    .stream())
                                    .collect(toUnmodifiableSet());

                            return statementAsyncService.saveAll(dfStatements.setAlreadyPushed(allNewStatements), userId);
                        }
                );
    }
}
