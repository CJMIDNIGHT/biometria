// ============================================================================
// Logica.js
// Clase que maneja la lógica de negocio para obtener mediciones del servidor
// ============================================================================
class Logica {
    // ------------------------------------------------------------------------
    // Constructor vacío de la clase
    // ------------------------------------------------------------------------
    constructor() {
    }
    
    // ------------------------------------------------------------------------
    // Método asíncrono que obtiene la última medición del servidor y la formatea
    // @return objeto con formato: 
    //   - Si éxito: { success: true, medicion: { tipo, tipoRaw, valor } }
    //   - Si error: { success: false, error: mensaje_de_error }
    // ------------------------------------------------------------------------
    async getMedicion() {
        // URL del endpoint de la API para obtener mediciones
        const url = 'https://amburet.upv.edu.es/api/medicion'
        
        try {
            // Hacer petición HTTP GET al servidor
            const response = await fetch(url, {
                // Especifica que es una petición GET
                method: 'GET',
                // Configura las cabeceras HTTP
                headers: {
                    // Indica que estamos enviando datos JSON
                    'Content-Type': 'application/json',
                    // Indica que esperamos recibir datos JSON
                    'Accept': 'application/json'
                }
            });
            
            // Convierte la respuesta del servidor de JSON a objeto JavaScript
            const datos = await response.json();
            
            // Verifica si la respuesta HTTP fue exitosa (código 200-299)
            if (!response.ok) {
                // Si no fue exitosa, lanza un error con el mensaje del servidor
                // o un mensaje genérico con el código HTTP
                throw new Error(datos.error || `Error HTTP: ${response.status}`);
            }
    
            // Extrae los datos de la medición del objeto respuesta
            const m = datos.data;
            
            // Verifica que los datos de medición existan y tengan los campos requeridos
            if (!m || m.tipo === undefined || m.valor === undefined) {
                return { success: false, error: 'Datos incompletos' };
            }
            
            // Formatea el tipo de medición para mostrar:
            // - 'temperatura' → 'Temperatura'
            // - 'gas' → 'Gas'
            // - cualquier otro → se deja como está
            const tipo = m.tipo === 'temperatura' ? 'Temperatura' : 
                        m.tipo === 'gas' ? 'Gas' : 
                        m.tipo;
            
            // Formatea el valor de la medición según su tipo:
            // - Si es temperatura: añade "°C" y redondea a 2 decimales
            // - Si es gas: añade "ppm" y redondea a 2 decimales
            // - Otro tipo: solo redondea a 2 decimales sin unidad
            // Math.round(m.valor*100)/100 redondea a 2 decimales
            const valor = m.tipo === 'temperatura' ? `${Math.round(m.valor*100)/100} °C` :
                         m.tipo === 'gas' ? `${Math.round(m.valor*100)/100} ppm` :
                         `${Math.round(m.valor*100)/100}`;
            
            // Devuelve objeto exitoso con la medición formateada
            return { 
                success: true, 
                medicion: { 
                    tipo,              // Tipo formateado para mostrar
                    tipoRaw: m.tipo,   // Tipo original sin formatear
                    valor              // Valor con unidad formateada
                } 
            };
            
        } catch (err) {
            // Si ocurre cualquier error (red, JSON inválido, etc.)
            // devuelve objeto de error con el mensaje
            return { success: false, error: err.message };
        }
    }
}
