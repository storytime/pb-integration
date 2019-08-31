package com.github.storytime.mapper;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.error.exception.PbSignatureException;
import com.github.storytime.model.jaxb.statement.response.ok.Response;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbStatementMapper {

    private static final Logger LOGGER = getLogger(PbStatementMapper.class);
    private final Unmarshaller jaxbStatementErrorUnmarshaller;
    private final Unmarshaller jaxbStatementOkUnmarshaller;
    private final CustomConfig customConfig;

    @Autowired
    public PbStatementMapper(final Unmarshaller jaxbStatementErrorUnmarshaller,
                             final CustomConfig customConfig,
                             final Unmarshaller jaxbStatementOkUnmarshaller) {
        this.jaxbStatementErrorUnmarshaller = jaxbStatementErrorUnmarshaller;
        this.customConfig = customConfig;
        this.jaxbStatementOkUnmarshaller = jaxbStatementOkUnmarshaller;
    }

    public List<Statement> mapRequestBody(ResponseEntity<String> responseEntity) {

        final var body = ofNullable(responseEntity.getBody()).orElse(EMPTY);
        if (body.contains(customConfig.getPbBankSignature())) {
            throw new PbSignatureException("Invalid signature");
        }

        try {
            if (!body.contains("signature")) { // is error response, wrong ip etc
                final com.github.storytime.model.jaxb.statement.response.error.Response error =
                        (com.github.storytime.model.jaxb.statement.response.error.Response) jaxbStatementErrorUnmarshaller.unmarshal(new StringReader(body));
                LOGGER.error("Bank return response with error:[{}]", error.getData().getError().getMessage());
                return emptyList();
            }

            final Response parsedResponse = (Response) jaxbStatementOkUnmarshaller.unmarshal(new StringReader(body));
            return ofNullable(parsedResponse.getData())
                    .map(Response.Data::getInfo)
                    .map(Response.Data.Info::getStatements)
                    .map(Response.Data.Info.Statements::getStatement)
                    .orElse(emptyList());
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank response:[{}]", e.getMessage(), e);
            return emptyList();
        }
    }
}
