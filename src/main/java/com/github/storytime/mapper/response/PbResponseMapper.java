package com.github.storytime.mapper.response;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.error.exception.PbInvalidIpException;
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
import java.util.Optional;

import static com.github.storytime.config.props.Constants.*;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbResponseMapper {

    private static final Logger LOGGER = getLogger(PbResponseMapper.class);

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


    public List<Statement> mapStatementRequestBody(final ResponseEntity<String> responseEntity, final String shortDesc) {

        final var maybeBody = ofNullable(responseEntity.getBody()).orElse(EMPTY);

        if (maybeBody.contains(customConfig.getPbBankSignature())) {
            throw new PbSignatureException(INVALID_SIGNATURE_ERROR);
        }

        if (maybeBody.contains(INVALID_IP)) {
            throw new PbInvalidIpException(INVALID_IP_ERROR);
        }

        try {
            if (!maybeBody.contains(SIGNATURE)) { // is error response, wrong ip etc
                final var error = (com.github.storytime.model.pb.jaxb.statement.response.error.Response) jaxbStatementErrorUnmarshaller.unmarshal(new StringReader(maybeBody));
                LOGGER.error("Bank return for: [{}], response with error: [{}]", shortDesc, error.getData().getError().getMessage());
                return emptyList();
            }
            LOGGER.debug("Bank response for: [{}], string:\n [{}]", shortDesc, maybeBody);
            final var parsedResponse = (com.github.storytime.model.pb.jaxb.statement.response.ok.Response) jaxbStatementOkUnmarshaller.unmarshal(new StringReader(maybeBody));
            LOGGER.debug("Bank response for: [{}], string parsed: [{}]", shortDesc, parsedResponse);

            Optional<com.github.storytime.model.pb.jaxb.statement.response.ok.Response> parsedResponseMaybe = ofNullable(parsedResponse);
            Optional<com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data> dataMaybe = parsedResponseMaybe.map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response::getData);
            Optional<com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info> infoMaybe = dataMaybe.map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data::getInfo);
            Optional<com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements> statementsMaybe = infoMaybe.map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info::getStatements);
            Optional<List<Statement>> statementsListMaybe = statementsMaybe.map(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements::getStatement);

            LOGGER.debug("Before pas parsedResponseMaybe: [{}], dataMaybe: [{}], infoMaybe: [{}], statementsMaybe: [{}], statementsListMaybe: [{}]", parsedResponseMaybe, dataMaybe, infoMaybe, statementsMaybe, statementsListMaybe);

            return statementsListMaybe.orElse(emptyList());
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank XML, for: [{}], response: [{}]", shortDesc, e.getMessage(), e);
            return emptyList();
        }
    }

    public BigDecimal mapAccountRequestBody(final ResponseEntity<String> responseEntity) {
        final var body = ofNullable(responseEntity.getBody()).orElse(EMPTY);
        try {
            final Response parsedResponse = (Response) jaxbAccountOkUnmarshaller.unmarshal(new StringReader(body));
            return ofNullable(parsedResponse.getData())
                    .map(Response.Data::getInfo)
                    .map(Response.Data.Info::getCardbalance)
                    .map(Response.Data.Info.Cardbalance::getBalance)
                    .map(bal -> BigDecimal.valueOf(bal).setScale(CURRENCY_SCALE, HALF_DOWN))
                    .orElse(DEFAULT_ACC_BALANCE);
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank response: [{}]", e.getMessage(), e);
            return DEFAULT_ACC_BALANCE;
        }
    }
}
