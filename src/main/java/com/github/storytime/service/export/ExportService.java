package com.github.storytime.service.export;

import com.github.storytime.mapper.response.ExportMapper;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.export.ExportTransaction;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logExport;
import static com.github.storytime.mapper.response.ExportMapper.*;
import static java.util.Collections.emptyList;
import static java.util.Map.Entry.comparingByKey;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class ExportService {

    private static final Logger LOGGER = getLogger(ExportService.class);
    private final static Map<String, String> quarter = new TreeMap<>();

    private final UserService userService;
    private final ZenAsyncService zenAsyncService;
    private final ExportMapper exportMapper;

    private final Function<TransactionItem, ExportTransaction> outMonthlyDateMapperFk = t -> new ExportTransaction(t.getOutcome(), getCategory(t), getYear(t) + DATE_SEPARATOR + getMonth(t));
    private final Function<TransactionItem, ExportTransaction> inMonthlyDateMapperFk = t -> new ExportTransaction(t.getIncome(), getCategory(t), getYear(t) + DATE_SEPARATOR + getMonth(t)); //TODO move getOutcome//getIncome to function
    private final Function<TransactionItem, ExportTransaction> outYearlyDateMapperFk = t -> new ExportTransaction(t.getOutcome(), getCategory(t), YEAR + getYear(t));
    private final Function<TransactionItem, ExportTransaction> inYearlyDateMapperFk = t -> new ExportTransaction(t.getIncome(), getCategory(t), YEAR + getYear(t));
    private final Function<TransactionItem, ExportTransaction> outQuarterlyDateMapperFk = t -> new ExportTransaction(t.getOutcome(), getCategory(t), QUARTER + getYear(t) + DATE_SEPARATOR + quarter.get(getMonth(t)));
    private final Function<TransactionItem, ExportTransaction> inQuarterlyDateMapperFk = t -> new ExportTransaction(t.getIncome(), getCategory(t), QUARTER + getYear(t) + DATE_SEPARATOR + quarter.get(getMonth(t)));
    private final Predicate<TransactionItem> transactionOutSelectPredicate = t -> t.getIncome() == INITIAL_VALUE;
    private final Predicate<TransactionItem> transactionInSelectPredicate = t -> t.getOutcome() == INITIAL_VALUE;

    @Autowired
    public ExportService(final UserService userService,
                         final ExportMapper exportMapper,
                         final ZenAsyncService zenAsyncService) {
        this.userService = userService;
        this.zenAsyncService = zenAsyncService;
        this.exportMapper = exportMapper;

        quarter.put("01", "1");
        quarter.put("02", "1");
        quarter.put("03", "1");
        quarter.put("04", "2");
        quarter.put("05", "2");
        quarter.put("06", "2");
        quarter.put("07", "3");
        quarter.put("08", "3");
        quarter.put("09", "3");
        quarter.put("10", "4");
        quarter.put("11", "4");
        quarter.put("12", "4");
    }

    public CompletableFuture<List<Map<String, String>>> getOutMonthlyData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export out monthly user: [{}] - start", userId);

            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, outMonthlyDateMapperFk, transactionOutSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export out monthly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    public CompletableFuture<List<Map<String, String>>> getInMonthlyData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export in monthly user: [{}] - start", userId);

            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, inMonthlyDateMapperFk, transactionInSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export in monthly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    public CompletableFuture<List<Map<String, String>>> getOutYearlyData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export out yearly user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, outYearlyDateMapperFk, transactionOutSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export out yearly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    public CompletableFuture<List<Map<String, String>>> getInYearlyData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export in yearly user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, inYearlyDateMapperFk, transactionInSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export in yearly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    public CompletableFuture<List<Map<String, String>>> getInQuarterData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export in quarterly user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, inQuarterlyDateMapperFk, transactionInSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export in quarterly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    public CompletableFuture<List<Map<String, String>>> getOutQuarterlyData(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get export out quarterly user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(appUser -> getExportData(appUser, outQuarterlyDateMapperFk, transactionOutSelectPredicate))
                    .whenComplete((r, e) -> logExport(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect get export out quarterly user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTime(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }

    private CompletableFuture<List<Map<String, String>>> getExportData(final AppUser appUser,
                                                                       final Function<TransactionItem, ExportTransaction> transactionMapper,
                                                                       final Predicate<TransactionItem> transactionFilter) {
        return zenAsyncService
                .zenDiffByUserTagsAndTransaction(appUser, INITIAL_TIMESTAMP)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> exportMapper.mapTransaction(transactionMapper, transactionFilter, zenDiff))
                .thenApply(transactions -> transactions.stream()
                        .collect(groupingBy(ExportTransaction::category, toList()))
                        .entrySet()
                        .stream()
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, LinkedHashMap::new))
                        .entrySet()
                        .stream()
                        .sorted(comparingByKey())
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, LinkedHashMap::new)))
                .thenApply(exportMapper::mapExportData);
    }
}