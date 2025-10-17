// Importa la clase Database que maneja la conexión y operaciones con la base de datos
const { Database } = require('./database');

// Importa el módulo logger para registro de mensajes en archivo local
// Útil porque los console.log normales pueden no guardarse en todos los entornos
require('./logger');


// ============================================================================
// Clase principal que contiene toda la lógica de negocio del servidor
// Gestiona las operaciones relacionadas con mediciones biométricas
// ============================================================================
class LogicaDeNegocio {
    // ------------------------------------------------------------------------
    // Constructor: inicializa la conexión a la base de datos
    // ------------------------------------------------------------------------
    constructor() {
        // Crea una instancia de Database para ejecutar queries SQL
        this.database = new Database();
    }

    // ================================
    // MÉTODO 1: guardarMedicion
    // Recibe datos del Android y los guarda en la base de datos
    // @param datos - objeto con { tipo, valor, timestamp (opcional) }
    // @return objeto con la medición guardada incluyendo su ID
    // ================================
    async guardarMedicion(datos) {
        try {
            console.log('🔄 Iniciando guardado de medición:', datos);

            // Valida que los datos tengan la estructura correcta (tipo y valor presentes)
            this.validarDatosEntrada(datos);

            // Valida que el tipo sea 'temperatura' o 'gas'
            this.validarTipoMedicion(datos.tipo);

            // Valida que el valor sea numérico y esté en rango válido
            this.validarValorMedicion(datos.valor);

            // Prepara los datos añadiendo campos adicionales (dispositivo_id, timestamp)
            const datosParaDB = this.prepararDatosParaDB(datos);

            // Ejecuta el INSERT en la base de datos
            const resultado = await this.database.ejecutarQuery(
                `INSERT INTO mediciones (id_sensor, tipo, valor, fecha) 
                 VALUES (?, ?, ?, ?)`,
                [
                    datosParaDB.dispositivo_id,   // ID del sensor/dispositivo
                    datosParaDB.tipo,              // 'temperatura' o 'gas'
                    datosParaDB.valor,             // Valor numérico de la medición
                    datosParaDB.timestamp          // Fecha y hora de la medición
                ]
            );

            console.log('✅ Medición guardada exitosamente - ID:', resultado.insertId);

            // Retorna un objeto con todos los datos de la medición guardada
            return {
                id: resultado.insertId,                    // ID autoincremental asignado por la BD
                dispositivo_id: datosParaDB.dispositivo_id,
                tipo: datosParaDB.tipo,
                valor: datosParaDB.valor,
                timestamp: datosParaDB.timestamp
            };

        } catch (error) {
            console.error('❌ Error en guardarMedicion:', error);
            // Re-lanza el error para que sea manejado por el controlador
            throw error;
        }
    }

    // ================================
    // MÉTODO 2: getMediciones
    // Obtiene mediciones con filtros opcionales (tipo, fechas, dispositivo, etc.)
    // @param filtros - objeto opcional con criterios de búsqueda
    // @return array de mediciones que cumplen los filtros
    // ================================
    async getMediciones(filtros = {}) {
        try {
            console.log('🔍 Consultando mediciones con filtros:', filtros);

            // Construye la query SQL y parámetros según los filtros recibidos
            const { query, params } = this.construirQueryConFiltros(filtros);

            // Ejecuta la consulta en la base de datos
            const mediciones = await this.database.ejecutarQuery(query, params);

            console.log(`✅ Encontradas ${mediciones.length} mediciones`);

            // Formatea cada medición (convierte tipos, asegura consistencia)
            return mediciones.map(medicion => this.formatearMedicion(medicion));

        } catch (error) {
            console.error('❌ Error en getMediciones:', error);
            throw new Error('Error al consultar mediciones: ' + error.message);
        }
    }

    // ================================
    // MÉTODO 3: getMedicionesRecientes
    // Obtiene las últimas N mediciones para visualización en tiempo real
    // @param limite - cantidad de mediciones a obtener (1-1000, default: 50)
    // @return array con las últimas mediciones ordenadas por fecha
    // ================================
    async getMedicionesRecientes(limite = 50) {
        try {
            console.log(`📊 Consultando ${limite} mediciones más recientes`);

            // Valida que el límite sea un número entero válido
            if (!Number.isInteger(limite) || limite < 1 || limite > 1000) {
                throw new Error('El límite debe ser un número entero entre 1 y 1000');
            }

            // Query optimizada que selecciona las últimas N mediciones
            const query = `
                SELECT id, dispositivo_id, tipo, valor, fecha 
                FROM mediciones 
                ORDER BY fecha DESC 
                LIMIT ?
            `;

            // Ejecuta la consulta pasando el límite como parámetro
            const medicionesRecientes = await this.database.ejecutarQuery(query, [limite]);

            console.log(`✅ Obtenidas ${medicionesRecientes.length} mediciones recientes`);

            // Formatea y retorna las mediciones
            return medicionesRecientes.map(medicion => this.formatearMedicion(medicion));

        } catch (error) {
            console.error('❌ Error en getMedicionesRecientes:', error);
            throw new Error('Error al consultar mediciones recientes: ' + error.message);
        }
    }

    // ================================
    // MÉTODOS DE VALIDACIÓN
    // Comprueban que los datos sean correctos antes de guardarlos
    // ================================

    // Valida que el objeto de datos exista y tenga tipo y valor
    validarDatosEntrada(datos) {
        // Verifica que datos no sea null/undefined y sea un objeto
        if (!datos || typeof datos !== 'object') {
            throw new Error('Los datos de la medición son requeridos');
        }

        // Verifica que exista el campo 'tipo'
        if (!datos.tipo) {
            throw new Error('El tipo de medición es requerido');
        }

        // Verifica que exista el campo 'valor' (puede ser 0, por eso se compara con undefined/null)
        if (datos.valor === undefined || datos.valor === null) {
            throw new Error('El valor de la medición es requerido');
        }
    }

    // Valida que el tipo de medición sea 'temperatura' o 'gas'
    validarTipoMedicion(tipo) {
        // Array con los tipos permitidos
        const tiposValidos = ['temperatura', 'gas'];
        
        // Verifica que el tipo (en minúsculas y sin espacios) esté en la lista
        if (!tiposValidos.includes(tipo.toLowerCase().trim())) {
            throw new Error(`Tipo de medición inválido. Debe ser: ${tiposValidos.join(' o ')}`);
        }
    }

    // Valida que el valor sea numérico y esté en un rango razonable
    validarValorMedicion(valor) {
        // Convierte el valor a número (por si viene como string)
        const valorNumerico = Number(valor);
        
        // Verifica que sea un número válido
        if (isNaN(valorNumerico)) {
            throw new Error('El valor de la medición debe ser numérico');
        }

        // Valida que esté dentro de un rango amplio pero razonable
        // -1000 a 10000 cubre temperaturas extremas y niveles de gas
        if (valorNumerico < -1000 || valorNumerico > 10000) {
            throw new Error('El valor está fuera del rango permitido (-1000 a 10000)');
        }

        return valorNumerico;
    }

    // ================================
    // MÉTODOS DE PROCESAMIENTO
    // Preparan y transforman los datos
    // ================================

    // Prepara los datos para insertar en la base de datos
    // Añade campos adicionales y normaliza valores
    prepararDatosParaDB(datos) {
        return {
            // Por ahora siempre es 1 (dispositivo por defecto)
            // En el futuro podría identificar diferentes sensores
            dispositivo_id: 1,
            
            // Normaliza el tipo: minúsculas y sin espacios
            tipo: datos.tipo.toLowerCase().trim(),
            
            // Valida y convierte el valor a número
            valor: this.validarValorMedicion(datos.valor),
            
            // Usa el timestamp recibido o genera uno nuevo con la fecha actual
            timestamp: datos.timestamp || new Date().toISOString()
        };
    }

    // Construye dinámicamente una query SQL según los filtros proporcionados
    // @return objeto con { query: string SQL, params: array de parámetros }
    construirQueryConFiltros(filtros) {
        // Query base que selecciona todos los campos de mediciones
        let query = 'SELECT id, id_sensor, tipo, valor, fecha FROM mediciones';
        let condiciones = [];  // Array para acumular condiciones WHERE
        let params = [];       // Array para parámetros de la query (evita SQL injection)

        // Si hay filtro por dispositivo, añade condición WHERE
        if (filtros.dispositivo_id) {
            condiciones.push('dispositivo_id = ?');
            params.push(filtros.dispositivo_id);
        }

        // Si hay filtro por tipo de medición (temperatura/gas)
        if (filtros.tipo) {
            // Solo añade si el tipo es válido
            if (['temperatura', 'gas'].includes(filtros.tipo)) {
                condiciones.push('tipo = ?');
                params.push(filtros.tipo);
            }
        }

        // Si hay filtro de fecha de inicio (desde cuándo)
        if (filtros.fecha_inicio) {
            condiciones.push('timestamp >= ?');
            params.push(filtros.fecha_inicio);
        }

        // Si hay filtro de fecha fin (hasta cuándo)
        if (filtros.fecha_fin) {
            condiciones.push('timestamp <= ?');
            params.push(filtros.fecha_fin);
        }

        // Si hay condiciones, las une con AND y las añade a la query
        if (condiciones.length > 0) {
            query += ' WHERE ' + condiciones.join(' AND ');
        }

        // Siempre ordena por fecha descendente (más recientes primero)
        query += ' ORDER BY fecha DESC';

        // Si se especifica un límite de resultados
        if (filtros.limite) {
            const limite = parseInt(filtros.limite);
            // Valida que el límite sea razonable (1-1000)
            if (limite > 0 && limite <= 1000) {
                query += ' LIMIT ?';
                params.push(limite);
            }
        }

        return { query, params };
    }

    // Formatea una medición para asegurar tipos de datos consistentes
    formatearMedicion(medicion) {
        return {
            id: medicion.id,
            dispositivo_id: medicion.dispositivo_id,
            tipo: medicion.tipo,
            // Asegura que el valor sea un número (puede venir como string de la BD)
            valor: parseFloat(medicion.valor),
            timestamp: medicion.timestamp
        };
    }

    // ================================
    // MÉTODO AUXILIAR PARA VERIFICACIÓN
    // ================================

    // Verifica que la conexión a la base de datos funcione correctamente
    // @return true si la conexión es exitosa
    async verificarConexion() {
        try {
            console.log('🔄 Verificando conexión a base de datos...');
            
            // Ejecuta una query simple de prueba (SELECT 1)
            await this.database.ejecutarQuery('SELECT 1 as test');
            
            console.log('✅ Conexión a base de datos verificada exitosamente');
            return true;
            
        } catch (error) {
            console.error('❌ Error de conexión a base de datos:', error);
            throw new Error('No se puede conectar a la base de datos: ' + error.message);
        }
    }

    // ================================
    // MÉTODOS DE ESTADÍSTICAS BÁSICAS (BONUS)
    // ================================

    // Obtiene estadísticas generales sobre las mediciones almacenadas
    // @return objeto con totales, promedios y última medición
    async obtenerEstadisticas() {
        try {
            // Query que calcula múltiples estadísticas en una sola consulta
            const stats = await this.database.ejecutarQuery(`
                SELECT 
                    COUNT(*) as total_medicione
