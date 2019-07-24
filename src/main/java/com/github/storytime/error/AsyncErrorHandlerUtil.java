package com.github.storytime.error;

import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;

public interface AsyncErrorHandlerUtil {

    Logger LOGGER = LogManager.getLogger(AsyncErrorHandlerUtil.class);

    static BiFunction<List<Statement>, Throwable, List<Statement>> getPbServiceAsyncHandler() {
        return (s, t) -> {
            if (t != null) {
                LOGGER.error("Async error, cannot continue with PB transactions ", t);
                return emptyList();
            }
            return s;
        };
    }

    static BiFunction<Void, Throwable, Void> getZenDiffUpdateHandler() {
        return (s, t) -> {
            if (t != null) {
                LOGGER.error("Async error, cannot continue zen diff update ", t);
                return null;
            }
            return s;
        };
    }


}
