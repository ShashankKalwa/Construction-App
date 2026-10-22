package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.CalculatorType;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.CalculationService;
import com.example.contruction_management_app.backend.service.CivilEngineeringCalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CivilEngineeringCalculatorService calculatorService;
    private final CalculationService calculationService;

    public CalculatorController(
        CivilEngineeringCalculatorService calculatorService,
        CalculationService calculationService
    ) {
        this.calculatorService = calculatorService;
        this.calculationService = calculationService;
    }

    // =========================================================================
    // Pure Calculation Endpoints
    // =========================================================================

    @PostMapping("/calculators/concrete")
    public ResponseEntity<ConcreteCalcResult> calculateConcrete(@Valid @RequestBody ConcreteCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculateConcrete(request));
    }

    @PostMapping("/calculators/steel")
    public ResponseEntity<SteelCalcResult> calculateSteel(@Valid @RequestBody SteelCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculateSteel(request));
    }

    @PostMapping("/calculators/brickwork")
    public ResponseEntity<BrickworkCalcResult> calculateBrickwork(@Valid @RequestBody BrickworkCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculateBrickwork(request));
    }

    @PostMapping("/calculators/excavation")
    public ResponseEntity<ExcavationCalcResult> calculateExcavation(@Valid @RequestBody ExcavationCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculateExcavation(request));
    }

    @PostMapping("/calculators/plaster")
    public ResponseEntity<PlasterCalcResult> calculatePlaster(@Valid @RequestBody PlasterCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculatePlaster(request));
    }

    @PostMapping("/calculators/paint")
    public ResponseEntity<PaintCalcResult> calculatePaint(@Valid @RequestBody PaintCalcRequest request) {
        return ResponseEntity.ok(calculatorService.calculatePaint(request));
    }

    @PostMapping("/calculators/unit-convert")
    public ResponseEntity<UnitConvertResult> convertUnits(@Valid @RequestBody UnitConvertRequest request) {
        return ResponseEntity.ok(calculatorService.convertUnits(request));
    }

    // =========================================================================
    // Project Calculation History Endpoints
    // =========================================================================

    @PostMapping("/projects/{projectId}/calculations")
    public ResponseEntity<CalculationResponseDto> saveCalculation(
        @PathVariable String projectId,
        @Valid @RequestBody CalculationSaveRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Ensure projectId in URL matches body if given
        CalculationSaveRequest normalized = new CalculationSaveRequest(
            projectId,
            request.calculatorType(),
            request.title(),
            request.elementType(),
            request.inputParameters(),
            request.outputResults(),
            request.primaryQuantity(),
            request.primaryUnit()
        );
        CalculationResponseDto saved = calculationService.saveCalculation(normalized, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/projects/{projectId}/calculations")
    public ResponseEntity<List<CalculationResponseDto>> getProjectCalculations(
        @PathVariable String projectId,
        @RequestParam(required = false) CalculatorType type,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CalculationResponseDto> calculations = calculationService.getCalculationsForProject(projectId, type, principal);
        return ResponseEntity.ok(calculations);
    }

    @GetMapping("/calculations/{id}")
    public ResponseEntity<CalculationResponseDto> getCalculationById(
        @PathVariable String id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CalculationResponseDto calculation = calculationService.getCalculationById(id, principal);
        return ResponseEntity.ok(calculation);
    }
}
