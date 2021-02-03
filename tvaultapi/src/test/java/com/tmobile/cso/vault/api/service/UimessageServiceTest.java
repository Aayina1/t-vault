package com.tmobile.cso.vault.api.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.codehaus.groovy.syntax.TokenUtil;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.BDDMockito.Then;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.tmobile.cso.vault.api.controller.ControllerUtil;
import com.tmobile.cso.vault.api.model.Message;
import com.tmobile.cso.vault.api.process.RequestProcessor;
import com.tmobile.cso.vault.api.process.Response;
import com.tmobile.cso.vault.api.utils.CommonUtils;
import com.tmobile.cso.vault.api.utils.JSONUtil;
import com.tmobile.cso.vault.api.utils.ThreadLocalContext;
import com.tmobile.cso.vault.api.utils.TokenUtils;

@RunWith(PowerMockRunner.class)
@ComponentScan(basePackages = { "com.tmobile.cso.vault.api" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest({ ControllerUtil.class, JSONUtil.class })
@PowerMockIgnore({ "javax.management.*" })
public class UimessageServiceTest {
	@InjectMocks
	UimessageService uimessageService;
	@Mock
	RequestProcessor reqProcessor;
	@Mock
	CommonUtils commonUtils;
	@Mock 
	TokenUtils tokenUtils;

	@Before
	public void setUp() {
		PowerMockito.mockStatic(ControllerUtil.class);
		PowerMockito.mockStatic(JSONUtil.class);

		Whitebox.setInternalState(ControllerUtil.class, "log", LogManager.getLogger(ControllerUtil.class));
		when(JSONUtil.getJSON(Mockito.any(ImmutableMap.class))).thenReturn("log");

		Map<String, String> currentMap = new HashMap<>();
		currentMap.put("apiurl", "http://localhost:8080/v2/safes/message");
		currentMap.put("user", "");
		ThreadLocalContext.setCurrentMap(currentMap);
	}

	Response getMockResponse(HttpStatus status, boolean success, String expectedBody) {
		Response response = new Response();
		response.setHttpstatus(status);
		response.setSuccess(success);
		if (expectedBody != "") {
			response.setResponse(expectedBody);
		}
		return response;
	}

	@Test
	public void test_writeMessage_successfully() {
		String token = "s.2oefXc9A7iPbP9rGfGtKLUMf";
		String path = "metadata/message";
		String writeJson = "{\"path\":\"metadata/message\",\"data\":{\"message1\":\"value1\",\"message2\":\"value2\"}}";
		String responsejson = "{\"id\":\"s.2oefXc9A7iPbP9rGfGtKLUMf\",\"policies\":[\"root\"]}";
		Response authresponse = getMockResponse(HttpStatus.OK, true, responsejson);

		HashMap<String, String> data = new HashMap<>();
		data.put("message1", "value1");
		data.put("message2", "value2");
		Message message = new Message(data);

		Response responseNoContent = getMockResponse(HttpStatus.NO_CONTENT, true, "");
		Response response = getMockResponse(HttpStatus.OK, true, "{\"messages\":[\"message saved to vault\"]}");
		ResponseEntity<String> responseEntityExpected = ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"message saved to vault\"]}");
		when(commonUtils.isAuthorizedToken(token)).thenReturn(true);
		when(JSONUtil.getJSON(message)).thenReturn(writeJson);
		when(reqProcessor.process("/auth/tvault/lookup", "{}", token)).thenReturn(authresponse);
		String[] policies = { "root" };
		try {
			when(commonUtils.getPoliciesAsArray(Mockito.any(), Mockito.any())).thenReturn(policies);
		} catch (IOException e) {
			e.printStackTrace();
		}

		when(ControllerUtil.isFolderExisting(path, token)).thenReturn(true);

		Response readResponse = getMockResponse(HttpStatus.OK, true,
				"{\"data\":{\"message1\":\"value1\",\"message2\":\"value2\"}}");
		when(reqProcessor.process("/read", "{\"path\":\"metadata/" + path + "\"}", token)).thenReturn(readResponse);

		when(reqProcessor.process(Mockito.eq("/write"), Mockito.anyString(), Mockito.eq(token))).thenReturn(response);
		when(reqProcessor.process(Mockito.eq("/sdb/createfolder"), Mockito.anyString(), Mockito.eq(token))).thenReturn(responseNoContent);

		ResponseEntity<String> responseEntity = uimessageService.writeMessage(token, message);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(responseEntityExpected, responseEntity);
	}

	@Test
	public void test_readMessage_successfully() {
		String token = "2oefXc9A7iPbP9rGfGtKLUMf";
		String metadataJson = "data\":{\"message1\":\"value1\",\"message2\":\"value2\"}";
		String path = "{\"path\":\"metadata/message\"}";

		Response response = getMockResponse(HttpStatus.OK, true, metadataJson);
		ResponseEntity<String> responseEntityExpected = ResponseEntity.status(HttpStatus.OK).body(metadataJson);

		when(tokenUtils.getSelfServiceToken()).thenReturn(token);
		when(reqProcessor.process(Mockito.eq("/sdb"), Mockito.eq(path), Mockito.eq(token))).thenReturn(response);
		ResponseEntity<String> responseEntity = uimessageService.readMessage();
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(responseEntityExpected, responseEntity);
	}

}
