// =========================================================================
// Copyright 2018 T-Mobile, US
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// See the readme.txt file for additional language around disclaimer of warranties.
// =========================================================================

package com.tmobile.cso.vault.api.process;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.vault.api.config.ApiConfig;
import com.tmobile.cso.vault.api.config.ConfigManager;
import com.tmobile.cso.vault.api.config.Param;
import com.tmobile.cso.vault.api.exception.NoApiConfigFoundException;


@Component
public class RequestProcessor {
	@Autowired
	private RestProcessor restprocessor;
	@Autowired
	private RequestValidator reqValidator;
	@Autowired
	private RequestTransformer reqTransformer;
	@Autowired
	private ResponseTransformer respTransformer;
	
	private static Logger log = LogManager.getLogger(RequestProcessor.class);
	
	public Response process(String apiEndPoint, String request, String token){
		Response response = new Response(); 
		ApiConfig apiConfig = null ;
		try{
			apiConfig = ConfigManager.lookUpApiConfig(apiEndPoint);
		}catch(NoApiConfigFoundException e){
			log.error(e);
			response.httpstatus= HttpStatus.NOT_IMPLEMENTED;
			response.success = false;
			response.response= "{\"errors\":[\"End point is not not found/ configured.\"]}";
			return response;
		}
		
	
		
		Map<String, Object> requestParams = parseInputJson (request,response);
		
		if(requestParams == null){
			return response;
		}
		
		Message msg = reqValidator.validate(apiConfig, requestParams, token);
		if(MSG_TYPE.ERR.equals(msg.getMsgType())){
			response.httpstatus= HttpStatus.UNPROCESSABLE_ENTITY;
			response.success = false;
			response.response= "{\"errors\":[\""+msg.getMsgTxt()+"\"]}";
			return response;
		}
		
		reqTransformer.transform(apiConfig, requestParams);
		
		StringBuffer vaultEndponint = new StringBuffer(apiConfig.getVaultEndPoint());
		String vaultRequestJson = createVaultRequestJson(requestParams,apiConfig.getParams(),vaultEndponint,response);
		
		if(vaultRequestJson == null){
			return response;
		}
		
		
		ResponseEntity<String> vaultResponse = null;
		
		switch (apiConfig.getMethod()) {
		case "POST":
			vaultResponse = restprocessor.post(vaultEndponint.toString(), token, vaultRequestJson);
			break;
		case "GET":
			vaultResponse = restprocessor.get(vaultEndponint.toString(), token);
			break;
		case "DELETE":
			vaultResponse = restprocessor.delete(vaultEndponint.toString(), token);
			break;
		}
		
		log.debug("Status >"+vaultResponse.getStatusCode());
		
		Map<String,Object > vaultResponseMap = new HashMap<String,Object >();

		
		if(!HttpStatus.NO_CONTENT.equals(vaultResponse.getStatusCode())){
			vaultResponseMap = parseVaultResponseJson(vaultResponse.getBody());
		}
		
		if(!(vaultResponseMap.containsKey("errors") || vaultResponseMap.size() == 0))
			respTransformer.transform(apiConfig, vaultResponseMap,token);
		
		response.response = createResponseJson(vaultResponseMap,apiConfig);
		response.httpstatus= vaultResponse.getStatusCode();
		return response;
	}
		
	private String createVaultRequestJson (final Map<String, Object> requestParams,List<Param> params, StringBuffer path, Response response){
		
		if(params==null) return "{}";
		
		Map<String, Object> outputParams = new  HashMap<String, Object>();
		for(Param param:params){
			Object value;
			if(param.getValue()!=null){
				value = param.getValue();
			}else{
				value = requestParams.get(param.getName());
			}
					
			if(value==null && param.isRequired()){
				response.success = false;
				response.httpstatus = HttpStatus.BAD_REQUEST;
				response.response = "{\"errors\":[\"Requried Parameter Missing : "+ param.getName()+"\"]}";
				break;
			}
			if(param.isAppendToPath()){
				String _toReplace = "<"+param.getName()+">";
				int _replaceLen = _toReplace.length();
				int _starIndex = path.indexOf(_toReplace);
				path.replace(_starIndex, _starIndex+_replaceLen,value.toString());
			}
			
			if(!param.isAppendToPath()){	
				if(value!=null)
					outputParams.put(param.getName(), value);
			}
				
		}
		if(response.httpstatus == null || "".equals(response.httpstatus)){
			try {
				if(outputParams.containsKey("data")){
					return new ObjectMapper().writeValueAsString(outputParams.get("data"));
				}else{
					return new ObjectMapper().writeValueAsString(outputParams);
				}
			} catch (JsonProcessingException e) {
				log.error(e);
				response.success = false;
				response.httpstatus = HttpStatus.UNPROCESSABLE_ENTITY;
				response.response = "{\"errors\":[\"Unexpected input \"]}";
				return null;
			}
		}else{
			return null;
		}
	}
	
	private Map<String, Object> parseInputJson(String jsonString, Response response){
		
		Map<String, Object> requestParams = null ;
		try {
			requestParams = new ObjectMapper().readValue(jsonString, new TypeReference<Map<String, Object>>(){});
		} catch (JsonParseException e) {
			log.error(e);
			response.httpstatus= HttpStatus.BAD_REQUEST;
			response.success = false;
			response.response= "{\"errors\":[\"Invalid request. Check JSON \"]}";
		} catch (JsonMappingException e) {
			log.error(e);
			response.httpstatus= HttpStatus.BAD_REQUEST;
			response.success = false;
			response.response= "{\"errors\":[\"Invalid request. Check JSON \"]}";
		} catch (IOException e) {
			log.error(e);
			response.httpstatus= HttpStatus.BAD_REQUEST;
			response.success = false;
			response.response= "{\"errors\":[\"Invalid request. Check JSON \"]}";
		}
		return requestParams;
	}
	
	
	private Map<String,Object> parseVaultResponseJson (String jsonString){
		Map<String, Object> vaultResponse = new HashMap<String, Object>(); 
		try {
			if(jsonString !=null )
				vaultResponse = new ObjectMapper().readValue(jsonString, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e) {
			log.error(e);
		}
		return vaultResponse;
	}
	
	private String createResponseJson(Map<String,Object> vaultResponse,ApiConfig apiConfig){
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,Object> apiResponseMap = new LinkedHashMap<String,Object>();
		try {
			if(vaultResponse.containsKey("errors")){
				return objMapper.writeValueAsString(vaultResponse);
			}else{
				if(apiConfig.getOutparams()!=null){
					for(String param : apiConfig.getOutparams()){
						if(param.contains("/")){
							String paths[] = param.split("/");
							Object value = vaultResponse.get(paths[0]);
							JsonNode root = objMapper.readTree(objMapper.writeValueAsString(value));
							String jsonPointer = param.substring(param.indexOf("/"));
							apiResponseMap.put(paths[paths.length-1],root.at(jsonPointer));							
						}else{
							apiResponseMap.put(param, vaultResponse.get(param));
						}
					}
					return objMapper.writeValueAsString(apiResponseMap);
				}
			}
		}catch (JsonProcessingException e) {
			log.error(e);
		}catch (IOException e){
			log.error(e);
		}
		return null;
	}
}
