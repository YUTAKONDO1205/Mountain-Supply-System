package com.kondo.mss.common;

public record ApiResponse<T>(String message, T data) {
}
