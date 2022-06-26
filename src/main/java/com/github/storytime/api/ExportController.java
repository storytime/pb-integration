package com.github.storytime.api;

import com.github.storytime.service.export.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.CacheNames.*;
import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ExportController {

    private final ExportService exportService;

    @Autowired
    public ExportController(final ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/out/monthly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = OUT_DATA_BY_MONTH, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getOutDataByMonth(@PathVariable("userId") String userId) {
        return exportService.getOutMonthlyData(userId);
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/in/monthly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = IN_DATA_BY_MONTH, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getInDataByMonth(@PathVariable("userId") String userId) {
        return exportService.getInMonthlyData(userId);
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/out/yearly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = OUT_DATA_BY_YEAR, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getOutDataByYear(@PathVariable("userId") String userId) {
        return exportService.getOutYearlyData(userId);
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/in/yearly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = IN_DATA_BY_YEAR, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getInDataByYear(@PathVariable("userId") String userId) {
        return exportService.getInYearlyData(userId);
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/out/quarterly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = OUT_DATA_BY_QUARTER, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getOutDataByQuarter(@PathVariable("userId") String userId) {
        return exportService.getOutQuarterlyData(userId);
    }

    @GetMapping(value = API_PREFIX + "/export/{userId}/in/quarterly", produces = APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = IN_DATA_BY_QUARTER, key = "#userId")
    public CompletableFuture<List<Map<String, String>>> getInDataByQuarter(@PathVariable("userId") String userId) {
        return exportService.getInQuarterData(userId);
    }
}