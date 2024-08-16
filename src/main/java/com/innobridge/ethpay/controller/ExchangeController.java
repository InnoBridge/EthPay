package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.Crypto;
import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.model.Exchange;
import com.innobridge.ethpay.service.ExchangeService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.innobridge.ethpay.constants.HTTPConstants.*;

@RestController
@RequestMapping(EXCHANGE_URL)
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieve exchange for a specified cryptocurrency",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Exchange.class)))
    })
    public ResponseEntity<?> getExchange(@RequestParam Crypto crypto) {
        try {
            return ResponseEntity.ok(exchangeService.getExchange(crypto));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieves all Exchanges",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Exchange.class)))
    })
    public ResponseEntity<?> getAllExchanges() {
        try {
            return ResponseEntity.ok(exchangeService.getAllExchanges());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Updates an Exchange",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Exchange.class)))
    })
    public ResponseEntity<?> updateExchange(@RequestParam Crypto crypto, @RequestParam Currency currency, @RequestParam Double rate) {
        try {
            return ResponseEntity.ok(exchangeService.updateExchange(crypto, currency, rate));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
