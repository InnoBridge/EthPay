package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.ProfileResponse;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.util.Utility.getAuthentication;

@RestController
@RequestMapping(PROFILE_URL)
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieve user's profile",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = ProfileResponse.class)))
    })
    public ResponseEntity<?> getProfile() {
        try {
            User user = userService.getById(getAuthentication().getId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
            return ResponseEntity.ok(new ProfileResponse(user.getId(), user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
