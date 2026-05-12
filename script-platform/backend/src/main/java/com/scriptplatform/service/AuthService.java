package com.scriptplatform.service;

import com.scriptplatform.dto.LoginRequest;
import com.scriptplatform.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    /**
     * refresh token for the current logged-in user.
     */
    LoginResponse refresh();
}
