package com.mimteam.mimserver.model.responses;

import org.springframework.http.ResponseEntity;

import com.mimteam.mimserver.model.responses.ResponseDTO.ResponseType;

public class ErrorResponseCreator {
    public static ResponseEntity<ResponseDTO> createResponse(final ResponseType responseType) {
        return ResponseBuilder.builder()
                .responseType(responseType)
                .build();
    }
}
