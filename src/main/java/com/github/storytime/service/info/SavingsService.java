package com.github.storytime.service.info;

import com.github.storytime.mapper.SavingsInfoMapper;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.api.SavingsInfoAsJson;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.service.SavingsInfoFormatter;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.github.storytime.config.props.Constants.TOTAL;
import static com.github.storytime.config.props.Constants.UAH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
public class SavingsService {


    private static final Logger LOGGER = getLogger(SavingsService.class);

    private final UserService userService;
    private final ZenAsyncService zenAsyncService;
    private final SavingsInfoMapper savingsInfoMapper;
    private final ZenResponseMapper zenResponseMapper;
    private final SavingsInfoFormatter savingsInfoFormatter;

    @Autowired
    public SavingsService(final UserService userService,
                          final SavingsInfoMapper savingsInfoMapper,
                          final ZenResponseMapper zenResponseMapper,
                          final SavingsInfoFormatter savingsInfoFormatter,
                          final ZenAsyncService zenAsyncService) {
        this.userService = userService;
        this.zenAsyncService = zenAsyncService;
        this.zenResponseMapper = zenResponseMapper;
        this.savingsInfoMapper = savingsInfoMapper;
        this.savingsInfoFormatter = savingsInfoFormatter;
    }

    public String getAllSavingsAsTable(final long userId) {
        try {
            LOGGER.debug("Calling get savings info as table for user: [{}]", userId);
            return userService.findUserById(userId)
                    .map(appUser -> {
                        final List<SavingsInfo> savingsInfo = getUserSavings(appUser);
                        final BigDecimal totalAmountInUah = savingsInfoMapper.getTotalInUah(savingsInfo);
                        final List<SavingsInfo> savingsInfoNew = savingsInfoMapper.updateSavingsInfoList(totalAmountInUah, savingsInfo);
                        final var niceSavingsText = savingsInfoMapper.getNiceSavings(savingsInfoNew);
                        final var niceTotalInUah = savingsInfoFormatter.formatAmount(totalAmountInUah);

                        LOGGER.debug("Finish get savings info as table for user: [{}]", userId);
                        return niceSavingsText.append(TOTAL).append(niceTotalInUah).append(SPACE).append(UAH).toString();
                    })
                    .orElse(EMPTY);
        } catch (Exception e) {
            //todo return server error
            LOGGER.error("Cannot collect saving info as table for user: [{}] request:[{}]", userId, e.getCause());
            return EMPTY;
        }
    }

    public ResponseEntity<SavingsInfoAsJson> getAllSavingsJson(final long userId) {
        try {
            LOGGER.debug("Calling get savings info as JSON for user: [{}]", userId);
            return userService.findUserById(userId)
                    .map(appUser -> {
                        final List<SavingsInfo> savingsInfo = getUserSavings(appUser);
                        final BigDecimal totalAmountInUah = savingsInfoMapper.getTotalInUah(savingsInfo);
                        final List<SavingsInfo> savingsInfoNew = savingsInfoMapper.updateSavingsInfoList(totalAmountInUah, savingsInfo);
                        final var niceTotalInUah = savingsInfoFormatter.formatAmount(totalAmountInUah);
                        final var response = new SavingsInfoAsJson().setSavings(savingsInfoNew).setTotal(niceTotalInUah);

                        LOGGER.debug("Finish get savings info as JSON for user: [{}]", userId);
                        return new ResponseEntity<>(response, OK);
                    })
                    .orElse(new ResponseEntity<>(NO_CONTENT));
        } catch (Exception e) {
            //todo return server error
            LOGGER.error("Cannot collect saving info as JSON for user: [{}] request:[{}]", userId, e.getCause());
            return new ResponseEntity<>(NO_CONTENT);
        }
    }


    private List<SavingsInfo> getUserSavings(final AppUser appUser) {
        final var zenDiff = zenAsyncService.zenDiffByUserForSavings(appUser)
                .orElseThrow(() -> new RuntimeException("Cannot get zen diff to map"));
        final var savingsAccounts = zenResponseMapper.getSavingsAccounts(zenDiff);
        return savingsInfoMapper.getUserSavings(savingsAccounts, zenDiff);
    }
}