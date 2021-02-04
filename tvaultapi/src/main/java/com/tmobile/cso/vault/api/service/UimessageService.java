package com.tmobile.cso.vault.api.service;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.tmobile.cso.vault.api.common.TVaultConstants;
import com.tmobile.cso.vault.api.controller.ControllerUtil;
import com.tmobile.cso.vault.api.exception.LogMessage;
import com.tmobile.cso.vault.api.model.Message;
import com.tmobile.cso.vault.api.process.RequestProcessor;
import com.tmobile.cso.vault.api.process.Response;
import com.tmobile.cso.vault.api.utils.CommonUtils;
import com.tmobile.cso.vault.api.utils.JSONUtil;
import com.tmobile.cso.vault.api.utils.ThreadLocalContext;
import com.tmobile.cso.vault.api.utils.TokenUtils;

@Component
public class UimessageService {

	@Autowired
	private RequestProcessor reqProcessor;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private CommonUtils commonUtils;

	private static Logger log = LogManager.getLogger(UimessageService.class);

	/**
	 * Save messages
	 * 
	 * @param token
	 * @param message
	 * @return
	 */
	public ResponseEntity<String> writeMessage(String token, Message message) {
		if (!commonUtils.isAuthorizedToken(token)) {
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
					.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER))
					.put(LogMessage.ACTION, "getSecretCount")
					.put(LogMessage.MESSAGE, "Access Denied: No enough permission to access this API")
					.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL)).build()));
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("{\"errors\":[\"Access Denied: No enough permission to access this API\"]}");
		}

		ObjectMapper objMapper = new ObjectMapper();

		HashMap<String, String> metadataMap = message.getDetails();
		String metadataJson = "";
		try {
			metadataJson = objMapper.writeValueAsString(metadataMap);
		} catch (JsonProcessingException e) {
		}

		String path = TVaultConstants.UIMES_SAFES_METADATA;
		String writeJson = "{\"path\":\"" + path + "\",\"data\":" + metadataJson + "}";

		if (isdataNullorEmpty(metadataMap)) {
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
					.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
					.put(LogMessage.ACTION, "Write Message")
					.put(LogMessage.MESSAGE, String.format("Writing message [%s] failed", path))
					.put(LogMessage.RESPONSE, "Invalid data")
					.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
					.build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid data\"]}");
		} else {
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
					.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
					.put(LogMessage.ACTION, "Write message")
					.put(LogMessage.MESSAGE, String.format("Trying to write message [%s]", path))
					.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
					.build()));

			if (ControllerUtil.isFolderExisting(path, token)) {
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
						.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
						.put(LogMessage.ACTION, "isFolderExisting")
						.put(LogMessage.MESSAGE, String.format("Folder is existing [%s]", path))
						.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
						.build()));

				Response response = reqProcessor.process("/write", writeJson, token);
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
						.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
						.put(LogMessage.ACTION, "Save message").put(LogMessage.MESSAGE, "saved messages to folder")
						.put(LogMessage.STATUS, response.getHttpstatus().toString())
						.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
						.build()));

				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"message saved to vault\"]}");
			} else {
				Response response1 = reqProcessor.process("/sdb/createfolder", writeJson, token);
				if (response1.getHttpstatus().equals(HttpStatus.NO_CONTENT)) {
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
							.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
							.put(LogMessage.ACTION, "Create Folder")
							.put(LogMessage.MESSAGE, "Trying to Create folder [%s] completed succssfully")
							.put(LogMessage.STATUS, response1.getHttpstatus().toString()).put(LogMessage.APIURL,
									ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
							.build()));

					Response response = reqProcessor.process("/write", writeJson, token);

					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
							.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
							.put(LogMessage.ACTION, "Save message")
							.put(LogMessage.MESSAGE, "saved messages to folder sucessfully")
							.put(LogMessage.STATUS, response != null ? response.getHttpstatus().toString() : "")
							.put(LogMessage.APIURL,
									ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString())
							.build()));
				}

				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"message saved to vault\"]}");
			}

		}
	}
	
	/**
	 * check for null value 
	 * 
	 * @return
	 */
	
	public boolean isdataNullorEmpty(HashMap<String, String> metadataMap) {
		if (metadataMap.containsValue(null) || metadataMap.isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * Get message
	 * 
	 * @return
	 */

	public ResponseEntity<String> readMessage() {
		String token = tokenUtils.getSelfServiceToken();
		String path = TVaultConstants.UIMES_SAFES_METADATA;
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
				.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
				.put(LogMessage.ACTION, "Get Info")
				.put(LogMessage.MESSAGE, String.format("Trying to get Info for [%s]", path))
				.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).build()));
		Response response = reqProcessor.process("/sdb", "{\"path\":\"" + path + "\"}", token);
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder()
				.put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString())
				.put(LogMessage.ACTION, "Get Info").put(LogMessage.MESSAGE, "Getting Info completed")
				.put(LogMessage.STATUS, response.getHttpstatus().toString())
				.put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).build()));
		return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());

	}

}