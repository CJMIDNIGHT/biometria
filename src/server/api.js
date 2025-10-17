// Importar la librería Express para crear el servidor web
const express = require('express');
// Importar CORS para permitir peticiones desde otros dominios
const cors = require('cors');
// Importar dotenv para leer variables de entorno del archivo .env
const dotenv = require('dotenv');
// Importar la clase LogicaDeNegocio que contiene la lógica principal
const { LogicaDeNegocio } = require('./LogicaDeNegocio');
// Cargar el módulo de logger para guardar logs en archivos
require('./logger');

// Cargar las variables de entorno desde el archivo .env
dotenv.config();

// Crear la instancia principal de Express (el servidor web)
const app = express();
// Obtener el puerto desde variables de entorno, o usar 3000 por defecto
const PORT = process.env.PORT || 3000;

// Crear una instancia de la clase LogicaDeNegocio para acceder a sus métodos
const logicaNegocio = new LogicaDeNegocio();

// ================================
// MIDDLEWARE - Configuraciones que procesan las peticiones antes de llegar a las rutas
// ================================

// Configurar CORS: Permite que desde otros dominios puedan hacer peticiones
app.use(cors({
    origin: process.env.FRONTEND_URL || '*', // Origen permitido (desde el .env o cualquiera)
    methods: ['GET', 'POST', 'PUT', 'DELETE'], // Métodos HTTP permitidos
    credentials: true // Permitir credenciales (cookies, tokens, etc)
}));

// Middleware para convertir JSON en el body de las peticiones a objetos JavaScript
app.use(express.json({ limit: '5mb' })); // Máximo 5MB de datos

// Middleware para convertir datos URL-encoded a objetos JavaScript
app.use(express.urlencoded({ extended: true }));

// Middleware personalizado: Registra cada petición que llega al servidor
app.use((req, res, next) => {
    // Obtener la hora actual en formato ISO
    const timestamp = new Date().toISOString();
    // Mostrar en consola: hora, método HTTP, URL e IP del cliente
    console.log(`[${timestamp}] ${req.method} ${req.url} - IP: ${req.ip}`);
    // Permitir que la petición continúe al siguiente middleware/ruta
    next();
});

// ================================
// RUTAS DE LA API REST
// ================================

// RUTA 1: Verificar si el servidor está funcionando
app.get('/api/health', (req, res) => {
    // Responder con estado 200 (OK) y un JSON
    res.status(200).json({
        success: true, // Indica que fue exitoso
        message: 'API IoT funcionando correctamente',
        timestamp: new Date().toISOString() // Hora en la que se consulta
    });
});

// ================================
// RUTA 2: Guardar nueva medición (POST)
// Recibe datos del Android con: { tipo: "temperatura" | "gas", valor: number }
// ================================
app.post('/api/mediciones', async (req, res) => {
	
	// Mostrar los headers (encabezados) recibidos en la petición
    console.log('Headers recibidos:', req.headers);
    // Mostrar el body (contenido) recibido de la petición
    console.log('Body recibido:', req.body);
    // Verificar que el body sea un objeto
    console.log('Body es objeto?', typeof req.body);
    // Verificar si existe la propiedad 'tipo' en el body
    console.log('Body tiene tipo?', req.body?.tipo);
    try {
        // Validación: Verificar que sí llegaron datos en el cuerpo de la petición
        if (!req.body) {
            return res.status(400).json({
                success: false,
                error: 'No se enviaron datos en la petición'
            });
        }
		
        // Guardar los datos recibidos en una variable
        const datosMedicion = req.body;
		// Mostrar los datos en consola para debugging
		console.log("AQUI ABAJO ESTA EL REQ BODY?????")
		console.log(Object.entries(req.body)); // Convertir el objeto a pares clave-valor
		console.log(datosMedicion);
        
        // Log informativo mostrando los datos en formato JSON
        console.log('📥 Datos recibidos del Android:', JSON.stringify(datosMedicion));
		
        // VALIDACIÓN 1: Verificar que exista el campo "tipo"
        if (!datosMedicion.tipo) {
			console.log( 'El campo "tipo" es requerido')
            return res.status(400).json({
                success: false,
                error: 'El campo "tipo" es requerido'
            });
        }

        // VALIDACIÓN 2: Verificar que exista el campo "valor" y no sea null/undefined
        if (datosMedicion.valor === undefined || datosMedicion.valor === null) {
			console.log('El campo "valor" es requerido')
            return res.status(400).json({
                success: false,
                error: 'El campo "valor" es requerido'
            });
        }

        // VALIDACIÓN 3: Verificar que "tipo" sea solo "temperatura" o "gas"
        const tiposValidos = ['temperatura', 'gas']; // Array con tipos permitidos
        if (!tiposValidos.includes(datosMedicion.tipo.toLowerCase())) {
			console.log( `El tipo debe ser "temperatura" o "gas". Recibido: "${datosMedicion.tipo}"`)
            return res.status(400).json({
                success: false,
                error: `El tipo debe ser "temperatura" o "gas". Recibido: "${datosMedicion.tipo}"`
            });
        }

        // Llamar al método guardarMedicion de logicaNegocio para guardar en la BD
        const resultado = await logicaNegocio.guardarMedicion(datosMedicion);
		
        // Si todo fue bien, responder con estado 201 (Creado)
        res.status(201).json({
            success: true,
            message: 'Medición guardada exitosamente',
            data: {
                id: resultado.id, // ID de la medición guardada
                dispositivo_id: resultado.dispositivo_id, // ID del dispositivo
                tipo: resultado.tipo, // Tipo de medición (temperatura o gas)
                valor: resultado.valor, // Valor medido
                timestamp: resultado.timestamp // Hora de la medición
            }
        });

    } catch (error) {
        // Si ocurre un error, mostrarlo en consola
        console.error('❌ Error en POST /api/mediciones:', error);		
        
        // MANEJO DE ERRORES: Errores de validación de datos
        if (error.message.includes('tipo') || 
            error.message.includes('valor') || 
            error.message.includes('rango') || 
            error.message.includes('numérico')) {
			// Responder con estado 400 (Petición inválida)
            return res.status(400).json({
                success: false,
                error: error.message
            });
        }
        
        // MANEJO DE ERRORES: Problemas con la conexión a la base de datos
        if (error.message.includes('base de datos') || 
            error.message.includes('conexión') ||
            error.code === 'ECONNREFUSED') {
            // Responder con estado 503 (Servicio no disponible)
            return res.status(503).json({
                success: false,
                error: 'Error de conexión con la base de datos'
            });
        }

        // MANEJO DE ERRORES: Error genérico del servidor
        res.status(500).json({
            success: false,
            error: 'Error interno del servidor',
            // Mostrar detalles solo en desarrollo, no en producción
            detalle: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// ================================
// RUTA 3: Obtener la última medición (GET)
// ================================
app.get('/api/mediciones', async (req, res) => {
    try {
        // Log informativo
        console.log('🔍 Obteniendo última medición');

        // Llamar al método que obtiene las mediciones recientes (solicitamos solo 1)
        const ultimaMedicion = await logicaNegocio.getMedicionesRecientes(1);

        // Verificar si hay al menos una medición en la BD
        if (!ultimaMedicion || ultimaMedicion.length === 0) {
            // Responder con estado 404 (No encontrado)
            return res.status(404).json({
                success: false,
                error: 'No se encontraron mediciones en el sistema'
            });
        }

        // Si existe la medición, responder con estado 200 (OK)
        res.status(200).json({
            success: true,
            data: ultimaMedicion[0] // Enviar solo la primera medición (la más reciente)
        });

    } catch (error) {
        // Mostrar error en consola
        console.error('❌ Error en GET /api/mediciones:', error);
        
        // MANEJO DE ERRORES: Problemas con la conexión a BD
        if (error.message.includes('base de datos') || 
            error.message.includes('conexión') ||
            error.code === 'ECONNREFUSED') {
            return res.status(503).json({
                success: false,
                error: 'Error de conexión con la base de datos'
            });
        }

        // MANEJO DE ERRORES: Error genérico
        res.status(500).json({
            success: false,
            error: 'Error interno del servidor',
            detalle: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// ================================
// RUTA 4: Obtener múltiples mediciones recientes (GET)
// Parámetro opcional: ?limite=50 (número de mediciones a obtener)
// ================================
app.get('/api/mediciones/recientes', async (req, res) => {
    try {
        // Variable para almacenar el límite de mediciones a obtener
        let limite = 50; // Valor por defecto
        
        // Verificar si se envió un parámetro "limite" en la URL
        if (req.query.limite) {
            // Convertir el parámetro de texto a número entero
            limite = parseInt(req.query.limite);
            
            // Validación: El límite debe ser un número válido entre 1 y 1000
            if (isNaN(limite) || limite < 1 || limite > 1000) {
                return res.status(400).json({
                    success: false,
                    error: 'El parámetro "limite" debe ser un número entre 1 y 1000'
                });
            }
        }

        // Log informativo mostrando cuántas mediciones se van a obtener
        console.log(`📊 Obteniendo ${limite} mediciones recientes`);

        // Llamar al método para obtener las mediciones recientes
        const medicionesRecientes = await logicaNegocio.getMedicionesRecientes(limite);

        // Responder con estado 200 (OK) con los datos obtenidos
        res.status(200).json({
            success: true,
            data: medicionesRecientes, // Array de mediciones
            total: medicionesRecientes.length, // Número total de mediciones obtenidas
            limite_aplicado: limite // El límite que se usó
        });

    } catch (error) {
        // Mostrar error en consola
        console.error('❌ Error en GET /api/mediciones/recientes:', error);
        
        // MANEJO DE ERRORES: Problemas con la conexión a BD
        if (error.message.includes('base de datos') || 
            error.message.includes('conexión') ||
            error.code === 'ECONNREFUSED') {
            return res.status(503).json({
                success: false,
                error: 'Error de conexión con la base de datos'
            });
        }

        // MANEJO DE ERRORES: Error genérico
        res.status(500).json({
            success: false,
            error: 'Error interno del servidor',
            detalle: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// ================================
// RUTA COMODÍN: Manejo de rutas no encontradas (404)
// ================================
app.use('*', (req, res) => {
    // Responder con estado 404 (No encontrado)
    res.status(404).json({
        success: false,
        error: `Ruta no encontrada: ${req.method} ${req.originalUrl}`,
        // Mostrar las rutas disponibles en el servidor
        rutas_disponibles: [
            'GET  /api/health',
            'POST /api/mediciones (body: {tipo: "temperatura|gas", valor: number})', 
            'GET  /api/mediciones (retorna la última medición)',
            'GET  /api/mediciones/recientes (params: ?limite=50)'
        ]
    });
});

// ================================
// MIDDLEWARE GLOBAL DE MANEJO DE ERRORES
// Se ejecuta si hay un error no capturado en las rutas anteriores
// ================================
app.use((err, req, res, next) => {
    // Mostrar el error en consola
    console.error('💥 Error no manejado:', err);
    
    // Responder con estado 500 (Error interno del servidor)
    res.status(500).json({
        success: false,
        error: 'Error interno del servidor',
        timestamp: new Date().toISOString(),
        // Mostrar detalles del error solo en modo desarrollo
        detalle: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
});

// ================================
// INICIALIZACIÓN DEL SERVIDOR
// ================================

// Función asíncrona que inicia el servidor
async function iniciarServidor() {
    try {
        // Mostrar que se va a verificar la conexión a BD
        console.log('🔄 Verificando conexión a base de datos...');
        // Llamar al método verificarConexion de logicaNegocio
        await logicaNegocio.verificarConexion();
        // Si no hay error, la conexión fue exitosa
        console.log('✅ Conexión a base de datos exitosa');
        
        // Iniciar el servidor en el puerto especificado
        app.listen(PORT, () => {
            // Mostrar mensajes de bienvenida y información
            console.log(`\n🚀 ============================================`);
            console.log(`   Servidor IoT iniciado exitosamente`);
            console.log(`============================================`);
            console.log(`📡 Puerto: ${PORT}`);
            console.log(`🌐 URL Local: http://localhost:${PORT}`);
            console.log(`📋 Health Check: http://localhost:${PORT}/api/health`);
            console.log(`\n📊 Endpoints Disponibles:`);
            console.log(`   POST /api/mediciones`);
            console.log(`        Body: {tipo: "temperatura|gas", valor: number}`);
            console.log(`   GET  /api/mediciones`);
            console.log(`        Retorna la última medición registrada`);
            console.log(`   GET  /api/mediciones/recientes`);
            console.log(`        Params: ?limite=50`);
            console.log(`============================================`);
            console.log(`⏰ Servidor listo para recibir peticiones...\n`);
        });
        
    } catch (error) {
        // Si hay un error al iniciar el servidor, mostrarlo
        console.error('\n❌ ============================================');
        console.error('   Error al iniciar servidor');
        console.error('============================================');
        console.error('Error:', error.message);
        console.error('\n💡 Posibles soluciones:');
        console.error('   1. Verifique la configuración en el archivo .env');
        console.error('   2. Asegúrese que MySQL esté ejecutándose');
        console.error('   3. Verifique las credenciales de base de datos');
        console.error('   4. Verifique que la tabla "mediciones" exista');
        console.error('============================================\n');
        // Cerrar el proceso con código de error
        process.exit(1);
    }
}

// Manejador de señal SIGTERM: Cierra el servidor de forma segura cuando se termina el proceso
process.on('SIGTERM', () => {
    console.log('\n🔄 Cerrando servidor graciosamente...');
    process.exit(0);
});

// Manejador de señal SIGINT: Cierra el servidor cuando el usuario presiona Ctrl+C
process.on('SIGINT', () => {
    console.log('\n🔄 Servidor interrumpido por usuario...');
    process.exit(0);
});

// Llamar a la función para iniciar el servidor
iniciarServidor();

// Exportar la aplicación para que se pueda usar en tests
module.exports = app;
