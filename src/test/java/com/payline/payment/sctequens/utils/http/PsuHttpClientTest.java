package com.payline.payment.sctequens.utils.http;

import com.payline.payment.sctequens.MockUtils;
import com.payline.payment.sctequens.bean.business.psu.Psu;
import com.payline.payment.sctequens.bean.business.psu.PsuCreateRequest;
import com.payline.payment.sctequens.bean.configuration.RequestConfiguration;
import com.payline.payment.sctequens.exception.InvalidDataException;
import com.payline.payment.sctequens.exception.PluginException;
import com.payline.payment.sctequens.utils.properties.ConfigProperties;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PsuHttpClientTest {

    @Spy
    @InjectMocks
    private PsuHttpClient psuHttpClient = new PsuHttpClient();


    @BeforeEach
    void setup(){
        MockitoAnnotations.initMocks(this);
        // Mock a valid authorization
        doReturn( MockUtils.anAuthorization() ).when( psuHttpClient ).authorize( any(RequestConfiguration.class) );
    }

    @AfterEach
    void verifyMocks(){
        /* verify that execute() method is never called ! it ensures the mocks are working properly and there is no
        false negative that could be related to a failed HTTP request sent to the partner API. */
        verify( psuHttpClient, never() ).execute( any( HttpRequestBase.class ) );
    }

    // --- Test PsuHttpclient#createPsu ---

    @Test
    void createPsu_nominal(){
        // given: the partner API returns a valid success response
        String responseBody = "{" +
                "  \"MessageCreateDateTime\":\"2019-11-18T09:50:32.724+0000\"," +
                "  \"MessageId\":\"d43a25cd1f29436ca40597429c9242fc\"," +
                "  \"Psu\":{" +
                "    \"PsuId\":\"303\"," +
                "    \"Address\":{\"AddressLines\":[{},{}]}," +
                "    \"Status\":\"ACTIVE\"" +
                "  }" +
                "}";
        doReturn( HttpTestUtils.mockStringResponse(201, "Created", responseBody ) )
                .when( psuHttpClient )
                .post( anyString(), anyList(), any(HttpEntity.class) );

        // when: calling the method
        Psu createdPsu = psuHttpClient.createPsu( MockUtils.aPsuCreateRequest(), MockUtils.aRequestConfiguration() );

        // then: created PSU returned contains the right values
        assertNotNull( createdPsu );
        assertEquals( "303", createdPsu.getPsuId() );
        assertEquals( "ACTIVE", createdPsu.getStatus() );
    }

    @Test
    void createPsu_invalidConfig(){
        // given: the config property containing the path is missing
        PartnerConfiguration partnerConfiguration;
        partnerConfiguration = new PartnerConfiguration(new HashMap<>(), new HashMap<>());
        RequestConfiguration requestConfiguration = new RequestConfiguration(MockUtils.aContractConfiguration("FR"), MockUtils.anEnvironment(), partnerConfiguration);


        // when: calling the method, then: an exception is thrown
        PsuCreateRequest request = MockUtils.aPsuCreateRequest();
        assertThrows( InvalidDataException.class, () -> psuHttpClient.createPsu( request , requestConfiguration ) );
    }

    @Test
    void createPsu_noPsuInResponse(){
        // given: the partner API returns a invalid success response, without the PSU data
        String responseBody = "{" +
                "  \"MessageCreateDateTime\":\"2019-11-18T09:50:32.724+0000\"," +
                "  \"MessageId\":\"d43a25cd1f29436ca40597429c9242fc\"" +
                "}";
        doReturn( HttpTestUtils.mockStringResponse(201, "Created", responseBody ) )
                .when( psuHttpClient )
                .post( anyString(), anyList(), any(HttpEntity.class) );

        // when: calling the method
        Psu createdPsu = psuHttpClient.createPsu( MockUtils.aPsuCreateRequest(), MockUtils.aRequestConfiguration() );

        // then: no error encountered, but returned object is null
        assertNull( createdPsu );
    }

    @Test
    void createPsu_missingMessageId(){
        // given: the partner API returns an error response
        String responseBody = "{\n" +
                "    \"MessageCreateDateTime\": \"2019-12-03T15:39:47.226+0000\",\n" +
                "    \"MessageId\": \"a6c264d15f1a40f0800e4667bebff622\",\n" +
                "    \"code\": \"002\",\n" +
                "    \"message\": \"The message does not comply the schema definition\",\n" +
                "    \"details\": \"Property messageId : must not be null\"\n" +
                "}";
        doReturn( HttpTestUtils.mockStringResponse(400, "Bad Request", responseBody ) )
                .when( psuHttpClient )
                .post( anyString(), anyList(), any(HttpEntity.class) );

        // when: calling the method
        PsuCreateRequest request = MockUtils.aPsuCreateRequest();
        RequestConfiguration requestConfiguration = MockUtils.aRequestConfiguration();
        PluginException thrown = assertThrows(PluginException.class,
                () -> psuHttpClient.createPsu( request, requestConfiguration ) );

        assertEquals("Property messageId : must not be null", thrown.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, thrown.getFailureCause());

    }

}
