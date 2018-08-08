package com.github.storytime.service;

import com.github.storytime.Pb24ProxyApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Pb24ProxyApplication.class)
public class PbSyncServiceTest {

    @Autowired
    private PbStatementsService pbStatementsService;

    @Autowired
    private PbSyncService pbSyncService;

    @Test
    public void test() {

//            final URL pbXml = getClass().getClassLoader().getResource("pb.xml");
//            final Path pbXmlPath = Paths.get(pbXml.toURI());
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
