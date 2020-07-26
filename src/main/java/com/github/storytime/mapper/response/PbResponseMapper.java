package com.github.storytime.mapper.response;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.error.exception.PbSignatureException;
import com.github.storytime.model.pb.jaxb.account.response.Response;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import static com.github.storytime.config.props.Constants.*;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbResponseMapper {

    private static final Logger LOGGER = getLogger(PbResponseMapper.class);
    private static final String SIGNATURE = "signature";
    private final Unmarshaller jaxbStatementErrorUnmarshaller;
    private final Unmarshaller jaxbStatementOkUnmarshaller;
    private final Unmarshaller jaxbAccountOkUnmarshaller;
    private final CustomConfig customConfig;

    @Autowired
    public PbResponseMapper(final Unmarshaller jaxbStatementErrorUnmarshaller,
                            final Unmarshaller jaxbAccountOkUnmarshaller,
                            final CustomConfig customConfig,
                            final Unmarshaller jaxbStatementOkUnmarshaller) {
        this.jaxbStatementErrorUnmarshaller = jaxbStatementErrorUnmarshaller;
        this.jaxbAccountOkUnmarshaller = jaxbAccountOkUnmarshaller;
        this.customConfig = customConfig;
        this.jaxbStatementOkUnmarshaller = jaxbStatementOkUnmarshaller;
    }


    public List<Statement> mapStatementRequestBody(final ResponseEntity<String> responseEntity) {

        final var body = ofNullable(responseEntity.getBody()).orElse(EMPTY);
        if (body.contains(customConfig.getPbBankSignature())) {
            throw new PbSignatureException("Invalid signature");
        }

        try {
            if (!body.contains(SIGNATURE)) { // is error response, wrong ip etc
                final var error = (com.github.storytime.model.pb.jaxb.statement.response.error.Response) jaxbStatementErrorUnmarshaller.unmarshal(new StringReader(body));
                LOGGER.error("Bank return response with error:[{}]", error.getData().getError().getMessage());
                return emptyList();
            }

            final var parsedResponse = (com.github.storytime.model.pb.jaxb.statement.response.ok.Response) jaxbStatementOkUnmarshaller.unmarshal(new StringReader(body));
            return ofNullable(parsedResponse.getData())
                    .map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data::getInfo)
                    .map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info::getStatements)
                    .map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements::getStatement)
                    .orElse(emptyList());
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank response:[{}]", e.getMessage(), e);
            return emptyList();
        }
    }

    public BigDecimal mapAccountRequestBody(final ResponseEntity<String> responseEntity) {
        final var body = ofNullable(responseEntity.getBody()).orElse(EMPTY);
        try {
            final Response parsedResponse = (Response) jaxbAccountOkUnmarshaller.unmarshal(new StringReader(body));
            final BigDecimal bigDecimal = ofNullable(parsedResponse.getData())
                    .map(Response.Data::getInfo)
                    .map(Response.Data.Info::getCardbalance)
                    .map(Response.Data.Info.Cardbalance::getBalance)
                    .map(bal -> BigDecimal.valueOf(bal).setScale(CURRENCY_SCALE, HALF_DOWN))
                    .orElse(DEFAULT_ACC_BALANCE);
            return bigDecimal;
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank response:[{}]", e.getMessage(), e);
            return DEFAULT_ACC_BALANCE;
        }
    }
}
