package com.github.storytime.service;

import com.github.storytime.config.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.storytime.config.Constants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SignatureGeneratorService {


    private static final Logger LOGGER = LogManager.getLogger(SignatureGeneratorService.class);

    public String generateSignature(final String starDate, final String endDate,
                                    final String card, final String password) {

        try {
            final String dataString = "<oper>" + CMT + "</oper><wait>" + PB_WAIT + "</wait><test>" + TEST + "</test>" +
                    "<payment id=\"\"><prop name=\"sd\" value=\"" + starDate + "\"/>" +
                    "<prop name=\"ed\" value=\"" + endDate + "\"/>" +
                    "<prop name=\"card\" value=\"" + card + "\"/>" +
                    "</payment>";

            return getSha1(getMd5(dataString + password));
        } catch (Exception e) {
            throw new RuntimeException("No such algo " + e.getMessage());
        }
    }

    private String getMd5(final String pass) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(MD5);
        md.reset();
        md.update(pass.getBytes(UTF_8));

        final StringBuilder sb = new StringBuilder();
        for (final byte b : md.digest()) {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }


    private String getSha1(final String str) throws NoSuchAlgorithmException {
        final MessageDigest crypt = MessageDigest.getInstance(Constants.SHA_1);
        crypt.reset();
        crypt.update(str.getBytes(UTF_8));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

}
