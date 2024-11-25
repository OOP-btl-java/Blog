package com.example.blog.exception;

import org.springframework.http.HttpStatus;

public class InstructorCode {
    public static final ResponseStatus EMPTY_FILE =
            new ResponseStatus("EMPTY_FILE", "Failed to store empty file", HttpStatus.BAD_REQUEST);
    public static final ResponseStatus INVALID_PATH =
            new ResponseStatus("INVALID_PATH", "Cannot store file outside the designated directory", HttpStatus.BAD_REQUEST);
    public static final ResponseStatus FILE_NOT_READABLE =
            new ResponseStatus("FILE_NOT_READABLE", "Could not read file", HttpStatus.BAD_REQUEST);

    public static final ResponseStatus FILE_LOAD_ERROR =
            new ResponseStatus("FILE_LOAD_ERROR", "Could not load file", HttpStatus.INTERNAL_SERVER_ERROR);
    public static final ResponseStatus STORAGE_INIT_ERROR =
            new ResponseStatus("STORAGE_INIT_ERROR", "Could not initialize storage", HttpStatus.INTERNAL_SERVER_ERROR);
}

