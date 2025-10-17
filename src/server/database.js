// Importar la librería mysql2 con soporte para promesas
const mysql = require('mysql2/promise');
// Importar dotenv para leer variables de entorno del archivo .env
const dotenv = require('dotenv');
// Cargar el módulo de logger para guardar logs en archivos
require('./logger');
// Cargar las variables de entorno desde el archivo .env
dotenv.config();

// Clase que maneja todas las operaciones con la base de datos MySQL
class Database {
    // Constructor: Se ejecuta cuando se crea una nueva instancia de Database
    constructor() {
        // Crear un pool de conexiones a MySQL
        // Un "pool" es un conjunto de conexiones reutilizables para mejor rendimiento
        this.pool = mysql.createPool({
            // Dirección del servidor MySQL (desde .env o 'localhost' por defecto)
            host: process.env.DB_HOST || 'localhost',
            // Usuario de MySQL (desde .env o 'root' por defecto)
            user: process.env.DB_USER || 'root',
            // Contraseña de MySQL (desde .env o vacía por defecto)
            password: process.env.DB_PASSWORD || '',
            // Nombre de la base de datos (desde .env o 'biometria_0' por defecto)
            database: process.env.DB_NAME || 'biometria_0',
            // Puerto de MySQL (desde .env o 3306 por defecto)
            port: process.env.DB_PORT || 3306,
            // Si true, espera a que haya una conexión disponible antes de ejecutar un query
            waitForConnections: true,
            // Máximo número de conexiones simultáneas en el pool (10 conexiones)
            connectionLimit: 10,
            // Máximo número de queries en espera de conexión (0 = sin límite)
            queueLimit: 0,
            // Tiempo máximo (en milisegundos) para establecer la conexión (10 segundos)
            connectTimeout: 10000
        });
        
        // Realizar una prueba inicial de conexión a la base de datos
        this.pool.getConnection()
            .then(conn => {
                // Si la conexión es exitosa, mostrar mensaje positivo
                console.log('✅ Conexión a MySQL establecida');
                // Liberar la conexión para que otros queries la puedan usar
                conn.release();
            })
            .catch(err => {
                // Si hay error en la conexión, mostrar el mensaje de error
                console.error('❌ Error conectando a MySQL:', err.message);
            });
    }
    
    /**
     * Método para ejecutar cualquier query SQL
     * @param {string} sql - Consulta SQL con placeholders (?) para valores
     * @param {Array} params - Array con los valores que reemplazarán los placeholders
     * @returns {Promise<Array|Object>} - Retorna un array con los resultados del query
     */
    async ejecutarQuery(sql, params = []) {
        try {
            // Obtener una conexión del pool y ejecutar el query
            // [rows] es desestructuración: obtenemos solo la primera parte del resultado
            const [rows] = await this.pool.execute(sql, params);
            // Retornar los resultados (filas) del query
            return rows;
        } catch (err) {
            // Si ocurre un error, mostrarlo en consola
            console.error('❌ Error en ejecutarQuery:', err.message);
            // Lanzar el error para que se maneje en otro lado del código
            throw err;
        }
    }
    
    /**
     * Método para iniciar una transacción manual
     * Una transacción permite ejecutar múltiples queries como una unidad atómica
     * Si algo falla, se pueden deshacer todos los cambios
     * @returns {Promise<Connection>} - Retorna la conexión con la transacción iniciada
     */
    async iniciarTransaccion() {
        // Obtener una conexión del pool
        const conn = await this.pool.getConnection();
        // Iniciar una transacción en esa conexión
        await conn.beginTransaction();
        // Retornar la conexión para que el usuario ejecute queries en ella
        return conn;
    }
    
    /**
     * Método para cerrar todas las conexiones del pool
     * Se usa cuando el servidor se apaga para liberar los recursos
     */
    async cerrarConexion() {
        try {
            // Cerrar todas las conexiones del pool
            await this.pool.end();
            // Mostrar mensaje indicando que se cerró correctamente
            console.log('🔒 Conexión MySQL cerrada correctamente');
        } catch (err) {
            // Si hay error al cerrar, mostrarlo en consola
            console.error('❌ Error cerrando conexión MySQL:', err.message);
        }
    }
}

// Exportar la clase Database para que otros archivos la puedan usar
module.exports = { Database };
