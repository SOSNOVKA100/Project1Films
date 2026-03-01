package com.example.project1films.security;

import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class EncryptionService {

    public String encrypt(String data) {

        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decrypt(String encrypted) {

        return new String(Base64.getDecoder().decode(encrypted));
    }
}