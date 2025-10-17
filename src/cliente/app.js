// ============================================================================
// app.js
// ============================================================================

// Instancia global de la lógica de negocio
let logicaNegocio;

// Referencias a elementos del DOM
let elementos = {};

// ============================================================================
// INICIALIZACIÓN
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    logicaNegocio = new Logica();
    inicializarElementos();
    configurarEventListeners();
    actualizarMedicion();
});

// ============================================================================
// CONFIGURACIÓN INICIAL
// ============================================================================

function inicializarElementos() {
    elementos = {
        medicionContainer: document.getElementById('medicion-container'),
        tipoValor: document.getElementById('tipo-valor'),
        medicionValor: document.getElementById('medicion-valor'),
        sinDatos: document.getElementById('sin-datos'),
        btnActualizar: document.getElementById('btn-actualizar')
    };
}

function configurarEventListeners() {
    elementos.btnActualizar.addEventListener('click', actualizarMedicion);
}

// ============================================================================
// ACTUALIZACIÓN DE MEDICIONES
// ============================================================================

async function actualizarMedicion() {
    elementos.btnActualizar.disabled = true;
    elementos.btnActualizar.textContent = 'Actualizando...';

    try {
        const resultado = await logicaNegocio.getMedicion();

        if (resultado.success) {
            mostrarMedicion(resultado.medicion);
        } else {
            mostrarSinDatos();
        }
    } catch (error) {
        mostrarSinDatos();
    } finally {
        elementos.btnActualizar.disabled = false;
        elementos.btnActualizar.textContent = 'Obtener dato más reciente';
    }
}

function mostrarMedicion(medicion) {
    elementos.sinDatos.style.display = 'none';
    elementos.medicionContainer.style.display = 'block';
    elementos.tipoValor.textContent = medicion.tipo;
    elementos.medicionValor.textContent = medicion.valor;

    // Cambiar color según tipo
    if (medicion.tipoRaw === 'temperatura') {
        elementos.medicionValor.style.color = '#e74c3c';
    } else if (medicion.tipoRaw === 'gas') {
        elementos.medicionValor.style.color = '#2980b9';
    }
}

function mostrarSinDatos() {
    elementos.medicionContainer.style.display = 'none';
    elementos.sinDatos.style.display = 'block';
}
