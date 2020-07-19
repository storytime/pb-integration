package com.github.storytime.function;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

    /**
     * In case if push tr to zen, we need to update merchant time sync time and add handled tr to tmp storage
     */
    public BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onRegularSyncSuccess(final MerchantService ms,
                                                                                         final Set<ExpiredPbStatement> set) {
        return (pbSet, mList) -> {
            ms.saveAll(mList);
            set.addAll(pbSet);
        };
    }

    public Consumer<List<MerchantInfo>> onEmptyFilter(final MerchantService ms) {
        return ms::saveAll;
    }
}
