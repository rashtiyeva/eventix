package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.service.CryptoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Slf4j
public class AesCryptoService implements CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec key;

    public AesCryptoService(
            @Value("${crypto.aes.key}") String keyString
    ) {

        byte[] keyBytes = HexFormat.of().parseHex(keyString);

        if (keyBytes.length != 16 &&
                keyBytes.length != 24 &&
                keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES key must be 16, 24, or 32 bytes"
            );
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String encrypt(String data) {

        try {

            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] encrypted =
                    cipher.doFinal(
                            data.getBytes(StandardCharsets.UTF_8)
                    );

            byte[] combined =
                    ByteBuffer.allocate(iv.length + encrypted.length)
                            .put(iv)
                            .put(encrypted)
                            .array();

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Encryption failed",
                    e
            );
        }
    }

    @Override
    public String decrypt(String data) {

        try {

            byte[] combined =
                    Base64.getDecoder().decode(data);

            ByteBuffer buffer =
                    ByteBuffer.wrap(combined);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted =
                    new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Decryption failed",
                    e
            );
        }
    }
}