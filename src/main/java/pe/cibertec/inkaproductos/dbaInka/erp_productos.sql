DROP DATABASE IF EXISTS erp_productos;
CREATE DATABASE erp_productos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE erp_productos;

-- ============================================================
-- 1. SEGURIDAD: ROL / USUARIO / USUARIO_ROLES
-- ============================================================

CREATE TABLE rol (
                     rol_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                     nombre   VARCHAR(50)     NOT NULL,
                     CONSTRAINT pk_rol    PRIMARY KEY (rol_id),
                     CONSTRAINT uq_rol_nm UNIQUE (nombre)
) ENGINE=InnoDB COMMENT='Roles de acceso al sistema (ADMIN, SUPERVISOR, USUARIO)';

CREATE TABLE usuario (
                         usuario_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                         nombre        VARCHAR(80)     NOT NULL,
                         email         VARCHAR(100)    NOT NULL,
                         password_hash VARCHAR(255)    NOT NULL,
                         enabled       TINYINT(1)      NOT NULL DEFAULT 1,
                         creado_en     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT pk_usuario    PRIMARY KEY (usuario_id),
                         CONSTRAINT uq_usuario_em UNIQUE (email)
) ENGINE=InnoDB COMMENT='Usuarios del sistema';

CREATE TABLE usuario_roles (
                               usuario_id BIGINT UNSIGNED NOT NULL,
                               rol_id     BIGINT UNSIGNED NOT NULL,
                               CONSTRAINT pk_usu_rol   PRIMARY KEY (usuario_id, rol_id),
                               CONSTRAINT fk_usu_rol_u FOREIGN KEY (usuario_id) REFERENCES usuario (usuario_id) ON DELETE CASCADE,
                               CONSTRAINT fk_usu_rol_r FOREIGN KEY (rol_id)     REFERENCES rol      (rol_id)     ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ============================================================
-- 2. CATÁLOGO: UNIDAD DE MEDIDA (UoM — patrón SAP)
-- ============================================================

CREATE TABLE unidad_medida (
                               uom_id      SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
                               codigo      VARCHAR(10)       NOT NULL   COMMENT 'Código ISO/SAP: KG, L, UND, SAC50, PACK6…',
                               descripcion VARCHAR(60)       NOT NULL,
                               CONSTRAINT pk_uom    PRIMARY KEY (uom_id),
                               CONSTRAINT uq_uom_cd UNIQUE (codigo)
) ENGINE=InnoDB COMMENT='Unidades de medida (UoM). Base para conversión futura.';

-- ============================================================
-- 3. CATÁLOGO: CATEGORÍA Y PRODUCTO
-- ============================================================

CREATE TABLE categoria (
                           categoria_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                           nombre       VARCHAR(80)  NOT NULL,
                           CONSTRAINT pk_cat    PRIMARY KEY (categoria_id),
                           CONSTRAINT uq_cat_nm UNIQUE (nombre)
) ENGINE=InnoDB COMMENT='Categorías de producto';

CREATE TABLE producto (
                          producto_id  INT UNSIGNED    NOT NULL AUTO_INCREMENT,
                          sku          VARCHAR(30)     NOT NULL,
                          nombre       VARCHAR(120)    NOT NULL,
                          descripcion  VARCHAR(250),
                          categoria_id INT UNSIGNED    NOT NULL,
                          uom_id       SMALLINT UNSIGNED NOT NULL  COMMENT 'Unidad de medida principal',
                          precio_lista DECIMAL(18,4)   NOT NULL,
                          activo       TINYINT(1)      NOT NULL DEFAULT 1,
                          CONSTRAINT pk_prod      PRIMARY KEY (producto_id),
                          CONSTRAINT uq_prod_sku  UNIQUE (sku),
                          CONSTRAINT fk_prod_cat  FOREIGN KEY (categoria_id) REFERENCES categoria     (categoria_id) ON DELETE RESTRICT,
                          CONSTRAINT fk_prod_uom  FOREIGN KEY (uom_id)       REFERENCES unidad_medida (uom_id)       ON DELETE RESTRICT,
                          CONSTRAINT ck_prod_prc  CHECK (precio_lista >= 0)
) ENGINE=InnoDB COMMENT='Maestro de productos';

CREATE TABLE auditoria_productos (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     producto_id INT UNSIGNED NOT NULL, -- CORREGIDO: Añadido UNSIGNED
                                     usuario_email VARCHAR(255) NOT NULL,
                                     accion VARCHAR(50) NOT NULL,
                                     detalles_cambio TEXT,
                                     fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
);
-- ============================================================
-- 4. ALMACENES
-- ============================================================

CREATE TABLE almacen (
                         almacen_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                         nombre     VARCHAR(80)  NOT NULL,
                         ciudad     VARCHAR(60)  NOT NULL,
                         direccion  VARCHAR(150),
                         activo     TINYINT(1)   NOT NULL DEFAULT 1,
                         CONSTRAINT pk_alm    PRIMARY KEY (almacen_id),
                         CONSTRAINT uq_alm_nm UNIQUE (nombre)
) ENGINE=InnoDB COMMENT='Almacenes / bodegas físicas';

-- ============================================================
-- 5. INVENTARIO — Foto actual del stock (saldo)
-- ============================================================

CREATE TABLE inventario (
                            almacen_id  INT UNSIGNED    NOT NULL,
                            producto_id INT UNSIGNED    NOT NULL,
                            cantidad    DECIMAL(18,4)   NOT NULL DEFAULT 0.0000,
                            actualizado DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT pk_inv       PRIMARY KEY (almacen_id, producto_id),
                            CONSTRAINT fk_inv_alm   FOREIGN KEY (almacen_id)  REFERENCES almacen  (almacen_id)  ON DELETE RESTRICT,
                            CONSTRAINT fk_inv_prod  FOREIGN KEY (producto_id) REFERENCES producto (producto_id) ON DELETE RESTRICT,
                            CONSTRAINT ck_inv_cant  CHECK (cantidad >= 0)
) ENGINE=InnoDB COMMENT='Stock actual por almacén (snapshot). Se actualiza vía trigger o servicio.';

-- ============================================================
-- 6. KARDEX — Libro diario de movimientos de stock
--    Cada fila es inmutable (append-only, auditoría blindada)
-- ============================================================

CREATE TABLE movimiento_stock (
                                  movimiento_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                  fecha              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  tipo_operacion     ENUM('ENTRADA','SALIDA','TRASLADO','AJUSTE','DEVOLUCION')
                                       NOT NULL,
                                  producto_id        INT UNSIGNED    NOT NULL,
                                  almacen_origen_id  INT UNSIGNED    NOT NULL  COMMENT 'En ENTRADAs puede ser el almacén proveedor/virtual',
                                  almacen_destino_id INT UNSIGNED   NOT NULL  COMMENT 'En SALIDAs puede ser el almacén cliente/virtual',
                                  stock_anterior     DECIMAL(18,4)   NOT NULL  COMMENT 'Stock en almacén afectado ANTES del movimiento',
                                  cantidad_movida    DECIMAL(18,4)   NOT NULL,
                                  stock_nuevo        DECIMAL(18,4)   NOT NULL  COMMENT 'Stock DESPUÉS del movimiento',
                                  usuario            VARCHAR(100)    NOT NULL  COMMENT 'email del responsable',
                                  referencia         VARCHAR(60)               COMMENT 'Nro de OC, guía de remisión, etc.',
                                  observacion        VARCHAR(255),
                                  CONSTRAINT pk_mov          PRIMARY KEY (movimiento_id),
                                  CONSTRAINT fk_mov_prod     FOREIGN KEY (producto_id)         REFERENCES producto (producto_id) ON DELETE RESTRICT,
                                  CONSTRAINT fk_mov_origen   FOREIGN KEY (almacen_origen_id)  REFERENCES almacen  (almacen_id)  ON DELETE RESTRICT,
                                  CONSTRAINT fk_mov_destino  FOREIGN KEY (almacen_destino_id) REFERENCES almacen  (almacen_id)  ON DELETE RESTRICT,
                                  CONSTRAINT ck_mov_cant     CHECK (cantidad_movida > 0)
) ENGINE=InnoDB COMMENT='Kardex: historial completo e inmutable de movimientos de stock';

-- Índices para consultas frecuentes (JPA + filtros de fecha/producto)
CREATE INDEX idx_mov_prod_fecha   ON movimiento_stock (producto_id, fecha);
CREATE INDEX idx_mov_alm_fecha    ON movimiento_stock (almacen_origen_id, fecha);
CREATE INDEX idx_mov_tipo         ON movimiento_stock (tipo_operacion);

-- ============================================================
-- 7. SOLICITUD DE COMPRA (respeta nombres Java existentes)
-- ============================================================

CREATE TABLE solicitud_compra (
                                  solicitud_id       INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                  fecha_solicitud    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  almacen_origen_id  INT UNSIGNED NOT NULL,
                                  almacen_destino_id INT UNSIGNED NOT NULL,
                                  usuario_solicitante VARCHAR(100) NOT NULL,
                                  estado              ENUM('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
                                  CONSTRAINT pk_sol       PRIMARY KEY (solicitud_id),
                                  CONSTRAINT fk_sol_orig  FOREIGN KEY (almacen_origen_id)  REFERENCES almacen (almacen_id) ON DELETE RESTRICT,
                                  CONSTRAINT fk_sol_dest  FOREIGN KEY (almacen_destino_id) REFERENCES almacen (almacen_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE solicitud_compra_detalle (
                                          detalle_id   INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                          solicitud_id INT UNSIGNED  NOT NULL,
                                          producto_id  INT UNSIGNED  NOT NULL,
                                          cantidad     DECIMAL(18,4) NOT NULL,
                                          CONSTRAINT pk_det      PRIMARY KEY (detalle_id),
                                          CONSTRAINT fk_det_sol  FOREIGN KEY (solicitud_id) REFERENCES solicitud_compra (solicitud_id) ON DELETE CASCADE,
                                          CONSTRAINT fk_det_prod FOREIGN KEY (producto_id)  REFERENCES producto          (producto_id)  ON DELETE RESTRICT,
                                          CONSTRAINT ck_det_cant CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- ============================================================
-- 8. MOVIMIENTO (tabla de traslados — respeta nombres Java)
-- ============================================================

CREATE TABLE movimiento (
                            id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
                            fecha      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            usuario    VARCHAR(100) NOT NULL,
                            origen_id  INT UNSIGNED NOT NULL,
                            destino_id INT UNSIGNED NOT NULL,
                            estado     ENUM('APROBADO','RECHAZADO','PENDIENTE') NOT NULL DEFAULT 'APROBADO',
                            CONSTRAINT pk_mov_trn      PRIMARY KEY (id),
                            CONSTRAINT fk_mov_trn_orig FOREIGN KEY (origen_id)  REFERENCES almacen (almacen_id) ON DELETE RESTRICT,
                            CONSTRAINT fk_mov_trn_dest FOREIGN KEY (destino_id) REFERENCES almacen (almacen_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE movimiento_detalle (
                                    id            INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                    movimiento_id INT UNSIGNED  NOT NULL,
                                    producto_id   INT UNSIGNED  NOT NULL,
                                    cantidad      DECIMAL(18,4) NOT NULL,
                                    CONSTRAINT pk_movdet       PRIMARY KEY (id),
                                    CONSTRAINT fk_movdet_mov   FOREIGN KEY (movimiento_id) REFERENCES movimiento (id)             ON DELETE CASCADE,
                                    CONSTRAINT fk_movdet_prod  FOREIGN KEY (producto_id)   REFERENCES producto   (producto_id)    ON DELETE RESTRICT,
                                    CONSTRAINT ck_movdet_cant  CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- ============================================================
-- 9. MENSAJES TI (respeta nombres Java existentes)
-- ============================================================

CREATE TABLE mensaje_ti (
                            mensaje_id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
                            emisor_email   VARCHAR(100) NOT NULL,
                            tipo_solicitud ENUM('NUEVO_PRODUCTO','ELIMINAR_USUARIO','EDITAR_STOCK','OTROS') NOT NULL,
                            prioridad      ENUM('BAJA','MEDIA','ALTA','URGENTE') NOT NULL DEFAULT 'MEDIA',
                            asunto         VARCHAR(150) NOT NULL,
                            contenido      TEXT         NOT NULL,
                            fecha_envio    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            fecha_atencion DATETIME,
                            estado         ENUM('PENDIENTE','EN_PROCESO','ATENDIDO') NOT NULL DEFAULT 'PENDIENTE',
                            version        INT          NOT NULL DEFAULT 0,
                            CONSTRAINT pk_msg PRIMARY KEY (mensaje_id)
) ENGINE=InnoDB;

-- ============================================================
-- ============================================================
--  DML — SEED DATA REAL (Mercado Peruano)
-- ============================================================
-- ============================================================

-- ── Roles (Modificados a: USUARIO, SUPERVISOR, ADMIN) ────────
INSERT INTO rol (nombre) VALUES ('USUARIO'), ('SUPERVISOR'), ('ADMIN');

-- ── Usuarios (Asignados a los nuevos roles) ──────────────────
INSERT INTO usuario (nombre, email, password_hash) VALUES
                                                       ('Administrador General', 'admin@inkaproductos.com',
                                                        '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe'),
                                                       ('Supervisor Almacén',     'supervisor@inkaproductos.com',
                                                        '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe'),
                                                       ('Operador Almacén',      'user@inkaproductos.com',
                                                        '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe');

-- Mapeo de usuario_id con rol_id (1=USUARIO, 2=SUPERVISOR, 3=ADMIN)
-- El admin tiene el id 1 y rol_id 3. El supervisor el id 2 y rol_id 2. El operador el id 3 y rol_id 1.
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1,3),(2,2),(3,1);

-- ── Unidades de Medida (UoM) ─────────────────────────────────
INSERT INTO unidad_medida (codigo, descripcion) VALUES
                                                    ('UND',   'Unidad'),
                                                    ('KG',    'Kilogramo'),
                                                    ('L',     'Litro'),
                                                    ('SAC50', 'Saco de 50 kg'),
                                                    ('SAC25', 'Saco de 25 kg'),
                                                    ('PACK6', 'Six-pack (6 unidades)'),
                                                    ('PACK12','Paquete de 12 unidades'),
                                                    ('CAJA',  'Caja'),
                                                    ('ATADO', 'Atado'),
                                                    ('GAL',   'Galón'),
                                                    ('BARRA', 'Barra / Varilla'),
                                                    ('BOL',   'Bolsa'),
                                                    ('LATA',  'Lata'),
                                                    ('ROLLO', 'Rollo');

-- ── Categorías ───────────────────────────────────────────────
INSERT INTO categoria (nombre) VALUES
                                   ('Electrónica'),    -- 1
                                   ('Alimentos'),      -- 2
                                   ('Ferretería'),     -- 3
                                   ('Oficina');        -- 4

-- ── Almacenes ────────────────────────────────────────────────
INSERT INTO almacen (nombre, ciudad, direccion) VALUES
                                                    ('Almacén Central',        'Lima',      'Av. Argentina 4545, Callao'),
                                                    ('Almacén Sur',            'Arequipa',  'Av. Ejército 305, Yanahuara'),
                                                    ('Almacén Norte',          'Trujillo',  'Jr. Independencia 1200, Centro'),
                                                    ('Almacén Callao',         'Callao',    'Jr. Loreto 880, Bellavista');

-- ── Productos ── (uom_id: 1=UND, 2=KG, 3=L, 4=SAC50, 5=SAC25,
--                          6=PACK6, 7=PACK12, 8=CAJA, 9=ATADO,
--                         10=GAL, 11=BARRA, 12=BOL, 13=LATA, 14=ROLLO)
-- ── ELECTRÓNICA ──────────────────────────────────────────────
INSERT INTO producto (sku,nombre,descripcion,categoria_id,uom_id,precio_lista) VALUES
                                                                                   ('ELEC-001','Laptop HP 14" Core i5',   'HP 14-DK1023DX, 8GB RAM, 256GB SSD',          1, 1, 2499.0000),
                                                                                   ('ELEC-002','Laptop Lenovo IdeaPad',   'Lenovo IdeaPad 3, 8GB RAM, 512GB SSD',         1, 1, 2199.0000),
                                                                                   ('ELEC-003','Celular Samsung A15',     'Android 14, 128GB, cámara triple',              1, 1,  699.0000),
                                                                                   ('ELEC-004','Celular Motorola G54',    'Android 13, 256GB, batería 5000mAh',            1, 1,  749.0000),
                                                                                   ('ELEC-005','Monitor LG 24" Full HD',  'IPS, 75Hz, entradas HDMI/VGA',                 1, 1,  489.0000),
                                                                                   ('ELEC-006','Mouse Logitech MX Master','Inalámbrico, ergonómico, recargable',          1, 1,   89.0000),
                                                                                   ('ELEC-007','Teclado Mecánico Redragon','Switch azul, retroiluminado RGB',             1, 1,  159.0000),
                                                                                   ('ELEC-008','Impresora Epson L3250',   'Ecotank multifuncional WiFi',                  1, 1,  699.0000),
                                                                                   ('ELEC-009','Router TP-Link AX3000',   'WiFi 6, dual band, 4 antenas',                 1, 1,  199.0000),
                                                                                   ('ELEC-010','USB Kingston 64GB',       'USB 3.2 Gen1, lectura 200MB/s',               1, 1,   29.0000);

-- ── ALIMENTOS ────────────────────────────────────────────────
INSERT INTO producto (sku,nombre,descripcion,categoria_id,uom_id,precio_lista) VALUES
                                                                                   ('ALIM-001','Arroz Costeño Extra Saco 50kg', 'Arroz blanco extra calidad certificada',  2, 4,  160.0000),
                                                                                   ('ALIM-002','Arroz Costeño Extra Kg',        'Fraccionado, bolsa 1kg',                   2, 2,    4.2000),
                                                                                   ('ALIM-003','Arroz Paisana Saco 25kg',       'Arroz corriente a granel',                 2, 5,   65.0000),
                                                                                   ('ALIM-004','Aceite Primor Botella 1L',      'Aceite vegetal de girasol refinado',      2, 3,    8.5000),
                                                                                   ('ALIM-005','Aceite Cocinero Garrafa 5L',    'Aceite mixto bidon familiar',              2, 3,   38.0000),
                                                                                   ('ALIM-006','Leche Gloria Evap. Six-pack',   'Lata 400g × 6, leche entera',             2, 6,   26.5000),
                                                                                   ('ALIM-007','Leche Gloria Evap. Lata',       'Lata individual 400g',                    2,13,    4.8000),
                                                                                   ('ALIM-008','Galletas Oreo Paquete 12u',     '12 paquetes × 39g c/u',                   2, 7,   18.0000),
                                                                                   ('ALIM-009','Galletas Soda Field Caja',      'Caja 630g display surtido',               2, 8,   12.5000),
                                                                                   ('ALIM-010','Fideos Don Vittorio Tallarin',  'Paquete 500g, pasta larga',               2,12,    3.8000),
                                                                                   ('ALIM-011','Azúcar Rubia Cartavio Kg',      'Azúcar rubia fraccionada 1kg',            2, 2,    3.6000),
                                                                                   ('ALIM-012','Azúcar Rubia Cartavio Saco 50', 'Saco 50kg para distribuidoras',           2, 4,  148.0000),
                                                                                   ('ALIM-013','Aceite Vegetal Palma 1L',       'Aceite de palma refinado exportación',   2, 3,    7.2000),
                                                                                   ('ALIM-014','Café Altomayo Molido 250g',     'Café peruano de altura, bolsa zip',      2,12,   14.5000),
                                                                                   ('ALIM-015','Atún Real en Aceite Lata',      'Lata 170g, atún aleta amarilla',          2,13,    5.9000);

-- ── FERRETERÍA ───────────────────────────────────────────────
INSERT INTO producto (sku,nombre,descripcion,categoria_id,uom_id,precio_lista) VALUES
                                                                                   ('FERR-001','Cemento Sol Bolsa 42.5kg',      'Cemento portland tipo I, 42.5kg',        3,12,   32.0000),
                                                                                   ('FERR-002','Fierro Corrugado 3/8" Varilla', 'Varilla de 9m, acero A615 G60',          3,11,   28.5000),
                                                                                   ('FERR-003','Fierro Corrugado 1/2" Varilla', 'Varilla de 9m, acero A615 G60',          3,11,   48.0000),
                                                                                   ('FERR-004','Pintura Tekno Látex Galón',     'Látex interior/exterior blanco hueso',   3,10,   62.0000),
                                                                                   ('FERR-005','Pintura Sherwin Esmalte Galón', 'Esmalte sintético brillante blanco',     3,10,   89.0000),
                                                                                   ('FERR-006','Thinner Acrílico 1L',           'Thinner industrial para esmaltes',       3, 3,   12.0000),
                                                                                   ('FERR-007','Taladro Bosch GSB 13 RE',       'Percutor 650W, maletín incluido',        3, 1,  269.0000),
                                                                                   ('FERR-008','Martillo Stanley 16oz',         'Mango fibra de vidrio antivibraciones', 3, 1,   38.0000),
                                                                                   ('FERR-009','Cinta Métrica Stanley 5m',      'Carcasa ABS, regla magnética',           3, 1,   22.0000),
                                                                                   ('FERR-010','Llave Francesa 12"',            'Acero cromo vanadio, mango antidesliz', 3, 1,   35.0000),
                                                                                   ('FERR-011','Clavos 2.5" Caja 1kg',          'Clavos de construcción brillantes',      3, 8,    8.5000),
                                                                                   ('FERR-012','Malla Raschel Rollo 4m×50m',   'Malla sombra 80%, verde',                3,14,  185.0000),
                                                                                   ('FERR-013','Tubo PVC 4" × 3m',              'PVC presión C-10, unión espiga',         3, 1,   28.0000),
                                                                                   ('FERR-014','Pala Bellota Hoja Cuadrada',    'Pala con mango de madera eucalipto',     3, 1,   52.0000),
                                                                                   ('FERR-015','Escalera Aluminio 8 peldaños',  'Tijera doble hoja, carga 120kg',         3, 1,  189.0000);

-- ── OFICINA ──────────────────────────────────────────────────
INSERT INTO producto (sku,nombre,descripcion,categoria_id,uom_id,precio_lista) VALUES
                                                                                   ('OFIC-001','Papel Atlas Bond A4 Resma',     'Resma 500h, 75g/m², blancura 91%',        4, 1,   22.5000),
                                                                                   ('OFIC-002','Lapicero BIC Cristal Caja',     'Caja 50 unidades, tinta azul',            4, 8,   18.0000),
                                                                                   ('OFIC-003','Folder Manila Paquete 25u',     'Tamaño A4, color manila',                 4, 7,   12.0000),
                                                                                   ('OFIC-004','Toner HP 85A (CE285A)',         'Toner original, rendimiento 1600 pág.',  4, 1,  159.0000),
                                                                                   ('OFIC-005','Archivador Palanca Ancho',      'Lomo 75mm, tapa dura kraft',              4, 1,   12.5000),
                                                                                   ('OFIC-006','Silla Ejecutiva Ergonómica',    'Malla transpirable, soporte lumbar',     4, 1,  389.0000),
                                                                                   ('OFIC-007','Escritorio Melamina 1.20m',     'Con cajones, tapa melamine 25mm',         4, 1,  399.0000),
                                                                                   ('OFIC-008','Pizarra Acrílica 120×80',       'Marco aluminio, incluye mota+plumón',    4, 1,  149.0000),
                                                                                   ('OFIC-009','Post-it 3M 76×76 mm Bloque',    'Bloque 100h, colores neón surtidos',     4, 1,    8.9000),
                                                                                   ('OFIC-010','Tijera Maped Essentials 21cm',  'Acero inox, mango ergonómico bicolor',  4, 1,    9.5000);

-- ── INVENTARIO INICIAL ───────────────────────────────────────
-- Lima (almacen_id=1): Electrónica + Alimentos
INSERT INTO inventario (almacen_id, producto_id, cantidad)
SELECT 1, producto_id,
       CASE
           WHEN sku LIKE 'ELEC%' THEN 50.0000
           WHEN sku LIKE 'ALIM%' THEN 200.0000
           END
FROM producto
WHERE sku LIKE 'ELEC%' OR sku LIKE 'ALIM%';

-- Arequipa (almacen_id=2): Alimentos + Ferretería
INSERT INTO inventario (almacen_id, producto_id, cantidad)
SELECT 2, producto_id,
       CASE
           WHEN sku LIKE 'ALIM%' THEN 150.0000
           WHEN sku LIKE 'FERR%' THEN 80.0000
           END
FROM producto
WHERE sku LIKE 'ALIM%' OR sku LIKE 'FERR%';

-- Trujillo (almacen_id=3): Ferretería + Oficina
INSERT INTO inventario (almacen_id, producto_id, cantidad)
SELECT 3, producto_id,
       CASE
           WHEN sku LIKE 'FERR%' THEN 60.0000
           WHEN sku LIKE 'OFIC%' THEN 120.0000
           END
FROM producto
WHERE sku LIKE 'FERR%' OR sku LIKE 'OFIC%';

-- Callao (almacen_id=4): Electrónica + Oficina (hub de exportación)
INSERT INTO inventario (almacen_id, producto_id, cantidad)
SELECT 4, producto_id,
       CASE
           WHEN sku LIKE 'ELEC%' THEN 30.0000
           WHEN sku LIKE 'OFIC%' THEN 90.0000
           END
FROM producto
WHERE sku LIKE 'ELEC%' OR sku LIKE 'OFIC%';

-- ── KARDEX INICIAL (movimientos de apertura) ─────────────────
-- Entrada inicial al Almacén Lima (tipo ENTRADA, stock_anterior=0)
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
SELECT
    'ENTRADA',
    producto_id,
    1,            -- origen = Lima (apertura desde el mismo almacén)
    1,            -- destino = Lima
    0.0000,
    CASE WHEN sku LIKE 'ELEC%' THEN 50.0000 ELSE 200.0000 END,
    CASE WHEN sku LIKE 'ELEC%' THEN 50.0000 ELSE 200.0000 END,
    'admin@inkaproductos.com',
    'OC-2024-001',
    'Stock inicial de apertura — Lima'
FROM producto
WHERE sku LIKE 'ELEC%' OR sku LIKE 'ALIM%';

-- Entrada inicial al Almacén Arequipa
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
SELECT
    'ENTRADA',
    producto_id,
    2, 2,
    0.0000,
    CASE WHEN sku LIKE 'ALIM%' THEN 150.0000 ELSE 80.0000 END,
    CASE WHEN sku LIKE 'ALIM%' THEN 150.0000 ELSE 80.0000 END,
    'admin@inkaproductos.com',
    'OC-2024-002',
    'Stock inicial de apertura — Arequipa'
FROM producto
WHERE sku LIKE 'ALIM%' OR sku LIKE 'FERR%';

-- ── MOVIMIENTOS DE EJEMPLO (TRASLADOS / SALIDAS) ─────────────

-- Traslado: 10 Laptops HP de Lima (1) → Callao (4)
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
VALUES
    ('TRASLADO',
     (SELECT producto_id FROM producto WHERE sku='ELEC-001'),
     1, 4,
     50.0000, 10.0000, 40.0000,
     'user@inkaproductos.com', 'GR-2024-0045', 'Traslado por demanda regional Callao');

-- Salida: 25 sacos de Arroz Costeño 50kg vendidos desde Lima
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
VALUES
    ('SALIDA',
     (SELECT producto_id FROM producto WHERE sku='ALIM-001'),
     1, 1,
     200.0000, 25.0000, 175.0000,
     'user@inkaproductos.com', 'PED-2024-0112', 'Venta a cliente mayorista Mercado Mayorista N°2');

-- Ajuste de inventario: 5 Tonners HP faltantes detectados en auditoría
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
VALUES
    ('AJUSTE',
     (SELECT producto_id FROM producto WHERE sku='OFIC-004'),
     3, 3,
     120.0000, 5.0000, 115.0000,
     'admin@inkaproductos.com', 'AUD-2024-003', 'Ajuste negativo por diferencia en auditoría física');

-- Devolución: 3 Taladros Bosch devueltos por cliente a Arequipa
INSERT INTO movimiento_stock
(tipo_operacion, producto_id, almacen_origen_id, almacen_destino_id,
 stock_anterior, cantidad_movida, stock_nuevo, usuario, referencia, observacion)
VALUES
    ('DEVOLUCION',
     (SELECT producto_id FROM producto WHERE sku='FERR-007'),
     2, 2,
     80.0000, 3.0000, 83.0000,
     'user@inkaproductos.com', 'DEV-2024-008', 'Devolución cliente por garantía — unidades revisadas');


-- ============================================================
-- ============================================================
--  QUERIES DE AUDITORÍA (KARDEX)
-- ============================================================
-- ============================================================

-- ──────────────────────────────────────────────────────────────
-- QUERY 1: Kardex completo de un producto en todos los almacenes
-- ──────────────────────────────────────────────────────────────
SELECT
    ms.movimiento_id,
    ms.fecha,
    ms.tipo_operacion,
    p.sku,
    p.nombre                         AS producto,
    u.codigo                         AS uom,
    ao.nombre                        AS almacen_origen,
    ad.nombre                        AS almacen_destino,
    ms.stock_anterior,
    ms.cantidad_movida,
    ms.stock_nuevo,
    ms.usuario,
    ms.referencia,
    ms.observacion
FROM movimiento_stock ms
         JOIN producto     p  ON p.producto_id       = ms.producto_id
         JOIN unidad_medida u ON u.uom_id            = p.uom_id
         JOIN almacen      ao ON ao.almacen_id       = ms.almacen_origen_id
         JOIN almacen      ad ON ad.almacen_id       = ms.almacen_destino_id
WHERE p.sku = 'ALIM-001'
ORDER BY ms.fecha ASC;

-- ──────────────────────────────────────────────────────────────
-- QUERY 2: Kardex por almacén — historial completo Almacén Lima
-- ──────────────────────────────────────────────────────────────
SELECT
    ms.movimiento_id,
    ms.fecha,
    ms.tipo_operacion,
    p.sku,
    p.nombre                         AS producto,
    cat.nombre                       AS categoria,
    u.codigo                         AS uom,
    ms.stock_anterior,
    ms.cantidad_movida,
    ms.stock_nuevo,
    ms.usuario,
    ms.referencia
FROM movimiento_stock ms
         JOIN producto      p   ON p.producto_id  = ms.producto_id
         JOIN categoria     cat ON cat.categoria_id = p.categoria_id
         JOIN unidad_medida u   ON u.uom_id       = p.uom_id
         JOIN almacen       ao  ON ao.almacen_id  = ms.almacen_origen_id
WHERE ao.nombre = 'Almacén Central'
ORDER BY ms.fecha DESC;

-- ──────────────────────────────────────────────────────────────
-- QUERY 3: Resumen de stock actual con categoría, UoM y valor
-- ──────────────────────────────────────────────────────────────
SELECT
    a.nombre                                    AS almacen,
    cat.nombre                                  AS categoria,
    p.sku,
    p.nombre                                    AS producto,
    u.codigo                                    AS uom,
    i.cantidad                                  AS stock_actual,
    p.precio_lista,
    ROUND(i.cantidad * p.precio_lista, 4)       AS valor_inventario,
    i.actualizado
FROM inventario    i
         JOIN almacen       a   ON a.almacen_id   = i.almacen_id
         JOIN producto      p   ON p.producto_id  = i.producto_id
         JOIN categoria     cat ON cat.categoria_id = p.categoria_id
         JOIN unidad_medida u   ON u.uom_id       = p.uom_id
WHERE p.activo = 1
ORDER BY a.nombre, cat.nombre, p.sku;

-- ──────────────────────────────────────────────────────────────
-- QUERY 4: Detección de inconsistencias Kardex vs Inventario
-- ──────────────────────────────────────────────────────────────
SELECT
    a.nombre                                AS almacen,
    p.sku,
    p.nombre                                AS producto,
    i.cantidad                              AS stock_tabla_inventario,
    ultimo_mov.stock_nuevo                  AS ultimo_stock_kardex,
    (i.cantidad - ultimo_mov.stock_nuevo)   AS diferencia
FROM inventario i
         JOIN producto p ON p.producto_id = i.producto_id
         JOIN almacen  a ON a.almacen_id  = i.almacen_id
         JOIN (
    SELECT
        ms1.producto_id,
        ms1.almacen_destino_id AS almacen_id,
        ms1.stock_nuevo
    FROM movimiento_stock ms1
    WHERE ms1.movimiento_id = (
        SELECT MAX(ms2.movimiento_id)
        FROM movimiento_stock ms2
        WHERE ms2.producto_id = ms1.producto_id
          AND ms2.almacen_destino_id = ms1.almacen_destino_id
    )
) ultimo_mov ON ultimo_mov.producto_id = i.producto_id
    AND ultimo_mov.almacen_id   = i.almacen_id
HAVING diferencia <> 0
ORDER BY ABS(diferencia) DESC;