package com.github.storytime.service;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
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
public class YnabService {

    private static final Logger LOGGER = getLogger(YnabService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;


    @Autowired
    public YnabService(final RestTemplate restTemplate,
                       final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }


    public Optional<YnabBudgetResponse> getBudget(final AppUser user) {
        try {
            final HttpEntity httpEntity = new HttpEntity<>(createHeader(user.getYnabAuthToken()));
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<YnabBudgetResponse> body = ofNullable(restTemplate.exchange(customConfig.getYnabUrl(), GET, httpEntity, YnabBudgetResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab budgets was fetched time:[{}]", st.getTotalTimeMillis());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot ynab budgets, error:[{}]", e.getMessage());
            return empty();
        }
    }


    public Optional<YnabCategoryResponse> getCategories(AppUser appUser, String id) {
        try {
            final String ynabToken = appUser.getYnabAuthToken();
            final HttpEntity httpEntity = new HttpEntity<>(createHeader(ynabToken));
            final StopWatch st = new StopWatch();
            st.start();
            final String url = customConfig.getYnabUrl() + "/" + id + "/categories";
            final Optional<YnabCategoryResponse> body = ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabCategoryResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab budgets categories fetched time:[{}]", st.getTotalTimeMillis());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot ynab categories, error:[{}]", e.getMessage());
            return empty();
        }
    }

    public Optional<YnabAccountResponse> getAccounts(AppUser appUser, String id) {
        try {
            final String ynabToken = appUser.getYnabAuthToken();
            final HttpEntity httpEntity = new HttpEntity<>(createHeader(ynabToken));
            final StopWatch st = new StopWatch();
            st.start();
            final String url = customConfig.getYnabUrl() + "/" + id + "/accounts";
            final Optional<YnabAccountResponse> body = ofNullable(restTemplate.exchange(url, GET, httpEntity, YnabAccountResponse.class).getBody());
            st.stop();
            LOGGER.debug("Ynab accounts was fetched time:[{}]", st.getTotalTimeMillis());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot ynab accounts, error:[{}]", e.getMessage());
            return empty();
        }
    }

    public Optional<String> pushToYnab(final AppUser u, String id, final YnabTransactionsRequest request) {
        try {
            final HttpEntity<YnabTransactionsRequest> diffObject = new HttpEntity<>(request, createHeader(u.getYnabAuthToken()));
            final String url = customConfig.getYnabUrl() + "/" + id + "/transactions";
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<String> body = ofNullable(restTemplate.postForEntity(url, diffObject, String.class).getBody());
            st.stop();
            LOGGER.info("Finish! {} were pushed to ynam", request.getTransactions().size());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot push Diff to ZEN request:[{}]", e.getMessage());
            return empty();
        }
    }

}
