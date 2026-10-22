-- =====================================================================
-- V3__add_calculations.sql: Civil Engineering Calculations & History
-- =====================================================================

CREATE TABLE IF NOT EXISTS calculations (
    id VARCHAR(50) PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    calculator_type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    element_type VARCHAR(64),
    input_parameters TEXT NOT NULL,
    output_results TEXT NOT NULL,
    primary_quantity DOUBLE PRECISION NOT NULL,
    primary_unit VARCHAR(20) NOT NULL,
    created_by VARCHAR(50) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_calculations_project ON calculations(project_id);
CREATE INDEX IF NOT EXISTS idx_calculations_type ON calculations(calculator_type);
CREATE INDEX IF NOT EXISTS idx_calculations_created_at ON calculations(created_at DESC);

-- Seed Sample Calculations for Green Valley Residency
INSERT INTO calculations (id, project_id, calculator_type, title, element_type, input_parameters, output_results, primary_quantity, primary_unit, created_by, created_at) VALUES
(
    'calc_gvr_01',
    'prj_green_valley_residency',
    'CONCRETE',
    'Tower A Ground Floor Slab M25 Concrete',
    'SLAB',
    '{"length":24.0,"width":14.0,"depth":0.15,"grade":"M25","wastagePct":5.0,"mixRatio":"1:1:2"}',
    '{"wetVolumeM3":50.40,"dryVolumeM3":81.50,"cementBags":586,"sandTonnes":47.28,"aggregateTonnes":88.65,"waterLiters":12600}',
    50.40,
    'm³',
    'usr_engineer',
    '2026-03-20 10:30:00+00'
),
(
    'calc_gvr_02',
    'prj_green_valley_residency',
    'STEEL',
    'Tower A Ground Floor Columns 20mm Main Rebar',
    'COLUMN',
    '{"diameterMm":20.0,"lengthM":12.0,"numberOfBars":48,"wastagePct":3.0}',
    '{"unitWeightKgPerM":2.469,"totalWeightKg":1464.6,"bundleCount":12}',
    1464.60,
    'kg',
    'usr_engineer',
    '2026-03-22 14:15:00+00'
),
(
    'calc_gvr_03',
    'prj_green_valley_residency',
    'BRICKWORK',
    'Perimeter 9-inch Brick Masonry Wall (Block A)',
    'WALL',
    '{"length":30.0,"height":3.0,"thickness":0.23,"mortarRatio":"1:6","wastagePct":5.0}',
    '{"wallVolumeM3":20.70,"brickCount":10350,"cementBags":21,"sandTonnes":4.45}',
    10350.0,
    'bricks',
    'usr_supervisor',
    '2026-04-02 09:45:00+00'
)
ON CONFLICT (id) DO NOTHING;
