// -*- mode: c++ -*-

// ====================================================================
// CLASE PUBLICADOR - Gestión de publicación de datos BLE
//
// Autor: Jordi Bataller i Mascarell
//
// Descripción: Clase que encapsula la lógica de publicación de datos
// de sensores a través de Bluetooth Low Energy en formato iBeacon
// ====================================================================

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

// ====================================================================
// CLASE PUBLICADOR
// ====================================================================
class Publicador {

  // ====================================================================
  // MIEMBROS PRIVADOS
  // ====================================================================
  
private:

  // UUID (Identificador Único Universal) del beacon - 16 bytes
  // Este identificador es fijo para todos los beacons de este proyecto
  // Los bytes representan la cadena "EPSG-GTI-PROY-3A" en hexadecimal
  // 0x45='E', 0x50='P', 0x53='S', 0x47='G', etc.
  uint8_t beaconUUID[16] = {
    0x45, 0x50, 0x53, 0x47,  // "EPSG"
    0x2D, 0x47, 0x54, 0x49,  // "-GTI"
    0x2D, 0x50, 0x52, 0x4F,  // "-PRO"
    0x59, 0x2D, 0x33, 0x41   // "Y-3A"
  };

  // ====================================================================
  // MIEMBROS PÚBLICOS
  // ====================================================================
  
public:

  // Variable que contiene la emisora BLE
  // "laEmisora" es una instancia de EmisoraBLE que gestiona la transmisión
  EmisoraBLE laEmisora {
    "GTI",      // Nombre de la emisora (lo verán otros dispositivos BLE)
    0x004c,     // ID del fabricante: 0x004C es el ID de Apple para iBeacon
    4           // Potencia de transmisión en dBm (4 = potencia moderada)
  };
  
  // Potencia de la señal RSSI (Received Signal Strength Indicator)
  // Valor negativo en dBm que indica la "proximidad" del beacon
  // -53 es un valor arbitrario usado para ajustar la indicación de distancia
  const int RSSI = -53;

  // ====================================================================
  // ENUMERACIÓN: MedicionesID
  // ====================================================================
  // Define identificadores numéricos para cada tipo de medición
  // Se usa para identificar qué tipo de dato se está transmitiendo
  // ====================================================================
  enum MedicionesID  {
    CO2 = 11,           // Identificador para mediciones de CO2
    TEMPERATURA = 12,  // Identificador para mediciones de temperatura
    RUIDO = 13          // Identificador para mediciones de ruido
  };

  // ====================================================================
  // CONSTRUCTOR
  // ====================================================================
  // Se ejecuta cuando se crea una instancia de Publicador
  // NO enciende la emisora aquí (se hace después en setup())
  // ====================================================================
  Publicador( ) {
    // IMPORTANTE: No llamar a encenderEmisora() aquí
    // Esperar a que se llame desde setup() cuando todo esté inicializado
  }

  // ====================================================================
  // MÉTODO: encenderEmisora()
  // ====================================================================
  // Enciende el módulo Bluetooth Low Energy
  // Se llama desde el programa principal (setup)
  // ====================================================================
  void encenderEmisora() {
    (*this).laEmisora.encenderEmisora();
  }

  // ====================================================================
  // MÉTODO: publicarCO2()
  // ====================================================================
  // Publica un valor de CO2 a través de BLE en formato iBeacon
  // 
  // Parámetros:
  //   valorCO2: Valor del CO2 medido (2 bytes)
  //   contador: Número de lectura o identificador (1 byte)
  //   tiempoEspera: Milisegundos durante los cuales mantener la emisión
  // 
  // Funcionamiento:
  //   1. Calcula el valor "major" combinando el ID de medición con el contador
  //   2. Emite un iBeacon con la información
  //   3. Espera el tiempo indicado
  //   4. Detiene la emisión
  // ====================================================================
  void publicarCO2( int16_t valorCO2,      // Valor del CO2 a enviar
                    uint8_t contador,      // Número de medición
                    long tiempoEspera ) {  // Tiempo de emisión en ms

    // ================================================================
    // PASO 1: CONSTRUIR EL VALOR "MAJOR"
    // ================================================================
    // El valor "major" de un iBeacon es de 2 bytes
    // Lo dividimos en dos partes:
    //   - Byte alto (superior): ID de la medición (CO2 = 11)
    //   - Byte bajo (inferior): Contador de mediciones
    // 
    // Ejemplo: Si ID=11 y contador=3
    //   major = (11 << 8) + 3 = (11 * 256) + 3 = 2816 + 3 = 2819
    // ================================================================
    uint16_t major = (MedicionesID::CO2 << 8) + contador;
    
    // ================================================================
    // PASO 2: EMITIR EL IBEACON CON LOS DATOS
    // ================================================================
    // Parámetros de emitirAnuncioIBeacon():
    //   - beaconUUID: Identificador único del proyecto (16 bytes)
    //   - major: Tipo de medición + contador
    //   - valorCO2: Valor medido (se envía como "minor")
    //   - RSSI: Potencia de señal para indicar proximidad
    // ================================================================
    (*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
                                            major,
                                            valorCO2,       // Enviar CO2 como "minor"
                                            (*this).RSSI    // Potencia de señal
                                          );

    // Mostrar en el puerto serie los datos que se están enviando
    Globales::elPuerto.escribir( "   publicarCO2(): valor=" );
    Globales::elPuerto.escribir( valorCO2 );           // Mostrar valor de CO2
    Globales::elPuerto.escribir( "   contador=" );
    Globales::elPuerto.escribir( contador );           // Mostrar número de medición
    Globales::elPuerto.escribir( "   todo="  );
    Globales::elPuerto.escribir( major );              // Mostrar el valor major completo
    Globales::elPuerto.escribir( "\n" );

    // ================================================================
    // PASO 3: ESPERAR EL TIEMPO INDICADO
    // ================================================================
    // Durante este tiempo, la emisora sigue transmitiendo el iBeacon
    // Otros dispositivos BLE pueden detectar y recibir los datos
    // ================================================================
    esperar( tiempoEspera );

    // ================================================================
    // PASO 4: DETENER LA EMISIÓN
    // ================================================================
    // Después de esperar, se detiene la transmisión del iBeacon
    // ================================================================
    (*this).laEmisora.detenerAnuncio();
  }

  // ====================================================================
  // MÉTODO: publicarTemperatura()
  // ====================================================================
  // Publica un valor de temperatura a través de BLE en formato iBeacon
  // Funciona de manera muy similar a publicarCO2()
  // 
  // Parámetros:
  //   valorTemperatura: Valor de temperatura medido (2 bytes)
  //   contador: Número de lectura o identificador (1 byte)
  //   tiempoEspera: Milisegundos durante los cuales mantener la emisión
  // 
  // Diferencia con publicarCO2():
  //   - Usa el ID TEMPERATURA (12) en lugar de CO2 (11)
  //   - Envía el valor de temperatura como "minor"
  // ====================================================================
  void publicarTemperatura( int16_t valorTemperatura,  // Valor de temperatura a enviar
                            uint8_t contador,          // Número de medición
                            long tiempoEspera ) {      // Tiempo de emisión en ms

    // ================================================================
    // CONSTRUIR EL VALOR "MAJOR" CON ID DE TEMPERATURA
    // ================================================================
    // Igual que en publicarCO2, pero usando TEMPERATURA (12)
    // ================================================================
    uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;
    
    // ================================================================
    // EMITIR EL IBEACON CON LOS DATOS DE TEMPERATURA
    // ================================================================
    (*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
                                            major,
                                            valorTemperatura,  // Enviar temperatura como "minor"
                                            (*this).RSSI       // Potencia de señal
                                          );
    
    // Esperar el tiempo indicado mientras se transmite
    esperar( tiempoEspera );

    // Mostrar en el puerto serie los datos que se han enviado
    Globales::elPuerto.escribir( "   publicarTemperatura(): valor=" );
    Globales::elPuerto.escribir( valorTemperatura );   // Mostrar valor de temperatura
    Globales::elPuerto.escribir( "   contador=" );
    Globales::elPuerto.escribir( contador );           // Mostrar número de medición
    Globales::elPuerto.escribir( "   todo="  );
    Globales::elPuerto.escribir( major );              // Mostrar el valor major completo
    Globales::elPuerto.escribir( "\n" );

    // ================================================================
    // DETENER LA EMISIÓN
    // ================================================================
    (*this).laEmisora.detenerAnuncio();
  }
  
}; // fin de la clase Publicador

// ====================================================================
// FIN DEL ARCHIVO
// ====================================================================
#endif
