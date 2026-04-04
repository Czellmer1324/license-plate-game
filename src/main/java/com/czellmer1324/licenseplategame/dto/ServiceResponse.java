package com.czellmer1324.licenseplategame.dto;

import org.springframework.http.HttpStatus;

import java.util.Map;

public record ServiceResponse(Map<String, String> response, HttpStatus code) {
}
