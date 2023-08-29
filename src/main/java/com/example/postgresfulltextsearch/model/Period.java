package com.example.postgresfulltextsearch.model;

import java.time.LocalDateTime;

public record Period(String fieldName, LocalDateTime min, LocalDateTime max) {
}
