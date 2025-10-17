// -*-c++-*-

// --------------------------------------------------------------
//
// Jordi Bataller i Mascarell
// 2019-07-07
//
// --------------------------------------------------------------

// https://learn.sparkfun.com/tutorials/nrf52840-development-with-arduino-and-circuitpython

// https://stackoverflow.com/questions/29246805/can-an-ibeacon-have-a-data-payload

// -*-c++-*-

// ====================================================================
// PROGRAMA PARA NRF52840 - EMISOR BLE CON SENSORES
// 
// Autor: Jordi Bataller i Mascarell
// Fecha: 2019-07-07
// 
// Descripción: Este programa controla una placa NRF52840 que:
//   - Emite datos por Bluetooth Low Energy (BLE)
//   - Lee sensores de CO2 y temperatura
//   - Publica los datos mediante iBeacon
//   - Controla un LED indicador
// ====================================================================

// Incluir la librería de Bluetooth Low Energy (BLE)
#include <bluefruit.h>

// Desactivar las funciones min() y max() de bluefruit.h
// porque colisionan con las de la librería estándar
#undef min
#undef max

// ====================================================================
// INCLUIR LIBRERÍAS PERSONALIZADAS
// ====================================================================

// Librería para controlar el LED (encender, apagar, parpadear)
#include "LED.h"
// Librería para comunicación por puerto serie (mostrar mensajes en monitor)
#include "PuertoSerie.h"

// ====================================================================
// NAMESPACE GLOBALES - PARTE 1: COMPONENTES BÁSICOS
// ====================================================================
namespace Globales {
  
  // Variable global del LED conectado al pin 7
  // Se puede acceder desde cualquier parte del código como Globales::elLED
  LED elLED ( /* NUMERO DEL PIN LED = */ 7 );

  // Variable global del puerto serie a 115200 baudios
  // Se usa para enviar mensajes al monitor serial para debugging
  PuertoSerie elPuerto ( /* velocidad = */ 115200 ); // 115200 o 9600 o ...

  // Nota: Serial1 es la conexión entre la placa NRF52840 y los sensores

};

// ====================================================================
// INCLUIR LIBRERÍAS PERSONALIZADAS - PARTE 2
// ====================================================================

// Librería que maneja la emisora BLE (envía datos por Bluetooth)
#include "EmisoraBLE.h"
// Librería que publica datos en formato iBeacon
#include "Publicador.h"
// Librería que lee los sensores (CO2 y temperatura)
#include "Medidor.h"

// ====================================================================
// NAMESPACE GLOBALES - PARTE 2: COMPONENTES PRINCIPALES
// ====================================================================
namespace Globales {

  // Variable global del publicador BLE
  // Gestiona cómo se envían los datos por Bluetooth
  Publicador elPublicador;

  // Variable global del medidor de sensores
  // Lee los valores de CO2 y temperatura
  Medidor elMedidor;

}; // fin del namespace

// ====================================================================
// FUNCIÓN: inicializarPlaquita()
// ====================================================================
// Esta función inicializa configuraciones específicas de la placa
// Por ahora está vacía (de momento nada)
// ====================================================================
void inicializarPlaquita () {

  // TODO: Añadir configuraciones necesarias de la placa aquí
  // de momento nada

} // ()

// ====================================================================
// FUNCIÓN: setup()
// ====================================================================
// Se ejecuta UNA SOLA VEZ cuando se enciende la placa
// Inicializa todos los componentes del sistema
// ====================================================================
void setup() {

  // Esperar a que el puerto serie esté disponible para comunicación
  Globales::elPuerto.esperarDisponible();

  // Inicializar configuraciones específicas de la placa
  inicializarPlaquita();

  // Suspender el loop() para ahorrar energía (opcional, desactivado)
  // suspendLoop();

  // Encender la emisora BLE (módulo Bluetooth)
  // A partir de aquí, la placa comienza a emitir por Bluetooth
  Globales::elPublicador.encenderEmisora();

  // Línea comentada: Prueba de emisión (para debugging)
  // Globales::elPublicador.laEmisora.pruebaEmision();
  
  // Inicializar el medidor de sensores
  // Prepara los sensores de CO2 y temperatura para empezar a leer
  Globales::elMedidor.iniciarMedidor();

  // Esperar 1 segundo para que todo se estabilice
  esperar( 1000 );

  // Mostrar en el puerto serie que setup() ha terminado
  Globales::elPuerto.escribir( "---- setup(): fin ---- \n " );

} // fin de setup()

// ====================================================================
// FUNCIÓN: lucecitas()
// ====================================================================
// Parpadea el LED 3 veces rápido y luego 1 vez lento
// Se usa como indicador visual de que el programa está funcionando
// ====================================================================
inline void lucecitas() {
  using namespace Globales;

  // Primer parpadeo: LED encendido 100ms + apagado 400ms
  elLED.brillar( 100 );     // Encender LED 100 milisegundos
  esperar ( 400 );          // Esperar 400 milisegundos (apagado)
  
  // Segundo parpadeo: LED encendido 100ms + apagado 400ms
  elLED.brillar( 100 );     // Encender LED 100 milisegundos
  esperar ( 400 );          // Esperar 400 milisegundos (apagado)
  
  // Tercer parpadeo: LED encendido 100ms + apagado 400ms
  Globales::elLED.brillar( 100 );  // Encender LED 100 milisegundos
  esperar ( 400 );                  // Esperar 400 milisegundos (apagado)
  
  // Cuarto parpadeo (más largo): LED encendido 1000ms + apagado 1000ms
  Globales::elLED.brillar( 1000 ); // Encender LED 1000 milisegundos
  esperar ( 1000 );                 // Esperar 1000 milisegundos (apagado)
  
} // fin de lucecitas()

// ====================================================================
// FUNCIÓN: loop()
// ====================================================================
// Se ejecuta repetidamente después de setup()
// Es el ciclo principal del programa
// ====================================================================

// Namespace para variables locales del loop
// "cont" es un contador de cuántas veces se ha ejecutado el loop
namespace Loop {
  uint8_t cont = 0;  // Contador (número sin signo de 8 bits: 0-255)
};

// ..................................................................
// FUNCIÓN PRINCIPAL DEL LOOP
// ..................................................................
void loop () {

  using namespace Loop;      // Usar variables del namespace Loop
  using namespace Globales;  // Usar variables del namespace Globales

  // Esperar 5 segundos antes de hacer cualquier cosa
  // Esto permite que el programa no genere demasiados datos
  esperar ( 5000);

  // Incrementar el contador (cada vez que se ejecuta loop, suma 1)
  cont++;

  // Mostrar en el puerto serie que comienza una nueva iteración del loop
  elPuerto.escribir( "\n---- loop(): empieza " );
  elPuerto.escribir( cont );          // Mostrar número de iteración
  elPuerto.escribir( "\n" );

  // ................................................................
  // PARPADEAR EL LED COMO INDICADOR VISUAL
  // ................................................................
  lucecitas();

  // ................................................................
  // LEER Y PUBLICAR VALOR DE CO2
  // ................................................................
  
  // Leer el valor del sensor de CO2
  int valorCO2 = elMedidor.medirCO2();
  
  // Publicar el valor de CO2 por Bluetooth
  // Parámetros:
  //   - valorCO2: el valor medido del CO2
  //   - cont: número de iteración (para referencia)
  //   - 1000: intervalo en milisegundos entre emisiones
  elPublicador.publicarCO2( valorCO2,
                            cont,
                            1000 // intervalo de emisión
                          );
  
  // ................................................................
  // LEER Y PUBLICAR VALOR DE TEMPERATURA
  // ................................................................
  
  // Leer el valor del sensor de temperatura
  int valorTemperatura = elMedidor.medirTemperatura();
  
  // Publicar el valor de temperatura por Bluetooth
  // Parámetros:
  //   - valorTemperatura: el valor medido de temperatura
  //   - cont: número de iteración (para referencia)
  //   - 1000: intervalo en milisegundos entre emisiones
  elPublicador.publicarTemperatura( valorTemperatura, 
                                    cont,
                                    1000 // intervalo de emisión
                                  );

  // ................................................................
  // CÓDIGO COMENTADO - PRUEBA DE IBEACON CON DATOS PERSONALIZADOS
  // ................................................................
  // 
  // Esto es una prueba para enviar datos personalizados en un iBeacon
  // Un iBeacon normalmente usa 21 bytes (UUID 16 + major 2 + minor 2 + txPower 1)
  // Pero aquí se envía cualquier dato en esos 21 bytes
  // 
  // Nota: Cuando termine esta prueba, "Publicador::laEmisora" debe hacerse privado
  //
  
  /*char datos[21] = {
    'H', 'o', 'l', 'a',
    'H', 'o', 'l', 'a',
    'H', 'o', 'l', 'a',
    'H', 'o', 'l', 'a',
    'H', 'o', 'l', 'a',
    'H'
  };*/

  // Estas líneas emitirían datos personalizados (están comentadas)
  // elPublicador.laEmisora.emitirAnuncioIBeaconLibre ( &datos[0], 21 );
  // elPublicador.laEmisora.emitirAnuncioIBeaconLibre ( "Este tiene 21 chars!!", 21 );

  // Esperar 20 segundos antes de siguiente iteración
  esperar( 20000 );

  // Detener la emisión de datos por Bluetooth
  elPublicador.laEmisora.detenerAnuncio();
  
  // Mostrar en el puerto serie que el loop ha terminado
  elPuerto.escribir( "---- loop(): acaba **** " );
  elPuerto.escribir( cont );          // Mostrar número de iteración
  elPuerto.escribir( "\n" );
  
} // fin de loop()

// ====================================================================
// FIN DEL PROGRAMA
// ====================================================================
