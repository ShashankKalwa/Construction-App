package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.service.CivilEngineeringCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CivilEngineeringCalculatorServiceTest {

    private CivilEngineeringCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CivilEngineeringCalculatorService();
    }

    @Test
    void testCalculateConcrete_M20StandardSlab() {
        // 10m x 5m x 0.15m slab
        ConcreteCalcRequest request = new ConcreteCalcRequest(
            10.0, 5.0, 0.15, "M20", 5.0, "SLAB"
        );
        ConcreteCalcResult result = calculatorService.calculateConcrete(request);

        assertNotNull(result);
        assertEquals(7.5, result.wetVolumeM3());
        assertEquals("M20", result.grade());
        assertEquals("1:1.5:3", result.mixRatio());
        assertTrue(result.dryVolumeM3() > 7.5);
        assertTrue(result.cementBags() > 60 && result.cementBags() < 70);
        assertTrue(result.sandTonnes() > 5.0);
        assertTrue(result.aggregateTonnes() > 9.0);
        assertTrue(result.waterLiters() > 1500);
    }

    @Test
    void testCalculateSteel_16mmBars() {
        // 10 bars of 16mm, length 12m
        SteelCalcRequest request = new SteelCalcRequest(
            16.0, 12.0, 10, 3.0, "COLUMN"
        );
        SteelCalcResult result = calculatorService.calculateSteel(request);

        assertNotNull(result);
        assertEquals(16.0, result.diameterMm());
        assertEquals(120.0, result.totalLengthM());
        assertEquals(1.580, result.unitWeightKgPerM()); // 16^2 / 162 = 1.5802...
        assertTrue(result.totalWeightKg() > 190.0 && result.totalWeightKg() < 200.0);
        assertEquals(3, result.bundleCount()); // 10 bars / 4 per bundle = 3 bundles
    }

    @Test
    void testCalculateBrickwork_9InchWall() {
        BrickworkCalcRequest request = new BrickworkCalcRequest(
            10.0, 3.0, 0.23, 2.0, "1:6", 5.0
        );
        BrickworkCalcResult result = calculatorService.calculateBrickwork(request);

        assertNotNull(result);
        assertEquals(6.90, result.grossWallVolumeM3());
        assertEquals(0.46, result.deductionsVolumeM3());
        assertEquals(6.44, result.netWallVolumeM3());
        assertTrue(result.brickCount() >= 3380);
        assertTrue(result.cementBags() > 0);
        assertTrue(result.sandTonnes() > 0);
    }

    @Test
    void testCalculateExcavation_StandardPit() {
        ExcavationCalcRequest request = new ExcavationCalcRequest(
            10.0, 8.0, 2.0, 1.25, 0.85, 10.0
        );
        ExcavationCalcResult result = calculatorService.calculateExcavation(request);

        assertNotNull(result);
        assertEquals(160.0, result.pitVolumeM3());
        assertEquals(200.0, result.looseVolumeM3());
        assertEquals(136.0, result.compactedBackfillM3());
        assertEquals(20, result.truckTripsRequired()); // 200 / 10 = 20 trips
    }

    @Test
    void testCalculatePlaster_12mmInternal() {
        PlasterCalcRequest request = new PlasterCalcRequest(
            20.0, 3.0, 12.0, 0.0, "1:4", 5.0
        );
        PlasterCalcResult result = calculatorService.calculatePlaster(request);

        assertNotNull(result);
        assertEquals(60.0, result.surfaceAreaSqM());
        assertEquals(0.72, result.wetVolumeM3());
        assertTrue(result.dryVolumeM3() > 0.90);
        assertTrue(result.cementBags() > 5.0);
    }

    @Test
    void testCalculatePaint_TwoCoatsWithCeiling() {
        PaintCalcRequest request = new PaintCalcRequest(
            5.0, 4.0, 3.0, 4.0, true, 2, true, 10.0, 5.0
        );
        PaintCalcResult result = calculatorService.calculatePaint(request);

        assertNotNull(result);
        assertEquals(50.0, result.wallAreaSqM()); // 2 * (5+4) * 3 - 4 = 50
        assertEquals(20.0, result.ceilingAreaSqM()); // 5 * 4 = 20
        assertEquals(70.0, result.totalAreaSqM());
        assertTrue(result.paintLiters() > 0);
        assertTrue(result.primerLiters() > 0);
    }

    @Test
    void testConvertUnits_AllCategories() {
        // Linear: 1 meter to feet
        UnitConvertResult mToFt = calculatorService.convertUnits(
            new UnitConvertRequest("LINEAR", 1.0, "m", "ft")
        );
        assertNotNull(mToFt);
        assertEquals(3.2808, mToFt.toValue(), 0.001);

        // Area: 1 brass to sq.ft
        UnitConvertResult brassToSqFt = calculatorService.convertUnits(
            new UnitConvertRequest("AREA", 1.0, "brass", "sqft")
        );
        assertNotNull(brassToSqFt);
        assertEquals(100.0, brassToSqFt.toValue(), 0.01);

        // Mass: 1 tonne to kg
        UnitConvertResult tonneToKg = calculatorService.convertUnits(
            new UnitConvertRequest("MASS", 1.0, "tonne", "kg")
        );
        assertNotNull(tonneToKg);
        assertEquals(1000.0, tonneToKg.toValue(), 0.01);
    }
}
