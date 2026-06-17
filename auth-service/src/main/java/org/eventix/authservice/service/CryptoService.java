package org.eventix.authservice.service;

public interface CryptoService {
    String encrypt(String data);
    String decrypt(String data);
}