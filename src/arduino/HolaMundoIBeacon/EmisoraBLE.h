// -*- mode: c++ -*-

// ====================================================================
// CLASE EMISORA BLE - Control de Bluetooth Low Energy
//
// Autor: Jordi Bataller i Mascarell
// Fecha: 2019-07-07
//
// Descripción: Clase que encapsula toda la funcionalidad de BLE
// para emitir datos en formato iBeacon desde una placa NRF52840
// ====================================================================

#ifndef EMISORA_H_INCLUIDO
#define EMISORA_H_INCLUIDO

// Incluir la clase de servicios BLE
#include "ServicioEnEmisora.h"

// ====================================================================
// CLASE PRINCIPAL: EmisoraBLE
// ====================================================================
class EmisoraBLE {
private:

  // ================================================================
  // MIEMBROS PRIVADOS - Variables que guardan la configuración
  // ================================================================
  
  // Nombre que se mostrará cuando otros dispositivos detecten esta emisora
  const char * nombreEmisora;
  
  // ID del fabricante para iBeacon (Apple = 0x004C)
  const uint16_t fabricanteID;
  
  // Potencia de transmisión en dBm (milivatios)
  // Valores positivos = más potencia, más alcance
  const int8_t txPower;

public:

  // ================================================================
  // DEFINICIÓN DE TIPOS PARA CALLBACKS
  // ================================================================
  // Los "callbacks" son funciones que se ejecutan cuando ocurren eventos
  
  // Callback que se ejecuta cuando se establece una conexión BLE
  // connHandle: identificador de la conexión
  using CallbackConexionEstablecida = void ( uint16_t connHandle );
  
  // Callback que se ejecuta cuando se termina una conexión BLE
  // connHandle: identificador de la conexión
  // reason: razón por la que se cerró (desconexión, timeout, etc)
  using CallbackConexionTerminada = void ( uint16_t connHandle, uint8_t reason);

  // ================================================================
  // CONSTRUCTOR
  // ================================================================
  // Se ejecuta cuando se crea una nueva instancia de EmisoraBLE
  // Guarda la configuración pero NO enciende la emisora aquí
  // (se enciende después en encenderEmisora())
  // ================================================================
  EmisoraBLE( const char * nombreEmisora_,        // Nombre de la emisora
              const uint16_t fabricanteID_,       // ID del fabricante
              const int8_t txPower_ )             // Potencia de transmisión
    :
    // Inicializar los miembros privados con los parámetros
    nombreEmisora( nombreEmisora_ ) ,
    fabricanteID( fabricanteID_ ) ,
    txPower( txPower_ )
  {
    // No encender la emisora aquí porque podría haber problemas
    // si se llama antes de que Serial esté configurado
  }

  // ================================================================
  // MÉTODO: encenderEmisora()
  // ================================================================
  // Enciende el módulo Bluetooth de la placa
  // Debe llamarse DESPUÉS de que Serial esté configurado
  // ================================================================
  void encenderEmisora() {
    // Inicializar el módulo Bluefruit (enciende BLE)
    Bluefruit.begin();

    // Por si acaso, detener cualquier anuncio que pudiera estar en curso
    (*this).detenerAnuncio();
  }

  // ================================================================
  // MÉTODO: encenderEmisora() - Versión con Callbacks
  // ================================================================
  // Enciende la emisora e instala funciones que se ejecutarán
  // cuando ocurran eventos de conexión/desconexión
  // ================================================================
  void encenderEmisora( CallbackConexionEstablecida cbce,    // Callback de conexión
                        CallbackConexionTerminada cbct ) {   // Callback de desconexión

    // Primero encender la emisora
    encenderEmisora();

    // Instalar la función que se ejecutará cuando alguien se conecte
    instalarCallbackConexionEstablecida( cbce );
    
    // Instalar la función que se ejecutará cuando alguien se desconecte
    instalarCallbackConexionTerminada( cbct );

  }

  // ================================================================
  // MÉTODO: detenerAnuncio()
  // ================================================================
  // Detiene la emisión de datos BLE
  // Solo detiene si ya hay un anuncio en curso
  // ================================================================
  void detenerAnuncio() {

    // Verificar si la emisora está activamente transmitiendo
    if ( (*this).estaAnunciando() ) {
      // Detener la transmisión BLE
      Bluefruit.Advertising.stop();
    }

  }

  // ================================================================
  // MÉTODO: estaAnunciando()
  // ================================================================
  // Retorna true si la emisora está actualmente transmitiendo
  // Retorna false si NO está transmitiendo
  // ================================================================
  bool estaAnunciando() {
    return Bluefruit.Advertising.isRunning();
  }

  // ================================================================
  // MÉTODO: emitirAnuncioIBeacon()
  // ================================================================
  // Emite datos en formato iBeacon estándar
  // 
  // Parámetros:
  //   beaconUUID: Identificador único (16 bytes) del beacon
  //   major: Número mayor (2 bytes) para categorizar beacons
  //   minor: Número menor (2 bytes) para identificar beacon específico
  //   rssi: Potencia de la señal (indicador de proximidad)
  // ================================================================
  void emitirAnuncioIBeacon( uint8_t * beaconUUID, 
                             int16_t major, 
                             int16_t minor, 
                             uint8_t rssi ) {

    // Detener cualquier anuncio anterior
    (*this).detenerAnuncio();
    
    // Limpiar los datos publicitarios anteriores
    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    // Crear un objeto iBeacon con los parámetros proporcionados
    BLEBeacon elBeacon( beaconUUID, major, minor, rssi );
    
    // Establecer el ID del fabricante (Apple para iBeacon)
    elBeacon.setManufacturer( (*this).fabricanteID );

    // ================================================================
    // CONFIGURACIÓN DE LA EMISORA
    // ================================================================
    
    // Establecer la potencia de transmisión
    Bluefruit.setTxPower( (*this).txPower );
    
    // Establecer el nombre de la emisora
    Bluefruit.setName( (*this).nombreEmisora );
    
    // Añadir el nombre a la respuesta de escaneo
    Bluefruit.ScanResponse.addName();

    // ================================================================
    // ESTABLECER Y INICIAR EL BEACON
    // ================================================================
    
    // Configurar este beacon como el que se va a emitir
    Bluefruit.Advertising.setBeacon( elBeacon );

    // Reiniciar la emisión si la conexión se cierra
    Bluefruit.Advertising.restartOnDisconnect(true);
    
    // Establecer el intervalo de emisión (en unidades de 0.625 ms)
    Bluefruit.Advertising.setInterval(100, 100);

    // Iniciar la emisión indefinidamente (0 = sin tiempo límite)
    Bluefruit.Advertising.start( 0 );
    
  }

  // ================================================================
  // MÉTODO: emitirAnuncioIBeaconLibre()
  // ================================================================
  // Emite datos personalizados en formato iBeacon
  // En lugar de seguir el formato estándar (UUID-major-minor-power),
  // permite enviar cualquier dato en los 21 bytes de carga
  // 
  // Parámetros:
  //   carga: Datos personalizados a enviar (hasta 21 bytes)
  //   tamanyoCarga: Tamaño en bytes de los datos personalizados
  // 
  // ESTRUCTURA DEL ANUNCIO IBEACON LIBRE (31 bytes total):
  // 
  // Bytes 1-5:   Prefijo fijo (flags)
  // Bytes 6-31:  Datos del beacon (4 bytes de prefijo + 21 de carga)
  //   - Bytes 6-7:   0x4C, 0x00 (Apple Company ID)
  //   - Byte 8:      0x02 (iBeacon type)
  //   - Byte 9:      21 (longitud de la carga)
  //   - Bytes 10-31: 21 bytes de carga personalizada
  // ================================================================
  void emitirAnuncioIBeaconLibre( const char * carga, 
                                  const uint8_t tamanyoCarga ) {

    // Detener cualquier anuncio que esté en curso
    (*this).detenerAnuncio();

    // Limpiar datos publicitarios anteriores
    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    // Configurar el nombre de la emisora
    Bluefruit.setName( (*this).nombreEmisora );
    Bluefruit.ScanResponse.addName();

    // Establecer banderas de BLE: "Solo LE" y "Descubrible en modo general"
    Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);

    // ================================================================
    // PREPARAR DATOS DEL BEACON
    // ================================================================
    
    // Array que contiene:
    // - 4 bytes de prefijo (Company ID, tipo beacon, longitud)
    // - 21 bytes de carga personalizada
    uint8_t restoPrefijoYCarga[4+21] = {
      0x4c, 0x00,  // Company ID (Apple) - 2 bytes
      0x02,        // iBeacon type - 1 byte
      21,          // Longitud de la carga - 1 byte (0x15 en hexadecimal)
      
      // 21 espacios para la carga (llenados con guiones por defecto)
      '-', '-', '-', '-', 
      '-', '-', '-', '-', 
      '-', '-', '-', '-', 
      '-', '-', '-', '-', 
      '-', '-', '-', '-', 
      '-'
    };

    // ================================================================
    // COPIAR LOS DATOS PERSONALIZADOS
    // ================================================================
    
    // Copiar la carga personalizada en los últimos 21 bytes
    // memcpy copia datos de un lugar a otro en memoria
    memcpy( &restoPrefijoYCarga[4],     // Destino: a partir del byte 4
            &carga[0],                   // Origen: datos a copiar
            (tamanyoCarga > 21 ? 21 : tamanyoCarga)  // Copiar máximo 21 bytes
          );

    // ================================================================
    // CONFIGURAR Y INICIAR LA EMISIÓN
    // ================================================================
    
    // Añadir los datos del beacon al anuncio BLE
    Bluefruit.Advertising.addData( BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA,
                                   &restoPrefijoYCarga[0],
                                   4+21 );  // 4 bytes de prefijo + 21 de carga

    // Reiniciar la emisión si la conexión se cierra
    Bluefruit.Advertising.restartOnDisconnect(true);
    
    // Establecer intervalo de emisión (100 en unidades de 0.625 ms)
    Bluefruit.Advertising.setInterval(100, 100);

    // Establecer timeout en modo rápido (1 segundo)
    Bluefruit.Advertising.setFastTimeout( 1 );

    // Iniciar la emisión indefinidamente (0 = sin límite de tiempo)
    Bluefruit.Advertising.start( 0 );

    // Mostrar mensaje de confirmación en el puerto serie
    Globales::elPuerto.escribir( "emitiriBeacon libre  Bluefruit.Advertising.start( 0 );  \n");
  }

  // ================================================================
  // MÉTODO: anyadirServicio()
  // ================================================================
  // Añade un servicio BLE a la emisora
  // Un "servicio" es un conjunto de características que el cliente BLE puede leer
  // 
  // Parámetro:
  //   servicio: Referencia al servicio BLE a añadir
  // 
  // Retorna: true si se añadió correctamente, false si hubo error
  // ================================================================
  bool anyadirServicio( ServicioEnEmisora & servicio ) {

    // Mostrar en consola que se está añadiendo un servicio
    Globales::elPuerto.escribir( " Bluefruit.Advertising.addService( servicio ); \n");

    // Intentar añadir el servicio
    bool r = Bluefruit.Advertising.addService( servicio );

    // Si no se añadió correctamente, mostrar error
    if ( ! r ) {
      Serial.println( " SERVICION NO AÑADIDO \n");
    }

    return r;
  }

  // ================================================================
  // MÉTODO: anyadirServicioConSusCaracteristicas() - Versión base
  // ================================================================
  // Versión base que solo añade el servicio sin características
  // ================================================================
  bool anyadirServicioConSusCaracteristicas( ServicioEnEmisora & servicio ) {
    return (*this).anyadirServicio( servicio );
  }

  // ================================================================
  // MÉTODO: anyadirServicioConSusCaracteristicas() - Versión template
  // ================================================================
  // Versión template que usa argumentos variádicos (...)
  // Permite pasar múltiples características de una vez
  // Se llama a sí mismo recursivamente hasta procesar todas las características
  // 
  // Parámetros:
  //   servicio: El servicio donde añadir las características
  //   caracteristica: Primera característica a añadir
  //   restoCaracteristicas: Resto de características (cero o más)
  // ================================================================
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicas( ServicioEnEmisora & servicio,
                                             ServicioEnEmisora::Caracteristica & caracteristica,
                                             T& ... restoCaracteristicas) {

    // Añadir la primera característica al servicio
    servicio.anyadirCaracteristica( caracteristica );

    // Llamarse a sí mismo recursivamente con el resto de características
    return anyadirServicioConSusCaracteristicas( servicio, restoCaracteristicas... );
    
  }

  // ================================================================
  // MÉTODO: anyadirServicioConSusCaracteristicasYActivar()
  // ================================================================
  // Versión template que añade las características Y activa el servicio
  // 
  // Parámetros:
  //   servicio: El servicio donde añadir las características
  //   restoCaracteristicas: Las características a añadir (argumentos variádicos)
  // 
  // Retorna: true si todo fue bien, false si hubo error
  // ================================================================
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicasYActivar( ServicioEnEmisora & servicio,
                                                     T& ... restoCaracteristicas) {

    // Añadir todas las características al servicio
    bool r = anyadirServicioConSusCaracteristicas( servicio, restoCaracteristicas... );

    // Activar el servicio para que esté disponible
    servicio.activarServicio();

    return r;
    
  }

  // ================================================================
  // MÉTODO: instalarCallbackConexionEstablecida()
  // ================================================================
  // Instala la función callback que se ejecutará cuando alguien se conecte
  // 
  // Parámetro:
  //   cb: Función a ejecutar cuando se establezca una conexión
  // ================================================================
  void instalarCallbackConexionEstablecida( CallbackConexionEstablecida cb ) {
    Bluefruit.Periph.setConnectCallback( cb );
  }

  // ================================================================
  // MÉTODO: instalarCallbackConexionTerminada()
  // ================================================================
  // Instala la función callback que se ejecutará cuando se desconecte
  // 
  // Parámetro:
  //   cb: Función a ejecutar cuando se cierre una conexión
  // ================================================================
  void instalarCallbackConexionTerminada( CallbackConexionTerminada cb ) {
    Bluefruit.Periph.setDisconnectCallback( cb );
  }

  // ================================================================
  // MÉTODO: getConexion()
  // ================================================================
  // Obtiene un puntero a una conexión BLE específica
  // 
  // Parámetro:
  //   connHandle: Identificador de la conexión (asignado por el sistema)
  // 
  // Retorna: Puntero a la conexión BLE solicitada
  // ================================================================
  BLEConnection * getConexion( uint16_t connHandle ) {
    return Bluefruit.Connection( connHandle );
  }

}; // fin de la clase EmisoraBLE

#endif

// ====================================================================
// FIN DEL ARCHIVO
// ====================================================================
