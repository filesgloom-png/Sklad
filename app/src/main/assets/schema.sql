-- Облік-Склад: фактична схема локальної SQLite бази.
-- Реальних складських даних у репозиторії немає.

CREATE TABLE responsible_persons (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  full_name TEXT NOT NULL,
  position TEXT DEFAULT '',
  phone TEXT DEFAULT '',
  note TEXT DEFAULT ''
);

CREATE TABLE warehouses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  address TEXT DEFAULT '',
  note TEXT DEFAULT '',
  responsible_person_id INTEGER,
  FOREIGN KEY (responsible_person_id) REFERENCES responsible_persons(id)
);

CREATE TABLE storage_locations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  warehouse_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  note TEXT DEFAULT '',
  FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE materials (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nsn TEXT DEFAULT '',
  nomenclature_no TEXT DEFAULT '',
  name TEXT NOT NULL,
  unit TEXT NOT NULL,
  batch TEXT DEFAULT '',
  price REAL DEFAULT 0,
  note TEXT DEFAULT ''
);

CREATE TABLE movements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  material_id INTEGER NOT NULL,
  type TEXT NOT NULL CHECK(type IN ('RECEIPT','ISSUE','TRANSFER_OUT','TRANSFER_IN','WRITE_OFF')),
  quantity REAL NOT NULL CHECK(quantity > 0),
  from_warehouse_id INTEGER,
  to_warehouse_id INTEGER,
  document_no TEXT DEFAULT '',
  movement_date TEXT NOT NULL,
  note TEXT DEFAULT '',
  FOREIGN KEY (material_id) REFERENCES materials(id),
  FOREIGN KEY (from_warehouse_id) REFERENCES warehouses(id),
  FOREIGN KEY (to_warehouse_id) REFERENCES warehouses(id)
);

CREATE INDEX idx_mov_material ON movements(material_id);
CREATE INDEX idx_mov_date ON movements(movement_date);
CREATE INDEX idx_mov_from_warehouse ON movements(from_warehouse_id);
CREATE INDEX idx_mov_to_warehouse ON movements(to_warehouse_id);
