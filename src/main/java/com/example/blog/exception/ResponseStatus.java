package com.example.blog.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class ResponseStatus {
    private String code;
    private String message;
    private HttpStatus status;
}
