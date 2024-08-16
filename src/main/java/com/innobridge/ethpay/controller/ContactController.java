package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.Contacts;
import com.innobridge.ethpay.service.ContactService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static com.innobridge.ethpay.constants.HTTPConstants.*;

@RestController
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieve user's list of email contacts",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Contacts.class)))
    })
    public ResponseEntity<?> getContacts() {
        try {
            return ResponseEntity.ok(contactService.getByEmail());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Add a contact to user's list of email contacts",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Contacts.class)))
    })
    public ResponseEntity<?> addContact(String email) {
        try {
            return ResponseEntity.ok(contactService.addContact(email));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Remove a contact to user's list of email contacts",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Contacts.class)))
    })
    public ResponseEntity<?> removeContact(String email) {
        try {
            return ResponseEntity.ok(contactService.removeContact(email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(e.hashCode()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
