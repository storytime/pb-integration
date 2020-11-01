package com.github.storytime.mapper;

import com.github.storytime.model.api.PbZenReconcile;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.common.ZenYnabAccountReconcileProxyObject;
import com.github.storytime.model.zen.AccountItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.service.ReconcileTableService.X;
import static java.lang.String.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class ReconcileCommonMapper {

    private static final Logger LOGGER = LogManager.getLogger(ReconcileCommonMapper.class);
    private final YnabCommonMapper ynabCommonMapper;

    @Autowired
    public ReconcileCommonMapper(final YnabCommonMapper ynabCommonMapper) {
        this.ynabCommonMapper = ynabCommonMapper;
    }


    public List<ZenYnabAccountReconcileProxyObject> mapInfoForAccountTable(final List<AccountItem> zenAccs,
                                                                           final List<YnabAccounts> ynabAccs,
                                                                           final List<PbAccountBalance> pbAccs) {
        return zenAccs
                .stream()
                .map(zenAcc -> ynabAccs.stream()
                        .filter(yA -> yA.getName().equalsIgnoreCase(zenAcc.getTitle()))
                        .collect(toUnmodifiableList())
                        .stream()
                        .map(yAcc -> this.mapToZenYnabPbAcc(pbAccs, zenAcc, zenAcc.getTitle(), yAcc))
                        .collect(toUnmodifiableList()))
                .collect(toUnmodifiableList())
                .stream()
                .flatMap(Collection::stream)
                .sorted(comparing(ZenYnabAccountReconcileProxyObject::getPbAmount))
                .collect(toUnmodifiableList());
    }


    public List<PbZenReconcile> mapInfoForAccountJson(final List<AccountItem> zenAccs,
                                                      final List<PbAccountBalance> pbAccs) {
        return zenAccs.stream().map(za -> pbAccs.stream()
                .filter(pa -> pa.getAccount().equals(za.getTitle()))
                .collect(toUnmodifiableList())
                .stream()
                .map(x -> mapToZenPbAcc(za, x)).collect(toUnmodifiableList()))
                .collect(toUnmodifiableList())
                .stream()
                .flatMap(Collection::stream)
                .collect(toUnmodifiableList());
    }

    private PbZenReconcile mapToZenPbAcc(final AccountItem za, final PbAccountBalance x) {
        final String accountName = x.getAccount();
        final BigDecimal bankBal = x.getBalance();
        final BigDecimal zenBal = BigDecimal.valueOf(za.getBalance());
        final var diff = bankBal.subtract(zenBal).setScale(CURRENCY_SCALE, HALF_DOWN).toString();
        return new PbZenReconcile(accountName, bankBal.toString(), zenBal.toString(), diff);
    }

    public ZenYnabAccountReconcileProxyObject mapToZenYnabPbAcc(final List<PbAccountBalance> pbAccs,
                                                                final AccountItem zenAcc,
                                                                final String zenAccTitle,
                                                                final YnabAccounts yAcc) {
        var ynabBal = ynabCommonMapper.parseYnabBal(valueOf(yAcc.getBalance()));
        var zenBal = BigDecimal.valueOf(zenAcc.getBalance());
        var zenYnabDiff = zenBal.subtract(ynabBal).setScale(CURRENCY_SCALE, HALF_DOWN);
        var status = zenYnabDiff.longValue() == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;

        final Optional<PbAccountBalance> pbAcc = pbAccs.stream()
                .filter(pbAccountBalance -> pbAccountBalance.getAccount().equalsIgnoreCase(zenAccTitle))
                .findFirst();

        if (pbAcc.isPresent()) {
            final BigDecimal pbBal = pbAcc.get().getBalance();
            var zenPbDiff = pbBal.subtract(zenBal).setScale(CURRENCY_SCALE, HALF_DOWN);
            var fullStatus = (zenYnabDiff.longValue() + zenPbDiff.longValue()) == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;
            return new ZenYnabAccountReconcileProxyObject(zenAccTitle, zenBal.toString(), ynabBal.toString(), pbBal.toString(), zenPbDiff.toString(), zenYnabDiff.toString(), fullStatus);
        } else {
            return new ZenYnabAccountReconcileProxyObject(zenAccTitle, zenBal.toString(), ynabBal.toString(), X, X, zenYnabDiff.toString(), status);
        }
    }
}
