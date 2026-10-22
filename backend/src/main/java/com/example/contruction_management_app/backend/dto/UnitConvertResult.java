package com.example.contruction_management_app.backend.dto;

import java.util.Map;

public record UnitConvertResult(
    String category,
    Double fromValue,
    String fromUnit,
    Double toValue,
    String toUnit,
    String formulaUsed,
    Map<String, Double> allConversions
) {}
