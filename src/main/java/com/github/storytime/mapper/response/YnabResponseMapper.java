package com.github.storytime.mapper.response;

import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.from.TransactionsFormYnab;
import com.github.storytime.model.ynab.transaction.from.TransactionsItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class YnabResponseMapper {

    public List<YnabCategories> mapYnabCategoriesFromResponse(final Optional<YnabCategoryResponse> yMaybeCat) {
        //collect YNAB tags
        return yMaybeCat
                .map(yCat -> ofNullable(yCat.getYnabCategoryData())
                        .map(data -> ofNullable(data.getCategoryGroups())
                                .orElse(emptyList()))
                        .stream()
                        .flatMap(categoryGroupsItems ->
                                ofNullable(categoryGroupsItems)
                                        .orElse(emptyList())
                                        .stream()
                                        .flatMap(categoryGroupsItem -> ofNullable(categoryGroupsItem.getCategories())
                                                .orElse(emptyList())
                                                .stream()))
                        .collect(toUnmodifiableList()))
                .orElse(emptyList());
    }


    public List<YnabAccounts> mapYnabAccountsFromResponse(final Optional<YnabAccountResponse> yMaybeAcc) {
        return yMaybeAcc
                .map(yAcc -> ofNullable(yAcc.getYnabAccountData().getAccounts()).orElse(emptyList()))
                .orElse(emptyList());
    }

    public List<YnabBudgets> getSameBudgets(final Set<String> budgetNames,
                                            final YnabBudgetResponse ynabBudgetResponse) {
        return ynabBudgetResponse
                .getYnabBudgetData()
                .getBudgets()
                .stream()
                .filter(budget -> budgetNames.contains(budget.getName()))
                .collect(toUnmodifiableList());
    }

    public Optional<YnabBudgets> mapBudgets(final String budgetToReconcile,
                                            final YnabBudgetResponse ynabBudgetResponse) {
        return ynabBudgetResponse
                .getYnabBudgetData()
                .getBudgets()
                .stream()
                .filter(budget -> budgetToReconcile.equalsIgnoreCase(budget.getName()))
                .collect(toUnmodifiableList())
                .stream()
                .findFirst();
    }

    public List<TransactionsItem> mapTransactionsFromResponse(final TransactionsFormYnab ynabBudgetResponse) {
        return ynabBudgetResponse
                .getData()
                .getTransactions()
                .stream()
                .filter(not(TransactionsItem::isDeleted))
                .collect(toUnmodifiableList());
    }

}
