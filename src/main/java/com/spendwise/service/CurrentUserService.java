package com.spendwise.service;

import com.spendwise.security.SpendWiseUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        SpendWiseUserDetails userDetails =
                (SpendWiseUserDetails) authentication.getPrincipal();

        return userDetails.getUserId();
    }
}