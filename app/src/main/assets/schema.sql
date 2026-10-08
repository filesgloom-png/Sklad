-- Облік-Склад: логічна схема локальної бази.
-- УВАГА: не містить реальних складських даних.

CREATE TABLE warehouses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT UNIQUE,
  name TEXT NOT NULL,
  responsible_person_id INTEGER,
  address TEXT,
  note TEXT,
  created_at TEXT,
  FOREIGN KEY (responsible_person_id) REFERENCES responsible_persons(id)
);

CREATE TABLE responsible_persons (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  full_name TEXT NOT NULL,
  position TEXT,
  phone TEXT,
  note TEXT
);

CREATE TABLE storage_locations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  warehouse_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  note TEXT,
  FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE materials (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nsn_code TEXT,
  nomenclature_number TEXT,
  name TEXT NOT NULL,
  batch TEXT,
  unit TEXT NOT NULL,
  price REAL NOT NULL DEFAULT 0
);

CREATE TABLE movements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  material_id INTEGER NOT NULL,
  warehouse_id INTEGER,
  location_id INTEGER,
  from_warehouse_id INTEGER,
  to_warehouse_id INTEGER,
  type TEXT NOT NULL CHECK(type IN ('RECEIPT','ISSUE','TRANSFER_OUT','TRANSFER_IN','WRITE_OFF')),
  quantity REAL NOT NULL CHECK(quantity > 0),
  document_number TEXT,
  note TEXT,
  movement_date TEXT NOT NULL,
  FOREIGN KEY (material_id) REFERENCES materials(id),
  FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
  FOREIGN KEY (location_id) REFERENCES storage_locations(id),
  FOREIGN KEY (from_warehouse_id) REFERENCES warehouses(id),
  FOREIGN KEY (to_warehouse_id) REFERENCES warehouses(id)
);

CREATE INDEX idx_movements_material ON movements(material_id);
CREATE INDEX idx_movements_warehouse ON movements(warehouse_id);
CREATE INDEX idx_movements_from_warehouse ON movements(from_warehouse_id);
CREATE INDEX idx_movements_to_warehouse ON movements(to_warehouse_id);
CREATE INDEX idx_movements_date ON movements(movement_date);
