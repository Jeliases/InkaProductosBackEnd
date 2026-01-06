DROP DATABASE IF EXISTS erp_productos;
CREATE DATABASE erp_productos CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE erp_productos;

-- 1. ESTRUCTURA DE SEGURIDAD (ADMIN, USER, TI)
CREATE TABLE rol (
    rol_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (rol_id)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    usuario_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (usuario_id)
) ENGINE=InnoDB;

CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usu_rol_u FOREIGN KEY (usuario_id) REFERENCES usuario (usuario_id) ON DELETE CASCADE,
    CONSTRAINT fk_usu_rol_r FOREIGN KEY (rol_id) REFERENCES rol (rol_id)
) ENGINE=InnoDB;

-- 2. CATÁLOGOS
CREATE TABLE categoria (
    categoria_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    PRIMARY KEY (categoria_id)
) ENGINE=InnoDB;

CREATE TABLE producto (
    producto_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    sku VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(250),
    categoria_id INT UNSIGNED NOT NULL,
    precio_lista DECIMAL(18,2) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (producto_id),
    CONSTRAINT fk_prod_cat FOREIGN KEY (categoria_id) REFERENCES categoria (categoria_id)
) ENGINE=InnoDB;

CREATE TABLE almacen (
    almacen_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    direccion VARCHAR(100),
    PRIMARY KEY (almacen_id)
) ENGINE=InnoDB;

-- 3. INVENTARIO Y COMPRAS
CREATE TABLE inventario (
    almacen_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    cantidad DECIMAL(18,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (almacen_id, producto_id),
    CONSTRAINT fk_inv_alm FOREIGN KEY (almacen_id) REFERENCES almacen (almacen_id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_prod FOREIGN KEY (producto_id) REFERENCES producto (producto_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE solicitud_compra (
    solicitud_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    almacen_origen_id INT UNSIGNED NOT NULL,
    almacen_destino_id INT UNSIGNED NOT NULL,
    usuario_solicitante VARCHAR(60) NOT NULL,
    estado ENUM('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (solicitud_id),
    CONSTRAINT fk_sol_alm_orig FOREIGN KEY (almacen_origen_id) REFERENCES almacen (almacen_id),
    CONSTRAINT fk_sol_alm_dest FOREIGN KEY (almacen_destino_id) REFERENCES almacen (almacen_id)
) ENGINE=InnoDB;

CREATE TABLE solicitud_compra_detalle (
    detalle_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    solicitud_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    cantidad DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (detalle_id),
    CONSTRAINT fk_det_sol FOREIGN KEY (solicitud_id) REFERENCES solicitud_compra (solicitud_id) ON DELETE CASCADE,
    CONSTRAINT fk_det_prod FOREIGN KEY (producto_id) REFERENCES producto (producto_id)
) ENGINE=InnoDB;

CREATE TABLE mensaje_ti (
    mensaje_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    emisor_email VARCHAR(100) NOT NULL,
    tipo_solicitud ENUM('NUEVO_PRODUCTO', 'ELIMINAR_USUARIO', 'EDITAR_STOCK', 'OTROS') NOT NULL,
    prioridad ENUM('BAJA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL DEFAULT 'MEDIA',
    asunto VARCHAR(150) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_atencion DATETIME NULL, -- Se llena cuando TI marca como ATENDIDO
    estado ENUM('PENDIENTE', 'EN_PROCESO', 'ATENDIDO') NOT NULL DEFAULT 'PENDIENTE',
    version INT DEFAULT 0,
    PRIMARY KEY (mensaje_id)
) ENGINE=InnoDB;



CREATE TABLE movimiento (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(60) NOT NULL,

    origen_id INT UNSIGNED NOT NULL,
    destino_id INT UNSIGNED NOT NULL,

    estado ENUM('APROBADO','RECHAZADO','PENDIENTE') DEFAULT 'APROBADO',

    FOREIGN KEY (origen_id) REFERENCES almacen (almacen_id),
    FOREIGN KEY (destino_id) REFERENCES almacen (almacen_id)
) ENGINE=InnoDB;



CREATE TABLE movimiento_detalle (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    movimiento_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    cantidad DECIMAL(18,2) NOT NULL,

    FOREIGN KEY (movimiento_id) REFERENCES movimiento(id),
    FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
) ENGINE=InnoDB;


-- ==========================================================
-- DATA DE PRUEBA
-- ==========================================================

-- Roles y Usuarios 
INSERT INTO rol (nombre) VALUES ('ADMIN'), ('USER'), ('TI');
INSERT INTO usuario (nombre, email, password_hash) VALUES
('Admin', 'admin@inkaproductos.com', '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe'),
('User', 'user@inkaproductos.com', '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe'),
('Gestion TI', 'gestionti@inkaproductos.com', '$2a$10$M9qdWivDupim72L.lJqwsu26v8RKD9HtOi8gXzb0V1C5LwbUjmgVe');
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1,1), (2,2), (3,3);

INSERT INTO categoria (nombre) VALUES ('Electrónica'), ('Alimentos'), ('Ferretería'), ('Oficina');

-- 4 ALMACENES
INSERT INTO almacen (nombre, direccion) VALUES
('Almacén Central', 'Lima'),
 ('Almacén Sur', 'Arequipa'),
 ('Almacén Norte', 'Trujillo'),
 ('Almacén Callao', 'Callao');

INSERT INTO producto (sku,nombre,descripcion,categoria_id,precio_lista) VALUES
('LIMA01','Laptop HP','Laptop HP para oficina y hogar',1,2500),
('LIMA02','Mouse Logitech','Mouse inalámbrico ergonómico',1,50),
('LIMA03','Teclado Dell','Teclado estándar USB',1,80),
('LIMA04','Monitor LG','Monitor LED 24 pulgadas',1,500),
('LIMA05','Impresora Epson','Impresora multifuncional',1,700),
('LIMA06','Arroz Costeño','Arroz extra calidad 5kg',2,20),
('LIMA07','Aceite Primor','Aceite vegetal 1L',2,12),
('LIMA08','Leche Gloria','Leche evaporada lata',2,25),
('LIMA09','Fideos Don Vittorio','Fideos largos paquete',2,5),
('LIMA10','Azúcar Cartavio','Azúcar rubia 1kg',2,18),
('LIMA11','Martillo Stanley','Martillo de acero',3,30),
('LIMA12','Taladro Bosch','Taladro eléctrico industrial',3,250),
('LIMA13','Alicate Tramontina','Alicate profesional',3,22),
('LIMA14','Wincha Lufkin','Cinta métrica 5m',3,15),
('LIMA15','Sierra Truper','Sierra manual de corte',3,40),
('LIMA16','Papel Atlas','Papel bond A4',4,15),
('LIMA17','Lapicero Faber','Lapicero tinta azul',4,10),
('LIMA18','Folder Manila','Folder tamaño A4',4,5),
('LIMA19','Grapas Rapid','Caja de grapas',4,8),
('LIMA20','Toner HP','Toner original HP',4,200);


INSERT INTO producto (sku,nombre,descripcion,categoria_id,precio_lista) VALUES
('SUR01','Tablet Lenovo','Tablet 10 pulgadas',1,900),
('SUR02','Audifonos Sony','Audífonos estéreo',1,150),
('SUR03','Webcam Genius','Cámara web HD',1,100),
('SUR04','Parlantes Bose','Parlantes de alta fidelidad',1,400),
('SUR05','Cargador Univ','Cargador universal USB',1,60),
('SUR06','Lentejas Pack','Lentejas bolsa 1kg',2,15),
('SUR07','Atun Real','Atún en conserva',2,6),
('SUR08','Yogurt Danone','Yogurt bebible',2,8),
('SUR09','Galletas Soda','Galletas saladas',2,10),
('SUR10','Cafe Altomayo','Café molido',2,22),
('SUR11','Caja Herramientas','Caja organizadora',3,90),
('SUR12','Escalera Alum','Escalera de aluminio',3,180),
('SUR13','Candado Yale','Candado de seguridad',3,35),
('SUR14','Pala Bellota','Pala metálica',3,55),
('SUR15','Pico Pretul','Pico de construcción',3,45),
('SUR16','Cuaderno Stand','Cuaderno universitario',4,7),
('SUR17','Resaltador Pelikan','Resaltador fluorescente',4,4),
('SUR18','Silicona Liquida','Silicona multiuso',4,6),
('SUR19','Clips Caja','Caja de clips',4,3),
('SUR20','Corrector Cinta','Corrector en cinta',4,5);


INSERT INTO producto (sku,nombre,descripcion,categoria_id,precio_lista) VALUES
('NOR01','Router TpLink','Router inalámbrico',1,120),
('NOR02','Switch DLink','Switch de red 8 puertos',1,80),
('NOR03','Cable HDMI','Cable HDMI 2m',1,25),
('NOR04','Power Bank','Batería portátil',1,90),
('NOR05','USB 64GB','Memoria USB 64GB',1,35),
('NOR06','Sal Maras','Sal gourmet',2,5),
('NOR07','Vinagre Tinto','Vinagre artesanal',2,4),
('NOR08','Mayonesa AlaCena','Mayonesa clásica',2,7),
('NOR09','Ketchup Hellmans','Ketchup tradicional',2,7),
('NOR10','Mostaza Libby','Mostaza amarilla',2,6),
('NOR11','Nivel Torpedo','Nivel de burbuja',3,20),
('NOR12','Serrucho Carp','Serrucho carpintero',3,32),
('NOR13','Llave Inglesa','Llave ajustable',3,28),
('NOR14','Espatula 2p','Espátula metálica',3,10),
('NOR15','Lija Fierro','Lija para metal',3,2),
('NOR16','Post-it 3M','Notas adhesivas',4,12),
('NOR17','Engrapadora Swing','Engrapadora metálica',4,30),
('NOR18','Perforador Swing','Perforador de papel',4,25),
('NOR19','Tijera Maped','Tijera escolar',4,8),
('NOR20','Pizarra Blanca','Pizarra acrílica',4,150);

INSERT INTO producto (sku,nombre,descripcion,categoria_id,precio_lista) VALUES
('CALL01','Smartphone Moto','Smartphone Android',1,1100),
('CALL02','Smartwatch Mi','Reloj inteligente',1,180),
('CALL03','Kindle Paper','Lector de libros',1,550),
('CALL04','Adaptador C','Adaptador USB-C',1,40),
('CALL05','Cooler Laptop','Base refrigerante',1,75),
('CALL06','Avena 3Ositos','Avena instantánea',2,4),
('CALL07','Mermelada Fanny','Mermelada de fresa',2,6),
('CALL08','Te Filtrante','Té en sobres',2,5),
('CALL09','Chocolate Sol','Chocolate clásico',2,3),
('CALL10','Mantequilla Manty','Mantequilla tradicional',2,5),
('CALL11','Pintura Latex','Pintura blanca látex',3,45),
('CALL12','Brocha 3p','Brocha para pintar',3,8),
('CALL13','Rodillo Felpa','Rodillo de pintura',3,12),
('CALL14','Thinner 1L','Thinner industrial',3,10),
('CALL15','Cinta Masking','Cinta de enmascarar',3,6),
('CALL16','Silla Oficina','Silla ergonómica',4,350),
('CALL17','Escritorio Melam','Escritorio de melamina',4,400),
('CALL18','Lampara Escrit','Lámpara de escritorio',4,60),
('CALL19','Papelera Metal','Papelera metálica',4,20),
('CALL20','Organizador Mesa','Organizador de escritorio',4,15);

INSERT INTO inventario (almacen_id, producto_id, cantidad)
SELECT a.almacen_id, p.producto_id, 100
FROM almacen a
JOIN producto p
ON (a.almacen_id = 1 AND p.sku LIKE 'LIMA%')
OR (a.almacen_id = 2 AND p.sku LIKE 'SUR%')
OR (a.almacen_id = 3 AND p.sku LIKE 'NOR%')
OR (a.almacen_id = 4 AND p.sku LIKE 'CALL%');







