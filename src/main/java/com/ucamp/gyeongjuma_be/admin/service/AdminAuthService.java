package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.AdminLoginRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminLoginResult;

public interface AdminAuthService {

    AdminLoginResult login(AdminLoginRequest request);
}
