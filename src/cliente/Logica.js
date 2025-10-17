// ============================================================================
// Logica.js
// ============================================================================

class Logica {

    constructor() {
    }

    // ------------------------------------------------------------------------
    // Obtener y formatear última medición
    // ------------------------------------------------------------------------
    async getMedicion() {
		const url = 'https://amburet.upv.edu.es/api/medicion'
        try {

			// Hacer petición al backend mediante el peticionario
			const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

			// Leer el cuerpo de la respuesta
            const datos = await response.json();

            // Verificar si la petición fue exitosa
            if (!response.ok) {
                throw new Error(datos.error || `Error HTTP: ${response.status}`);
            }
	
			//Extraer los datos
            const m = datos.data;
            if (!m || m.tipo === undefined || m.valor === undefined) {
                return { success: false, error: 'Datos incompletos' };
            }

            // Formatear medición directamente
            const tipo = m.tipo === 'temperatura' ? 'Temperatura' : m.tipo === 'gas' ? 'Gas' : m.tipo;
            const valor = m.tipo === 'temperatura' ? `${Math.round(m.valor*100)/100} °C` :
                m.tipo === 'gas' ? `${Math.round(m.valor*100)/100} ppm` :
                    `${Math.round(m.valor*100)/100}`;

            return { success: true, medicion: { tipo, tipoRaw: m.tipo, valor } };

        } catch (err) {
            return { success: false, error: err.message };
        }
    }

}
