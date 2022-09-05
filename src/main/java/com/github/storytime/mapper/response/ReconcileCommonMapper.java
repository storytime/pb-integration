package com.github.storytime.mapper.response;

import com.github.storytime.model.api.PbZenReconcile;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.zen.AccountItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static com.github.storytime.config.props.Constants.CURRENCY_SCALE;
import static java.math.RoundingMode.HALF_DOWN;

@Component
public class ReconcileCommonMapper {


    public List<PbZenReconcile> mapInfoForAccountJson(final List<AccountItem> zenAccs,
                                                      final List<PbAccountBalance> pbAccs) {
        return zenAccs.stream().map(za -> pbAccs.stream()
                        .filter(pa -> pa.getAccount().equals(za.getTitle())).toList()
                        .stream()
                        .map(x -> mapToZenPbAcc(za, x)).toList()).toList()
                .stream()
                .flatMap(Collection::stream).toList();
    }

    private PbZenReconcile mapToZenPbAcc(final AccountItem za, final PbAccountBalance x) {
        final String accountName = x.getAccount();
        final BigDecimal bankBal = x.getBalance();
        final BigDecimal zenBal = BigDecimal.valueOf(za.getBalance());
        final var diff = bankBal.subtract(zenBal).setScale(CURRENCY_SCALE, HALF_DOWN).toString();
        return new PbZenReconcile(accountName, bankBal.toString(), zenBal.toString(), diff);
    }
}
