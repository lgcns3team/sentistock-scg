package com.example.sentistock_scg.jwt;

public interface JwtTokenProvider {
    boolean validate(String token);
    String getUserId(String token);
}
