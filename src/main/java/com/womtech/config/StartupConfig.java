package com.womtech.config;

import com.womtech.security.TokenRevokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupConfig implements CommandLineRunner {

    @Autowired
    private TokenRevokeService tokenRevokeService;

    @Override
    public void run(String... args) throws Exception {
        // Force revoke tất cả tokens khi start (fresh start)
        tokenRevokeService.revokeAllTokens();
        System.out.println("🚀 Server started - All tokens invalidated for fresh start");
    }
}
