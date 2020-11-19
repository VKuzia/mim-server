package com.mimteam.mimserver.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    private final ResponseDTO response = new ResponseDTO();

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public ResponseBuilder responseType(ResponseDTO.ResponseType responseType) {
        response.setResponseType(responseType);
        return this;
    }

    public <T> ResponseBuilder body(T object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response.setResponseMessage(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ResponseEntity<ResponseDTO> build() {
        if (response.getResponseType() == ResponseDTO.ResponseType.OK) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public ResponseEntity<ResponseDTO> ok() {
        response.setResponseType(ResponseDTO.ResponseType.OK);
        return build();
    }
}
