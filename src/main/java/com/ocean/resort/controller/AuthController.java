package com.ocean.resort.controller;

import com.ocean.resort.service.impl.LoginServiceImpl;
import com.ocean.resort.dto.request.LoginRequest;
import com.ocean.resort.dto.response.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final LoginServiceImpl loginServiceImpl;

	public AuthController(LoginServiceImpl loginService) {
		this.loginServiceImpl = loginService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		if (request == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new LoginResponse(false, "Request body is required."));
		}

		String username = request.username();
		String password = request.password();
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new LoginResponse(false, "Username and password are required."));
		}

		boolean authenticated = loginServiceImpl.authenticate(username, password);
		if (!authenticated) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new LoginResponse(false, "Invalid username or password."));
		}

		return ResponseEntity.ok(new LoginResponse(true, "Login successful."));
	}
}
