//package com.github.storytime.service.async;
//
//import com.github.storytime.model.api.ms.AppUser;
//import com.github.storytime.model.ynab.account.YnabAccountResponse;
//import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
//import com.github.storytime.model.ynab.category.YnabCategoryResponse;
//import com.github.storytime.model.ynab.transaction.from.TransactionsFormYnab;
//import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
//import com.github.storytime.service.http.YnabHttpService;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//import static java.util.concurrent.CompletableFuture.supplyAsync;
//
//@Deprecated
//@Service
//public class YnabAsyncService {
//
//    private static final Logger LOGGER = LogManager.getLogger(YnabAsyncService.class);
//
//    private final Executor pool;
//    private final YnabHttpService ynabHttpService;
//
//    @Autowired
//    public YnabAsyncService(final YnabHttpService ynabHttpService, final Executor cfThreadPool) {
//        this.ynabHttpService = ynabHttpService;
//        this.pool = cfThreadPool;
//    }
//
//    public CompletableFuture<Optional<YnabCategoryResponse>> getYnabCategories(final AppUser user,
//                                                                               final String budgetToSync) {
//        LOGGER.debug("Fetching Ynab categories, for user: [{}], budget: [{}] - stared", user.getId(), budgetToSync);
//        return supplyAsync(() -> ynabHttpService.getCategories(user, budgetToSync), pool);
//    }
//
//    public CompletableFuture<Optional<YnabAccountResponse>> getYnabAccounts(final AppUser appUser,
//                                                                            final String budget) {
//        LOGGER.debug("Fetching Ynab accounts, for user: [{}], budget: [{}] - stared", appUser.getId(), budget);
//        return supplyAsync(() -> ynabHttpService.getAccounts(appUser, budget), pool);
//    }
//
//    public CompletableFuture<Optional<TransactionsFormYnab>> getYnabTransactions(final AppUser appUser,
//                                                                                 final String budget) {
//        LOGGER.debug("Fetching Ynab transactions, for user: [{}], budget: [{}] - started", appUser.getId(), budget);
//        return supplyAsync(() -> ynabHttpService.getYnabTransactions(appUser, budget), pool);
//    }
//
//    public CompletableFuture<Optional<YnabBudgetResponse>> getYnabBudget(final AppUser appUser) {
//        LOGGER.debug("Fetching Ynab budgets, for user: [{}] - started", appUser.getId());
//        return supplyAsync(() -> ynabHttpService.getBudget(appUser), pool);
//    }
//
//    public CompletableFuture<Optional<String>> pushToYnab(final AppUser appUser,
//                                                          final String id,
//                                                          final YnabTransactionsRequest request) {
//        LOGGER.debug("Pushing  zen tr to Ynab, for user: [{}], budget: [{}], tr count [{}]", appUser.getId(), id, request.getTransactions().size());
//        return supplyAsync(() -> ynabHttpService.pushToYnab(appUser, id, request), pool);
//    }
//}
