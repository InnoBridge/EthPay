package com.innobridge.ethpay.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {

    @GetMapping("/oauth2/success")
    public String success(@AuthenticationPrincipal OAuth2User oAuth2User) {

        return "Success";
    }

    @GetMapping("/oauth2/redirect")
    public String redirect(@RequestParam boolean email_verified, @RequestParam String email) {
        return "email_verified: " + email_verified + " email: " + email;
    }
    @GetMapping("/")
    public String index() {
//        model.addAttribute("userName", oauth2User.getName());
//        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
//        model.addAttribute("userAttributes", oauth2User.getAttributes());
        return "Hello world";
    }
}
