package com.youtube.chanalyzer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorController {
    @GetMapping("/health")
    public ResponseEntity<Object> getError() {
        return new ResponseEntity<>("Oops, something went wrong!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
