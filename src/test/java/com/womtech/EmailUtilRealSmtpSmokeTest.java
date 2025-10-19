package com.womtech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.womtech.util.EmailUtil;

@SpringBootTest
class EmailUtilRealSmtpSmokeTest {

    private static final String TO_EMAIL = "23110181@student.hcmute.edu.vn";

    @Autowired
    private EmailUtil emailUtil;

    @Test
    void sendVerifyOtp_realSmtp_smoke() {
        emailUtil.sendVerifyOtp(TO_EMAIL, "WOMTester", "654321", 15);
    }
}
