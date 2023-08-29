package com.example.postgresfulltextsearch.model;

import java.util.List;

public record Search(List<Period> periods, List<Range> ranges, String fullText, long offset, long limit) {
}
