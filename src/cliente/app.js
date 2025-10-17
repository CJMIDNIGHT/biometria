// ============================================================================
// app.js
// Archivo principal que maneja la interfaz de usuario y la interacción
// ============================================================================

// Variable global que almacena la instancia de la clase Logica
// Se usa para acceder a los métodos de lógica de negocio desde toda la aplicación
let logicaNegocio;

// Objeto global que almacena referencias a los elementos HTML del DOM
// Evita buscar elementos repetidamente con getElementById
let elementos = {};

// ============================================================================
// INICIALIZACIÓN
// ============================================================================

// Event listener que se ejecuta cuando el HTML ha cargado completamente
// Es el punto de entrada de la aplicación
document.addEventListener('DOMContentLoaded', function() {
    // Crea una nueva instancia de la clase Logica
    logicaNegocio = new Logica();
    
    // Busca y guarda referencias a todos los elementos HTML necesarios
    inicializarElementos();
    
    // Configura los event listeners (clicks, etc.) en los elementos
    configurarEventListeners();
    
    // Obtiene y muestra la primera medición al cargar la página
    actualizarMedicion();
});

// ============================================================================
// CONFIGURACIÓN INICIAL
// ============================================================================

// Función que busca y guarda referencias a todos los elementos HTML del DOM
// Almacena las referencias en el objeto 'elementos' para acceso rápido
function inicializarElementos() {
    elementos = {
        // Contenedor principal donde se muestra la medición
        medicionContainer: document.getElementById('medicion-container'),
        
        // Elemento que muestra el tipo de medición (Temperatura/Gas)
        tipoValor: document.getElementById('tipo-valor'),
        
        // Elemento que muestra el valor de la medición con unidad
        medicionValor: document.getElementById('medicion-valor'),
        
        // Elemento que se muestra cuando no hay datos disponibles
        sinDatos: document.getElementById('sin-datos'),
        
        // Botón para actualizar/recargar la medición
        btnActualizar: document.getElementById('btn-actualizar')
    };
}

// Función que configura los event listeners (manejadores de eventos)
// Asocia acciones del usuario con funciones
function configurarEventListeners() {
    // Cuando se hace click en el botón actualizar, ejecuta la función actualizarMedicion
    elementos.btnActualizar.addEventListener('click', actualizarMedicion);
}

// ============================================================================
// ACTUALIZACIÓN DE MEDICIONES
// ============================================================================

// Función asíncrona que obtiene la última medición del servidor y actualiza la UI
// Se ejecuta al cargar la página y cada vez que se pulsa el botón actualizar
async function actualizarMedicion() {
    // Deshabilita el botón para evitar múltiples clicks mientras carga
    elementos.btnActualizar.disabled = true;
    
    // Cambia el texto del botón para indicar que está cargando
    elementos.btnActualizar.textContent = 'Actualizando...';
    
    try {
        // Llama al método getMedicion de la lógica de negocio
        // Espera (await) a que termine la petición al servidor
        const resultado = await logicaNegocio.getMedicion();
        
        // Verifica si la petición fue exitosa
        if (resultado.success) {
            // Si hay datos, los muestra en la interfaz
            mostrarMedicion(resultado.medicion);
        } else {
            // Si no hay datos o hubo error, muestra mensaje de sin datos
            mostrarSinDatos();
        }
    } catch (error) {
        // Si ocurre algún error no capturado, muestra mensaje de sin datos
        mostrarSinDatos();
    } finally {
        // Este bloque SIEMPRE se ejecuta al final (haya error o no)
        
        // Vuelve a habilitar el botón
        elementos.btnActualizar.disabled = false;
        
        // Restaura el texto original del botón
        elementos.btnActualizar.textContent = 'Obtener dato más reciente';
    }
}

// Función que muestra una medición en la interfaz
// @param medicion - objeto con { tipo, tipoRaw, valor }
function mostrarMedicion(medicion) {
    // Oculta el mensaje de "sin datos"
    elementos.sinDatos.style.display = 'none';
    
    // Muestra el contenedor de la medición
    elementos.medicionContainer.style.display = 'block';
    
    // Actualiza el texto del tipo de medición (Temperatura/Gas)
    elementos.tipoValor.textContent = medicion.tipo;
    
    // Actualiza el texto del valor con su unidad (ej: "25.5 °C")
    elementos.medicionValor.textContent = medicion.valor;
    
    // Cambia el color del valor según el tipo de medición
    if (medicion.tipoRaw === 'temperatura') {
        // Rojo para temperatura
        elementos.medicionValor.style.color = '#e74c3c';
    } else if (medicion.tipoRaw === 'gas') {
        // Azul para gas
        elementos.medicionValor.style.color = '#2980b9';
    }
}

// Función que muestra el mensaje de "sin datos disponibles"
// Se ejecuta cuando no hay mediciones o hay un error
function mostrarSinDatos() {
    // Oculta el contenedor de medición
    elementos.medicionContainer.style.display = 'none';
    
    // Muestra el mensaje de sin datos
    elementos.sinDatos.style.display = 'block';
}
