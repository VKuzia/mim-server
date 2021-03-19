package com.mimteam.mimserver.model.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    private final ResponseDTO response = new ResponseDTO();

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public static ResponseEntity<ResponseDTO> buildError(ResponseDTO.ResponseType responseType) {
        return builder()
                .responseType(responseType)
                .build();
    }

    public static ResponseEntity<ResponseDTO> buildSuccess() {
        return builder()
                .responseType(ResponseDTO.ResponseType.OK)
                .build();
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

    public ResponseBuilder stringBody(String value) {
        response.setResponseMessage(value);
        return this;
    }

    public ResponseEntity<ResponseDTO> build() {
        if (response.getResponseType() == ResponseDTO.ResponseType.OK) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
