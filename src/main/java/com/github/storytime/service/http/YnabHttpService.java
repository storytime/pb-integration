//package com.github.storytime.service.http;
//
//import com.github.storytime.config.CustomConfig;
//import com.github.storytime.model.api.ms.AppUser;
//import com.github.storytime.model.ynab.account.YnabAccountResponse;
//import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
//import com.github.storytime.model.ynab.category.YnabCategoryResponse;
//import com.github.storytime.model.ynab.transaction.from.TransactionsFormYnab;
//import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Optional;
//
//import static com.github.storytime.STUtils.createSt;
//import static com.github.storytime.STUtils.getTime;
//import static com.github.storytime.config.props.Constants.*;
//import static com.github.storytime.other.Utils.createHeader;
//import static java.util.Optional.empty;
//import static java.util.Optional.ofNullable;
//import static org.apache.logging.log4j.LogManager.getLogger;
//import static org.springframework.http.HttpMethod.GET;
//
//@Service
//public class YnabHttpService {
//
//    private static final Logger LOGGER = getLogger(YnabHttpService.class);
//    private final RestTemplate restTemplate;
//    private final CustomConfig customConfig;
//
//    @Autowired
//    public YnabHttpService(final RestTemplate restTemplate,
//                           final CustomConfig customConfig) {
//        this.restTemplate = restTemplate;
//        this.customConfig = customConfig;
//    }
//
//    public Optional<YnabBudgetResponse> getBudget(final AppUser user) {
//        final var st = createSt();
//        try {
//            final var httpEntity = new HttpEntity<>(createHeader(user.getYnabAuthToken()));
//            final var body =
//                    ofNullable(restTemplate.exchange(customConfig.getYnabUrl(), GET, httpEntity, YnabBudgetResponse.class).getBody());
//            LOGGER.debug("Ynab budgets for user: [{}], fetched time: [{}] - finish", user.getId(), getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Cannot fetch ynab budgets, for user: [{}], time: [{}], error: [{}] - error", user.getId(), getTime(st), e.getMessage(), e);
//            return empty();
//        }
//    }
//
//    public Optional<YnabCategoryResponse> getCategories(final AppUser appUser,
//                                                        final String budgetId) {
//        final var st = createSt();
//        try {
//            final var ynabToken = appUser.getYnabAuthToken();
//            final var httpEntity = new HttpEntity<>(createHeader(ynabToken));
//            final var url = customConfig.getYnabUrl().concat(SLASH).concat(budgetId).concat(YNAB_CATEGORIES);
//            final var body = ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabCategoryResponse.class).getBody());
//            LOGGER.debug("Ynab categories for user: [{}], fetched time: [{}] - finish", appUser.getId(), getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Cannot fetch ynab categories for user: [{}], time: [{}]. error: [{}] - error", appUser.getId(), getTime(st), e.getMessage());
//            return empty();
//        }
//    }
//
//    public Optional<TransactionsFormYnab> getYnabTransactions(final AppUser appUser,
//                                                              final String budgetId) {
//        final var st = createSt();
//        try {
//            final var ynabToken = appUser.getYnabAuthToken();
//            final var httpEntity = new HttpEntity<>(createHeader(ynabToken));
//            final var url = customConfig.getYnabUrl().concat(SLASH).concat(budgetId).concat(YNAB_TRANSACTIONS);
//            final var body = ofNullable(restTemplate.exchange(url, GET, httpEntity, TransactionsFormYnab.class).getBody());
//            LOGGER.debug("Ynab budgets transactions for user: [{}] fetched, time: [{}] - finish", appUser.getId(), getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Cannot fetch ynab transactions, for user: [{}], time: [{}], error: [{}]", appUser.getId(), getTime(st), e.getMessage(), e);
//            return empty();
//        }
//    }
//
//    public Optional<YnabAccountResponse> getAccounts(final AppUser appUser,
//                                                     final String id) {
//        final var st = createSt();
//        try {
//            final var ynabToken = appUser.getYnabAuthToken();
//            final var httpEntity = new HttpEntity<>(createHeader(ynabToken));
//            final var url = customConfig.getYnabUrl().concat(SLASH).concat(id).concat(YNAB_ACCOUNTS);
//            final var body = ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabAccountResponse.class).getBody());
//            LOGGER.debug("Ynab accounts for user: [{}], fetched time: [{}] - finish", appUser.getId(), getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Cannot fetch ynab accounts, for user: [{}], time [{}], error: [{}] - error", appUser.getId(), getTime(st), e.getMessage(), e);
//            return empty();
//        }
//    }
//
//    public Optional<String> pushToYnab(final AppUser u,
//                                       final String id,
//                                       final YnabTransactionsRequest request) {
//        final var st = createSt();
//        final int transactionCount = request.getTransactions().size();
//        try {
//            final var diffObject = new HttpEntity<>(request, createHeader(u.getYnabAuthToken()));
//            final var url = customConfig.getYnabUrl().concat(SLASH).concat(id).concat(YNAB_TRANSACTIONS);
//            final var body = ofNullable(restTemplate.postForEntity(url, diffObject, String.class).getBody());
//            LOGGER.info("Finish! [{}] were pushed to ynab, for user: [{}], time: [{}] - final", request.getTransactions().size(), u.getId(), getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Cannot push transactions: [{}] to ynab, for user: [{}], time: [{}], error: [{}] - error", transactionCount, u.getId(), getTime(st), e.getMessage(), e);
//            return empty();
//        }
//    }
//}
