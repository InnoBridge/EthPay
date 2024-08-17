package com.innobridge.ethpay.util;

import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utility {

    public static UsernameEmailPasswordAuthenticationToken getAuthentication() {
        return (UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
    }
}
