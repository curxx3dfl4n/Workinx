-- =====================================================================
-- WORKINX DATABASE - PROCEDIMIENTOS Y TRIGGERS AUTOMÁTICOS
-- Proyecto Formativo SENA ADSO 2026
-- =====================================================================

-- ---------------------------------------------------------------------
-- Trigger 1: trg_entrevistas_fecha_edicion
-- Propósito: Actualiza la fecha_edicion al timestamp actual cuando
--            se modifica cualquier dato descriptivo de la entrevista.
-- ---------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER `trg_entrevistas_fecha_edicion` BEFORE UPDATE ON `entrevistas` FOR EACH ROW BEGIN
    IF NOT (OLD.titulo <=> NEW.titulo)
       OR NOT (OLD.descripcion <=> NEW.descripcion)
       OR NOT (OLD.tipo <=> NEW.tipo)
       OR NOT (OLD.modalidad <=> NEW.modalidad)
       OR NOT (OLD.ubicacion <=> NEW.ubicacion)
       OR NOT (OLD.lugar_entrevista <=> NEW.lugar_entrevista)
       OR NOT (OLD.fecha_entrevista <=> NEW.fecha_entrevista)
       OR NOT (OLD.hora_entrevista <=> NEW.hora_entrevista)
       OR NOT (OLD.salario_a_convenir <=> NEW.salario_a_convenir)
       OR NOT (OLD.salario_min <=> NEW.salario_min)
       OR NOT (OLD.salario_max <=> NEW.salario_max)
       OR NOT (OLD.fecha_limite <=> NEW.fecha_limite)
    THEN
        SET NEW.fecha_edicion = CURRENT_TIMESTAMP;
    END IF;
END
$$
DELIMITER ;

-- ---------------------------------------------------------------------
-- Trigger 2: trg_desactivar_entrevista_por_reportes
-- Propósito: Regla de moderación comunitaria. Si una oferta acumula 3
--            o más reportes activos, se desactiva y pausa automáticamente.
-- ---------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER `trg_desactivar_entrevista_por_reportes` AFTER INSERT ON `reportes_entrevistas` FOR EACH ROW BEGIN
    DECLARE total_reportes_activos INT;

    SELECT COUNT(*)
    INTO total_reportes_activos
    FROM reportes_entrevistas
    WHERE entrevista_id = NEW.entrevista_id
      AND estado IN ('pendiente', 'en_revision');

    IF total_reportes_activos >= 3 THEN
        UPDATE entrevistas
        SET activa = FALSE,
            estado = 'pausada'
        WHERE id = NEW.entrevista_id;
    END IF;
END
$$
DELIMITER ;

-- ---------------------------------------------------------------------
-- Trigger 3: trg_reportes_fecha_resolucion
-- Propósito: Establece la marca de tiempo de resolución de un reporte
--            cuando su estado cambia a 'resuelto' o 'rechazado'.
-- ---------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER `trg_reportes_fecha_resolucion` BEFORE UPDATE ON `reportes_entrevistas` FOR EACH ROW BEGIN
    IF OLD.estado <> NEW.estado
       AND NEW.estado IN ('resuelto', 'rechazado')
       AND NEW.fecha_resolucion IS NULL
    THEN
        SET NEW.fecha_resolucion = CURRENT_TIMESTAMP;
    END IF;
END
$$
DELIMITER ;
