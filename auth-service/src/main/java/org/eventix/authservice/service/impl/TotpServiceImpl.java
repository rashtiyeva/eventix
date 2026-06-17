package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.service.TotpService;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TotpServiceImpl implements TotpService {

    @Value("${app.name:Eventix}")
    private String issuer;

    @Override
    public String generateSecret() {
        return Base32.random();
    }

    @Override
    public String getQrUri(String secret, String email) {

        String encodedIssuer =
                URLEncoder.encode(issuer, StandardCharsets.UTF_8);

        String encodedEmail =
                URLEncoder.encode(email, StandardCharsets.UTF_8);

        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer,
                encodedEmail,
                secret,
                encodedIssuer
        );
    }

    @Override
    public boolean verifyCode(String secret, String code) {

        if (secret == null || code == null) {
            return false;
        }

        if (!code.matches("\\d{6}")) {
            return false;
        }

        Totp totp = new Totp(secret);

        return totp.verify(code);
    }
}
