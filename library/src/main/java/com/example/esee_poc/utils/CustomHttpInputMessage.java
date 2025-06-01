package com.example.esee_poc.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
@AllArgsConstructor
public class CustomHttpInputMessage implements HttpInputMessage {
    private final InputStream body;
    private final HttpHeaders headers;
}
