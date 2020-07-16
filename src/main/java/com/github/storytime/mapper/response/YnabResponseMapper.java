package com.github.storytime.mapper.response;

import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class YnabResponseMapper {

    public List<YnabCategories> mapYnabCategoriesFromResponse(Optional<YnabCategoryResponse> yMaybeCat) {
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
}
