package com.mimteam.mimserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

public class TestingUtils {
    public static String convertToString(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    public static ResponseBuilder createEmptySuccessResponseBuilder() {
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        Mockito.when(responseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.build()).thenReturn(ResponseEntity.ok().build());
        return responseBuilder;
    }

    public static ResponseBuilder createMockSuccessResponseBuilder() {
        ResponseBuilder responseBuilder = createEmptySuccessResponseBuilder();
        Mockito.when(responseBuilder.body(Mockito.any())).thenReturn(responseBuilder);
        return responseBuilder;
    }

    public static ResponseBuilder createMockSuccessResponseBuilder(Object body) {
        ResponseBuilder responseBuilder = createEmptySuccessResponseBuilder();
        Mockito.when(responseBuilder.body(body)).thenReturn(responseBuilder);
        return responseBuilder;
    }

    public static ResponseBuilder createAnyStringSuccessResponseBuilder() {
        ResponseBuilder responseBuilder = createEmptySuccessResponseBuilder();
        Mockito.when(responseBuilder.stringBody(Mockito.anyString())).thenReturn(responseBuilder);
        return responseBuilder;
    }
}
