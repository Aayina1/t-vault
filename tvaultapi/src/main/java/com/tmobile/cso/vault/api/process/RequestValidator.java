// =========================================================================
// Copyright 2017 T-Mobile USA, Inc.
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
// =========================================================================

package com.tmobile.cso.vault.api.process;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.tmobile.cso.vault.api.config.ApiConfig;

@Component
public class RequestValidator {
	@Autowired
	private RestProcessor restProcessor;
	
	public Message validate(final ApiConfig apiConfig,final Map<String, Object> requestParams,String token){
		Message msg = new Message();
		switch (apiConfig.getApiEndPoint()){
			case "/access/create":{
				boolean duplicate = checkforDuplicatePolicy(requestParams, token);
				if(duplicate){
					msg.setMsgTxt("Existing access id. Use '/access/update' to update");
					msg.setMsgType(MSG_TYPE.ERR);
				}
				break;
		
			}
			case "/auth/aws/roles/create":{
				boolean duplicate = checkforDuplicateAwsGroup(requestParams, token);
				if(duplicate){
					msg.setMsgTxt("Existing role. Use '/auth/aws/roles/update' if needed");
					msg.setMsgType(MSG_TYPE.ERR);
				}
				break;
			}
			case "/sdb/create":{
				boolean duplicate = checkforDuplicateSDB(requestParams, token);
				if(duplicate){
					msg.setMsgTxt("Existing safe");
					msg.setMsgType(MSG_TYPE.ERR);
				}
				break;
			}
		}
		return msg;
	}
	private boolean checkforDuplicatePolicy(Map<String, Object> requestParams,String token){
		if(requestParams.get("accessid")!= null){
			String policyName = requestParams.get("accessid").toString();
			ResponseEntity<String> valutResponse = restProcessor.get("/sys/policy/"+policyName, token);
			if(valutResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)){
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean checkforDuplicateAwsGroup(Map<String, Object> requestParams,String token){	
		if(requestParams.get("role") !=null){
			String role = requestParams.get("role").toString();
			ResponseEntity<String> valutResponse = restProcessor.get("/auth/aws/role/"+role, token);
			if(HttpStatus.OK.equals(valutResponse.getStatusCode())){
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean checkforDuplicateSDB(Map<String, Object> requestParams,String token){	
		if(requestParams.get("path") !=null){
			String path = requestParams.get("path").toString();
			ResponseEntity<String> valutResponse = restProcessor.get("/metadata/"+path, token);
			if(valutResponse.getStatusCode().equals(HttpStatus.OK)){
				return true;
			}
			return false;
		}
		return false;
	}
}
