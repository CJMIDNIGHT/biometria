// logger.js
const fs = require('fs');
const path = require('path');

// Definir el archivo de logs en la misma carpeta de tu app
const logFile = path.join(__dirname, 'mi_log.txt');
const logStream = fs.createWriteStream(logFile, { flags: 'a' });

// Guardar referencias originales
const originalLog = console.log;
const originalError = console.error;
const originalWarn = console.warn;

// Sobrescribir console.log
console.log = function (...args) {
    const msg = `[LOG] ${new Date().toISOString()} - ${args.join(' ')}\n`;
    logStream.write(msg);
    originalLog.apply(console, args); // también lo muestra en la salida normal
};

// Sobrescribir console.error
console.error = function (...args) {
    const msg = `[ERROR] ${new Date().toISOString()} - ${args.join(' ')}\n`;
    logStream.write(msg);
    originalError.apply(console, args);
};

// Sobrescribir console.warn
console.warn = function (...args) {
    const msg = `[WARN] ${new Date().toISOString()} - ${args.join(' ')}\n`;
    logStream.write(msg);
    originalWarn.apply(console, args);
};

module.exports = {}; // no necesitas exportar nada, basta con requerir este archivo
