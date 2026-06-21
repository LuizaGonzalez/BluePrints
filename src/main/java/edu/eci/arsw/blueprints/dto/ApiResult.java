package edu.eci.arsw.blueprints.dto;

public record ApiResult<T>(int code, String message, T data) {}
