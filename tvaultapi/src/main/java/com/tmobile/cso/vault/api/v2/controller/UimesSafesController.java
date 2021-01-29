package com.tmobile.cso.vault.api.v2.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.tmobile.cso.vault.api.model.Message;
import com.tmobile.cso.vault.api.service.UimesSafesService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@Api(description = "Manage Ui message", position = 25)
public class UimesSafesController {

	@Value("${vault.auth.method}")
	private String vaultAuthMethod;

	@Autowired
	private UimesSafesService uimesSafesService;

	@ApiOperation(value = "${UimesSafesController.write.value}", notes = "${UimesSafesController.write.notes}")
	@PostMapping(value = { "v2/safes/message" }, consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> write(HttpServletRequest request, @RequestHeader(value = "vault-token") String token,
			@RequestBody Message message) {

		return uimesSafesService.write(token, message);

	}

	@ApiOperation(value = "${UimesSafesController.readFromVault.value}", notes = "${UimesSafesController.readFromVault.notes}")
	@GetMapping(value = "v2/safes/message", produces = "application/json")
	public ResponseEntity<String> readMessage() {
		return uimesSafesService.readMessage();
	}
}