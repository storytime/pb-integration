package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.config.props.Constants;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.from.TransactionsFormYnab;
import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.github.storytime.other.Utils.createHeader;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpMethod.GET;

@Service
public class YnabHttpService {

    private static final Logger LOGGER = getLogger(YnabHttpService.class);
    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public YnabHttpService(final RestTemplate restTemplate,
                           final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }

    public Optional<YnabBudgetResponse> getBudget(final AppUser user) {
        try {
            final HttpEntity<YnabBudgetResponse> httpEntity = new HttpEntity<>(createHeader(user.getYnabAuthToken()));
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<YnabBudgetResponse> body =
                    ofNullable(restTemplate.exchange(customConfig.getYnabUrl(), GET, httpEntity, YnabBudgetResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab budgets for user:[{}] were fetched time:[{}]", user.id, st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch ynab budgets, for user:[{}], error:[{}]", user.id, e.getMessage());
            return empty();
        }
    }

    public Optional<YnabCategoryResponse> getCategories(final AppUser appUser,
                                                        final String budgetId) {
        try {
            final String ynabToken = appUser.getYnabAuthToken();
            final HttpEntity<YnabCategoryResponse> httpEntity = new HttpEntity<>(createHeader(ynabToken));
            final StopWatch st = new StopWatch();
            st.start();
            final String url = customConfig.getYnabUrl() + Constants.SLASH + budgetId + "/categories";
            final Optional<YnabCategoryResponse> body =
                    ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabCategoryResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab categories for user:[{}] were fetched time:[{}]", appUser.id, st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch ynab categories, for user:[{}], error:[{}]", appUser.id, e.getMessage());
            return empty();
        }
    }

    public Optional<TransactionsFormYnab> getYnabTransactions(final AppUser appUser,
                                                              final String budgetId) {
        try {
            final String ynabToken = appUser.getYnabAuthToken();
            final HttpEntity<TransactionsFormYnab> httpEntity = new HttpEntity<>(createHeader(ynabToken));
            final StopWatch st = new StopWatch();
            st.start();
            final String url = customConfig.getYnabUrl() + Constants.SLASH + budgetId + "/transactions";
            final Optional<TransactionsFormYnab> body =
                    ofNullable(restTemplate.exchange(url, GET, httpEntity, TransactionsFormYnab.class).getBody());
            st.stop();
            LOGGER.debug("Ynab budgets transactions for user:[{}] were fetched time:[{}]", appUser.id, st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch ynab transactions, for user:[{}], error:[{}]", appUser.id, e.getMessage());
            return empty();
        }
    }

    public Optional<YnabAccountResponse> getAccounts(final AppUser appUser,
                                                     final String id) {
        try {
            final String ynabToken = appUser.getYnabAuthToken();
            final HttpEntity<YnabAccountResponse> httpEntity = new HttpEntity<>(createHeader(ynabToken));
            final StopWatch st = new StopWatch();
            st.start();
            final String url = customConfig.getYnabUrl() + Constants.SLASH + id + "/accounts";
            final Optional<YnabAccountResponse> body =
                    ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabAccountResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab accounts for user:[{}] were fetched time:[{}]", appUser.id, st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch ynab accounts, for user:[{}], error:[{}]", appUser.id, e.getMessage());
            return empty();
        }
    }

    public Optional<String> pushToYnab(final AppUser u,
                                       final String id,
                                       final YnabTransactionsRequest request) {
        final int transactionCount = request.getTransactions().size();
        try {
            final HttpEntity<YnabTransactionsRequest> diffObject = new HttpEntity<>(request, createHeader(u.getYnabAuthToken()));
            final String url = customConfig.getYnabUrl() + Constants.SLASH + id + "/transactions";
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<String> body = ofNullable(restTemplate.postForEntity(url, diffObject, String.class).getBody());
            st.stop();
            LOGGER.info("Finish! [{}] were pushed to ynab, for user:[{}]", request.getTransactions().size(), u.id);
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot push transactions:[{}] to ynab, for user:[{}], error:[{}]", transactionCount, u.id, e.getMessage());
            return empty();
        }
    }
}
