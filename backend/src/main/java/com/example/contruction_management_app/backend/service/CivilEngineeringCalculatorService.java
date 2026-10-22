package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CivilEngineeringCalculatorService {

    // =========================================================================
    // 1. Concrete Calculator (IS 456 compliant)
    // =========================================================================
    public ConcreteCalcResult calculateConcrete(ConcreteCalcRequest request) {
        double length = request.lengthM();
        double width = request.widthM();
        double depth = request.depthM();
        String grade = (request.grade() != null && !request.grade().isBlank())
            ? request.grade().trim().toUpperCase(Locale.ROOT)
            : "M20";
        double wastage = (request.wastagePct() != null && request.wastagePct() >= 0)
            ? request.wastagePct()
            : 5.0;

        // Grade ratios: Cement : Sand : Coarse Aggregate
        double cementPart = 1.0;
        double sandPart;
        double aggPart;
        String mixRatio;

        switch (grade) {
            case "M10" -> {
                sandPart = 3.0;
                aggPart = 6.0;
                mixRatio = "1:3:6";
            }
            case "M15" -> {
                sandPart = 2.0;
                aggPart = 4.0;
                mixRatio = "1:2:4";
            }
            case "M25" -> {
                sandPart = 1.0;
                aggPart = 2.0;
                mixRatio = "1:1:2";
            }
            case "M20" -> {
                sandPart = 1.5;
                aggPart = 3.0;
                mixRatio = "1:1.5:3";
            }
            default -> {
                sandPart = 1.5;
                aggPart = 3.0;
                mixRatio = "1:1.5:3";
                grade = "M20";
            }
        }

        double totalParts = cementPart + sandPart + aggPart;

        double wetVolume = length * width * depth;
        // Dry volume factor is 1.54 (IS 456 standard for concrete dry shrinkage)
        double dryVolume = wetVolume * 1.54 * (1.0 + (wastage / 100.0));

        double cementVol = dryVolume * (cementPart / totalParts);
        // Bulk density of cement = 1440 kg/m³; 1 bag = 50 kg
        double cementKg = cementVol * 1440.0;
        double cementBags = cementKg / 50.0;

        // Sand (coarse sand density ~ 1600 kg/m³ = 1.6 tonnes/m³)
        double sandVol = dryVolume * (sandPart / totalParts);
        double sandTonnes = sandVol * 1.60;
        double sandCft = sandVol * 35.3147;

        // Coarse aggregate (density ~ 1500 kg/m³ = 1.5 tonnes/m³)
        double aggVol = dryVolume * (aggPart / totalParts);
        double aggTonnes = aggVol * 1.50;
        double aggCft = aggVol * 35.3147;

        // Water-cement ratio ~ 0.50 (i.e. 25 L per 50 kg bag)
        double waterLiters = cementKg * 0.50;

        return new ConcreteCalcResult(
            round2(wetVolume),
            round2(dryVolume),
            grade,
            mixRatio,
            round2(cementBags),
            round2(cementKg),
            round2(sandTonnes),
            round2(sandCft),
            round2(aggTonnes),
            round2(aggCft),
            round2(waterLiters),
            round2(wastage)
        );
    }

    // =========================================================================
    // 2. Steel Rebar Calculator (IS 1786 / SP 34 compliant)
    // =========================================================================
    public SteelCalcResult calculateSteel(SteelCalcRequest request) {
        double d = request.diameterMm();
        double length = request.lengthM();
        int count = request.numberOfBars();
        double wastage = (request.wastagePct() != null && request.wastagePct() >= 0)
            ? request.wastagePct()
            : 3.0;

        // Standard unit weight formula: W = (D^2 / 162.0) kg/m
        double unitWeight = (d * d) / 162.0;
        double totalLength = length * count;
        double rawWeight = unitWeight * totalLength;
        double totalWeightKg = rawWeight * (1.0 + (wastage / 100.0));
        double totalWeightTonnes = totalWeightKg / 1000.0;

        // Standard bundle counts per commercial 12m delivery
        int barsPerBundle;
        if (d <= 8.0) {
            barsPerBundle = 12;
        } else if (d <= 10.0) {
            barsPerBundle = 8;
        } else if (d <= 12.0) {
            barsPerBundle = 6;
        } else if (d <= 16.0) {
            barsPerBundle = 4;
        } else if (d <= 20.0) {
            barsPerBundle = 2;
        } else {
            barsPerBundle = 1;
        }
        int bundleCount = (int) Math.ceil((double) count / barsPerBundle);

        return new SteelCalcResult(
            round2(d),
            round2(length),
            count,
            round2(totalLength),
            round3(unitWeight),
            round2(rawWeight),
            round2(totalWeightKg),
            round3(totalWeightTonnes),
            bundleCount,
            round2(wastage)
        );
    }

    // =========================================================================
    // 3. Brickwork Masonry Calculator (IS 2212 / CPWD compliant)
    // =========================================================================
    public BrickworkCalcResult calculateBrickwork(BrickworkCalcRequest request) {
        double length = request.lengthM();
        double height = request.heightM();
        double thickness = request.thicknessM();
        double openings = (request.openingsAreaSqM() != null && request.openingsAreaSqM() >= 0)
            ? request.openingsAreaSqM()
            : 0.0;
        double wastage = (request.wastagePct() != null && request.wastagePct() >= 0)
            ? request.wastagePct()
            : 5.0;
        String ratio = (request.mortarRatio() != null && !request.mortarRatio().isBlank())
            ? request.mortarRatio().trim()
            : "1:6";

        double grossVolume = length * height * thickness;
        double deductionsVolume = openings * thickness;
        double netVolume = Math.max(0.0, grossVolume - deductionsVolume);

        // Standard modular masonry: 500 bricks per m³ of finished wall
        double brickCount = Math.ceil(netVolume * 500.0 * (1.0 + (wastage / 100.0)));

        // Wet mortar is ~30% of masonry volume; dry factor is 1.33 for voids & absorption
        double wetMortar = netVolume * 0.30;
        double dryMortar = wetMortar * 1.33 * (1.0 + (wastage / 100.0));

        double cementPart = 1.0;
        double sandPart = ratio.equals("1:4") ? 4.0 : 6.0;
        double sumParts = cementPart + sandPart;

        double cementVol = dryMortar * (cementPart / sumParts);
        double cementKg = cementVol * 1440.0;
        double cementBags = cementKg / 50.0;

        double sandVol = dryMortar * (sandPart / sumParts);
        double sandTonnes = sandVol * 1.60;
        double sandCft = sandVol * 35.3147;

        return new BrickworkCalcResult(
            round2(grossVolume),
            round2(deductionsVolume),
            round2(netVolume),
            brickCount,
            round2(dryMortar),
            ratio,
            round2(cementBags),
            round2(cementKg),
            round2(sandTonnes),
            round2(sandCft),
            round2(wastage)
        );
    }

    // =========================================================================
    // 4. Excavation & Earthwork Calculator (IS 1200 Part 1 compliant)
    // =========================================================================
    public ExcavationCalcResult calculateExcavation(ExcavationCalcRequest request) {
        double length = request.lengthM();
        double width = request.widthM();
        double depth = request.depthM();
        double bulking = (request.bulkingFactor() != null && request.bulkingFactor() > 1.0)
            ? request.bulkingFactor()
            : 1.25;
        double compaction = (request.compactionFactor() != null && request.compactionFactor() > 0)
            ? request.compactionFactor()
            : 0.85;
        double truckCap = (request.truckCapacityM3() != null && request.truckCapacityM3() > 0)
            ? request.truckCapacityM3()
            : 10.0;

        double pitVolume = length * width * depth;
        double looseVolume = pitVolume * bulking;
        double compactedBackfill = pitVolume * compaction;
        int truckTrips = (int) Math.ceil(looseVolume / truckCap);

        return new ExcavationCalcResult(
            round2(pitVolume),
            round2(looseVolume),
            round2(bulking),
            round2(compactedBackfill),
            round2(compaction),
            round2(truckCap),
            truckTrips
        );
    }

    // =========================================================================
    // 5. Plastering Calculator (IS 1661 / CPWD compliant)
    // =========================================================================
    public PlasterCalcResult calculatePlaster(PlasterCalcRequest request) {
        double length = request.lengthM();
        double height = request.heightM();
        double thicknessMm = request.thicknessMm();
        double openings = (request.openingsAreaSqM() != null && request.openingsAreaSqM() >= 0)
            ? request.openingsAreaSqM()
            : 0.0;
        double wastage = (request.wastagePct() != null && request.wastagePct() >= 0)
            ? request.wastagePct()
            : 5.0;
        String ratio = (request.mortarRatio() != null && !request.mortarRatio().isBlank())
            ? request.mortarRatio().trim()
            : "1:4";

        double area = Math.max(0.0, (length * height) - openings);
        double wetVolume = area * (thicknessMm / 1000.0);
        // Dry volume factor for plaster is 1.30 (filling masonry joints, uneven surfaces, voids)
        double dryVolume = wetVolume * 1.30 * (1.0 + (wastage / 100.0));

        double cementPart = 1.0;
        double sandPart = ratio.equals("1:6") ? 6.0 : (ratio.equals("1:3") ? 3.0 : 4.0);
        double sumParts = cementPart + sandPart;

        double cementVol = dryVolume * (cementPart / sumParts);
        double cementKg = cementVol * 1440.0;
        double cementBags = cementKg / 50.0;

        double sandVol = dryVolume * (sandPart / sumParts);
        double sandTonnes = sandVol * 1.60;
        double sandCft = sandVol * 35.3147;

        return new PlasterCalcResult(
            round2(area),
            round2(wetVolume),
            round2(dryVolume),
            ratio,
            round2(cementBags),
            round2(cementKg),
            round2(sandTonnes),
            round2(sandCft),
            round2(wastage)
        );
    }

    // =========================================================================
    // 6. Paint & Primer Calculator (CPWD DSR compliant)
    // =========================================================================
    public PaintCalcResult calculatePaint(PaintCalcRequest request) {
        double length = request.lengthM();
        double width = request.widthM();
        double height = request.heightM();
        double openings = (request.openingsAreaSqM() != null && request.openingsAreaSqM() >= 0)
            ? request.openingsAreaSqM()
            : 0.0;
        boolean incCeiling = request.includeCeiling() == null || request.includeCeiling();
        int coats = (request.numberOfCoats() != null && request.numberOfCoats() > 0)
            ? request.numberOfCoats()
            : 2;
        boolean incPrimer = request.includePrimer() == null || request.includePrimer();
        double coverage = (request.coverageRateSqMPerLiter() != null && request.coverageRateSqMPerLiter() > 0)
            ? request.coverageRateSqMPerLiter()
            : 10.0;
        double wastage = (request.wastagePct() != null && request.wastagePct() >= 0)
            ? request.wastagePct()
            : 5.0;

        double wallArea = Math.max(0.0, (2.0 * (length + width) * height) - openings);
        double ceilingArea = incCeiling ? (length * width) : 0.0;
        double totalArea = wallArea + ceilingArea;

        // Primer rate: ~12.0 sq.m per liter
        double primerLiters = incPrimer
            ? (totalArea / 12.0) * (1.0 + (wastage / 100.0))
            : 0.0;

        // Paint liters across coats
        double paintLiters = ((totalArea * coats) / coverage) * (1.0 + (wastage / 100.0));

        // Pack sizes: 20L drum, 4L tin, 1L can
        int cans20L = (int) (paintLiters / 20.0);
        double remAfter20 = paintLiters - (cans20L * 20.0);
        int cans4L = (int) (remAfter20 / 4.0);
        double remAfter4 = remAfter20 - (cans4L * 4.0);
        int cans1L = (int) Math.ceil(remAfter4);

        return new PaintCalcResult(
            round2(wallArea),
            round2(ceilingArea),
            round2(totalArea),
            coats,
            round2(primerLiters),
            round2(paintLiters),
            cans20L,
            cans4L,
            cans1L,
            round2(wastage)
        );
    }

    // =========================================================================
    // 7. Universal Construction Unit Converter
    // =========================================================================
    public UnitConvertResult convertUnits(UnitConvertRequest request) {
        String category = request.category().trim().toUpperCase(Locale.ROOT);
        double val = request.value();
        String from = request.fromUnit().trim().toUpperCase(Locale.ROOT);
        String to = request.toUnit().trim().toUpperCase(Locale.ROOT);

        Map<String, Double> allMap = new LinkedHashMap<>();
        double toValue = 0.0;
        String formula = "";

        switch (category) {
            case "LINEAR" -> {
                // Base unit: METERS (m)
                double baseMeters = switch (from) {
                    case "M", "METER", "METERS" -> val;
                    case "FT", "FEET", "FOOT" -> val * 0.3048;
                    case "IN", "INCH", "INCHES" -> val * 0.0254;
                    case "MM", "MILLIMETER", "MILLIMETERS" -> val * 0.001;
                    case "CM", "CENTIMETER", "CENTIMETERS" -> val * 0.01;
                    case "YD", "YARD", "YARDS" -> val * 0.9144;
                    default -> val;
                };
                allMap.put("m", round4(baseMeters));
                allMap.put("ft", round4(baseMeters / 0.3048));
                allMap.put("in", round4(baseMeters / 0.0254));
                allMap.put("mm", round4(baseMeters / 0.001));
                allMap.put("cm", round4(baseMeters / 0.01));
                allMap.put("yd", round4(baseMeters / 0.9144));
                toValue = switch (to) {
                    case "M", "METER", "METERS" -> baseMeters;
                    case "FT", "FEET", "FOOT" -> baseMeters / 0.3048;
                    case "IN", "INCH", "INCHES" -> baseMeters / 0.0254;
                    case "MM", "MILLIMETER", "MILLIMETERS" -> baseMeters / 0.001;
                    case "CM", "CENTIMETER", "CENTIMETERS" -> baseMeters / 0.01;
                    case "YD", "YARD", "YARDS" -> baseMeters / 0.9144;
                    default -> baseMeters;
                };
                formula = val + " " + from + " -> " + round4(toValue) + " " + to;
            }
            case "AREA" -> {
                // Base unit: SQUARE METERS (m²)
                double baseSqM = switch (from) {
                    case "M2", "SQM", "SQ_METER" -> val;
                    case "SQFT", "FT2", "SQ_FEET" -> val * 0.092903;
                    case "BRASS" -> val * 9.2903; // 1 Brass Area = 100 sq.ft = 9.2903 m²
                    case "ACRE" -> val * 4046.86;
                    case "GUNTHA" -> val * 101.171; // 1 Guntha = 1089 sq.ft
                    case "HECTARE", "HA" -> val * 10000.0;
                    default -> val;
                };
                allMap.put("m²", round4(baseSqM));
                allMap.put("sq.ft", round4(baseSqM / 0.092903));
                allMap.put("brass", round4(baseSqM / 9.2903));
                allMap.put("acre", round4(baseSqM / 4046.86));
                allMap.put("guntha", round4(baseSqM / 101.171));
                allMap.put("hectare", round4(baseSqM / 10000.0));
                toValue = switch (to) {
                    case "M2", "SQM", "SQ_METER" -> baseSqM;
                    case "SQFT", "FT2", "SQ_FEET" -> baseSqM / 0.092903;
                    case "BRASS" -> baseSqM / 9.2903;
                    case "ACRE" -> baseSqM / 4046.86;
                    case "GUNTHA" -> baseSqM / 101.171;
                    case "HECTARE", "HA" -> baseSqM / 10000.0;
                    default -> baseSqM;
                };
                formula = val + " " + from + " -> " + round4(toValue) + " " + to;
            }
            case "VOLUME" -> {
                // Base unit: CUBIC METERS (m³)
                double baseCuM = switch (from) {
                    case "M3", "CUM", "CU_METER" -> val;
                    case "CFT", "FT3", "CU_FEET" -> val * 0.0283168;
                    case "BRASS" -> val * 2.83168; // 1 Brass Vol = 100 cu.ft = 2.83168 m³
                    case "L", "LITER", "LITERS" -> val * 0.001;
                    case "GAL", "GALLON", "GALLONS" -> val * 0.00378541;
                    default -> val;
                };
                allMap.put("m³", round4(baseCuM));
                allMap.put("cft", round4(baseCuM / 0.0283168));
                allMap.put("brass", round4(baseCuM / 2.83168));
                allMap.put("liters", round4(baseCuM / 0.001));
                allMap.put("gallons", round4(baseCuM / 0.00378541));
                toValue = switch (to) {
                    case "M3", "CUM", "CU_METER" -> baseCuM;
                    case "CFT", "FT3", "CU_FEET" -> baseCuM / 0.0283168;
                    case "BRASS" -> baseCuM / 2.83168;
                    case "L", "LITER", "LITERS" -> baseCuM / 0.001;
                    case "GAL", "GALLON", "GALLONS" -> baseCuM / 0.00378541;
                    default -> baseCuM;
                };
                formula = val + " " + from + " -> " + round4(toValue) + " " + to;
            }
            case "MASS" -> {
                // Base unit: KILOGRAMS (kg)
                double baseKg = switch (from) {
                    case "KG", "KILOGRAM", "KILOGRAMS" -> val;
                    case "TONNE", "TONNES", "MT", "T" -> val * 1000.0;
                    case "QUINTAL", "Q" -> val * 100.0;
                    case "LBS", "POUNDS" -> val * 0.45359237;
                    default -> val;
                };
                allMap.put("kg", round4(baseKg));
                allMap.put("tonne", round4(baseKg / 1000.0));
                allMap.put("quintal", round4(baseKg / 100.0));
                allMap.put("lbs", round4(baseKg / 0.45359237));
                toValue = switch (to) {
                    case "KG", "KILOGRAM", "KILOGRAMS" -> baseKg;
                    case "TONNE", "TONNES", "MT", "T" -> baseKg / 1000.0;
                    case "QUINTAL", "Q" -> baseKg / 100.0;
                    case "LBS", "POUNDS" -> baseKg / 0.45359237;
                    default -> baseKg;
                };
                formula = val + " " + from + " -> " + round4(toValue) + " " + to;
            }
            default -> {
                toValue = val;
                allMap.put(to, val);
                formula = "1:1 mapping";
            }
        }

        return new UnitConvertResult(
            category,
            val,
            from,
            round4(toValue),
            to,
            formula,
            allMap
        );
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private double round4(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
