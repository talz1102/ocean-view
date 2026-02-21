package com.ocean.resort.service;

public interface UserStore {
	boolean isValidCredential(String username, String rawPassword);
}
