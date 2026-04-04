package com.czellmer1324.licenseplategame.dto;

import org.springframework.http.HttpStatus;

public record ServiceResponse(Object response, HttpStatus code) {
}
