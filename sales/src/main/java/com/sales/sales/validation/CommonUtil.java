package com.sales.sales.validation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class CommonUtil {

    public static ResponseEntity<?> createErrorResponse(Object data, HttpStatus status) {
        GenericResponce response = GenericResponce.builder().responseStatus(status).status("failed").message("failed")
                .data(data).build();
        return response.create();
    }

    public static String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString(); //http:localhost:9090/api/v1/auth
        url = url.replace(request.getRequestURI(), "");//// http:localhost:9090
        return url;
    }

    public static ResponseEntity<?> createBuildResponse(Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.name());
        response.put("message", "success");
        response.put("data", data); // add actual data here
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<?> createErrorResponseMessage(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.name());
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}
