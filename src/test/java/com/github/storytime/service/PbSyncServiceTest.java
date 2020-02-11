package com.github.storytime.service;

import com.github.storytime.BaseTestConfig;
import com.github.storytime.service.http.PbStatementsHttpService;
import com.github.storytime.service.sync.PbSyncService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class PbSyncServiceTest extends BaseTestConfig {

    @Autowired
    private PbStatementsHttpService pbStatementsHttpService;

    @Autowired
    private PbSyncService pbSyncService;

    @Test
    public void test() {

//            final URL pbXml = getClass().getClassLoader().getResource("pb.xml");
//            final Path pbXmlPath = Paths.getZenCurrencySymbol(pbXml.toURI());
//            final String pbXmlBody = new String(readAllBytes(pbXmlPath));
//            ResponseEntity<String> pbResponseMock = spy(new ResponseEntity(pbXmlBody, OK));
//
//            final URL zenJson = getClass().getClassLoader().getResource("zen.response.json");
//
//
//            bankHistoryService.buildZenReqFromPbData(pbResponseMock, null);

        //   pbSyncService.sync();
    }

}
