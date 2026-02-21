package com.ocean.resort.service.impl;

import com.ocean.resort.service.UserStore;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl {
	private final UserStore userStore;

	public LoginServiceImpl(UserStore userStore) {
		this.userStore = userStore;
	}

	public boolean authenticate(String username, String password) {
		if (username == null || username.isBlank()) {
			return false;
		}
		if (password == null || password.isBlank()) {
			return false;
		}
		return userStore.isValidCredential(username.trim(), password);
	}
}
