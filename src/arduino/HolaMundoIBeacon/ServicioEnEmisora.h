// -*- mode: c++ -*-

// ====================================================================
// CLASE SERVICIO EN EMISORA - Gestión de servicios BLE
//
// Autor: Jordi Bataller i Mascarell
// Fecha: 2019-07-17
//
// Descripción: Define servicios y características BLE
// Un "servicio" es un conjunto de "características" que otros dispositivos
// pueden leer/escribir a través de Bluetooth Low Energy
// ====================================================================

#ifndef SERVICIO_EMISORA_H_INCLUIDO
#define SERVICIO_EMISORA_H_INCLUIDO

// Incluir librería de vectores (arrays dinámicos)
#include <vector>

// ====================================================================
// FUNCIÓN UTILIDAD: alReves()
// ====================================================================
// Invierte el orden de los elementos dentro de un array
// Modifica el array original
//
// Parámetros:
//   p: Puntero al array a invertir
//   n: Número de elementos en el array
//
// Retorna: Puntero al array (ahora invertido)
//
// Ejemplo: alReves([1,2,3,4], 4) -> [4,3,2,1]
// ====================================================================
template< typename T >
T * alReves( T * p, int n ) {
  T aux;  // Variable auxiliar para intercambiar elementos

  // Recorrer desde el principio hasta la mitad del array
  for( int i=0; i < n/2; i++ ) {
    // Guardar elemento del principio en variable auxiliar
    aux = p[i];
    
    // Copiar elemento del final al principio
    p[i] = p[n-i-1];
    
    // Copiar elemento guardado al final
    p[n-i-1] = aux;
  }
  
  return p;  // Retornar el puntero al array invertido
}

// ====================================================================
// FUNCIÓN UTILIDAD: stringAUint8AlReves()
// ====================================================================
// Convierte una cadena de texto en un array de bytes invertido
// Útil para convertir nombres en formato UUID
//
// Parámetros:
//   pString: Cadena de texto a convertir (ej: "MiServicio")
//   pUint: Puntero al array de uint8_t donde guardar el resultado
//   tamMax: Tamaño máximo del array destino (usualmente 16 para UUID)
//
// Retorna: Puntero al array de uint8_t (convertido e invertido)
//
// Ejemplo: stringAUint8AlReves("ABC", array, 16) 
//   -> array = [0,0,...,0,'C','B','A'] (con elementos al revés)
// ====================================================================
uint8_t * stringAUint8AlReves( const char * pString,  // Cadena de entrada
                                uint8_t * pUint,      // Array de salida
                                int tamMax ) {        // Tamaño máximo

  // Obtener la longitud de la cadena
  int longitudString = strlen( pString );
  
  // Calcular cuántos caracteres copiar (máximo tamMax)
  int longitudCopiar = ( longitudString > tamMax ? tamMax : longitudString );
  
  // Copiar la cadena en orden inverso en el array
  // El primer carácter de la cadena va al final del array
  for( int i=0; i<=longitudCopiar-1; i++ ) {
    pUint[ tamMax-i-1 ] = pString[ i ];  // Invertir mientras se copia
  }

  return pUint;  // Retornar el puntero al array convertido
}

// ====================================================================
// CLASE PRINCIPAL: ServicioEnEmisora
// ====================================================================
// Representa un servicio BLE que contiene características
// ====================================================================
class ServicioEnEmisora {

public:

  // ====================================================================
  // TIPO: CallbackCaracteristicaEscrita
  // ====================================================================
  // Define el tipo de función callback que se ejecuta cuando alguien
  // escribe datos en una característica
  //
  // Parámetros:
  //   conn_handle: Identificador de la conexión
  //   chr: Puntero a la característica que fue escrita
  //   data: Datos que se escribieron
  //   len: Cantidad de bytes escritos
  // ====================================================================
  using CallbackCaracteristicaEscrita = void ( uint16_t conn_handle,
                                               BLECharacteristic * chr,
                                               uint8_t * data, 
                                               uint16_t len);

  // ====================================================================
  // CLASE INTERNA: Caracteristica
  // ====================================================================
  // Representa una característica BLE dentro de un servicio
  // Las características son los datos que otros dispositivos pueden leer/escribir
  // ====================================================================
  class Caracteristica {
  private:

    // ================================================================
    // UUID DE LA CARACTERÍSTICA - 16 bytes
    // ================================================================
    // UUID es el Identificador Único de la característica
    // Se convierte de una cadena de texto a bytes invertidos
    // Comienza con bytes menos significativos primero
    // ================================================================
    uint8_t uuidCaracteristica[16] = {
      // Valores por defecto (será reemplazado en el constructor)
      '0', '1', '2', '3', 
      '4', '5', '6', '7', 
      '8', '9', 'A', 'B', 
      'C', 'D', 'E', 'F'
    };

    // Objeto BLE que representa la característica real en el hardware
    BLECharacteristic laCaracteristica;

  public:

    // ================================================================
    // CONSTRUCTOR 1: Solo con nombre
    // ================================================================
    // Crea una característica con nombre (que se convierte a UUID)
    // ================================================================
    Caracteristica( const char * nombreCaracteristica_ )
      :
      // Inicializar laCaracteristica con el UUID convertido del nombre
      laCaracteristica( stringAUint8AlReves( nombreCaracteristica_, 
                                             &uuidCaracteristica[0], 
                                             16 ) )
    {
      // Constructor completado
    }

    // ================================================================
    // CONSTRUCTOR 2: Con nombre y propiedades
    // ================================================================
    // Crea una característica completamente configurada
    // ================================================================
    Caracteristica( const char * nombreCaracteristica_,
                    uint8_t props,                      // Propiedades (read/write/notify)
                    SecureMode_t permisoRead,          // Permisos de lectura
                    SecureMode_t permisoWrite,         // Permisos de escritura
                    uint8_t tam )                       // Tamaño máximo de datos
      :
      // Llamar al otro constructor primero
      Caracteristica( nombreCaracteristica_ )
    {
      // Luego asignar propiedades, permisos y tamaño
      (*this).asignarPropiedadesPermisosYTamanyoDatos( props, 
                                                        permisoRead, 
                                                        permisoWrite, 
                                                        tam );
    }

  private:

    // ================================================================
    // MÉTODO PRIVADO: asignarPropiedades()
    // ================================================================
    // Establece qué operaciones se pueden hacer en la característica
    //
    // Propiedades comunes:
    //   CHR_PROPS_READ: Se puede leer la característica
    //   CHR_PROPS_WRITE: Se puede escribir en la característica
    //   CHR_PROPS_NOTIFY: La característica puede notificar cambios
    // ================================================================
    void asignarPropiedades ( uint8_t props ) {
      (*this).laCaracteristica.setProperties( props );
    }

    // ================================================================
    // MÉTODO PRIVADO: asignarPermisos()
    // ================================================================
    // Establece permisos de seguridad para lectura y escritura
    //
    // Permisos comunes:
    //   SECMODE_OPEN: Acceso permitido sin restricciones
    //   SECMODE_NO_ACCESS: No se permite el acceso
    // ================================================================
    void asignarPermisos( SecureMode_t permisoRead, 
                          SecureMode_t permisoWrite ) {
      (*this).laCaracteristica.setPermission( permisoRead, permisoWrite );
    }

    // ================================================================
    // MÉTODO PRIVADO: asignarTamanyoDatos()
    // ================================================================
    // Establece el tamaño máximo de datos que puede almacenar
    // la característica (en bytes)
    // ================================================================
    void asignarTamanyoDatos( uint8_t tam ) {
      (*this).laCaracteristica.setMaxLen( tam );
    }

  public:

    // ================================================================
    // MÉTODO PÚBLICO: asignarPropiedadesPermisosYTamanyoDatos()
    // ================================================================
    // Configura todas las propiedades de una característica de una sola vez
    // ================================================================
    void asignarPropiedadesPermisosYTamanyoDatos( uint8_t props,
                                                  SecureMode_t permisoRead,
                                                  SecureMode_t permisoWrite, 
                                                  uint8_t tam ) {
      asignarPropiedades( props );
      asignarPermisos( permisoRead, permisoWrite );
      asignarTamanyoDatos( tam );
    }

    // ================================================================
    // MÉTODO: escribirDatos()
    // ================================================================
    // Escribe datos en la característica
    //
    // Parámetro:
    //   str: Cadena de texto a escribir
    //
    // Retorna: Número de bytes escritos
    // ================================================================
    uint16_t escribirDatos( const char * str ) {
      uint16_t r = (*this).laCaracteristica.write( str );
      return r;
    }

    // ================================================================
    // MÉTODO: notificarDatos()
    // ================================================================
    // Notifica a los clientes conectados que los datos han cambiado
    // Los clientes BLE reciben una notificación con los nuevos datos
    //
    // Parámetro:
    //   str: Cadena de texto a notificar
    //
    // Retorna: Número de bytes notificados
    // ================================================================
    uint16_t notificarDatos( const char * str ) {
      uint16_t r = laCaracteristica.notify( &str[0] );
      return r;
    }

    // ================================================================
    // MÉTODO: instalarCallbackCaracteristicaEscrita()
    // ================================================================
    // Instala una función que se ejecutará cuando alguien escriba
    // datos en esta característica
    //
    // Parámetro:
    //   cb: Función callback a ejecutar
    // ================================================================
    void instalarCallbackCaracteristicaEscrita( CallbackCaracteristicaEscrita cb ) {
      (*this).laCaracteristica.setWriteCallback( cb );
    }

    // ================================================================
    // MÉTODO: activar()
    // ================================================================
    // Activa la característica en el hardware BLE
    // Debe llamarse después de configurar todas las propiedades
    // ================================================================
    void activar() {
      err_t error = (*this).laCaracteristica.begin();
      Globales::elPuerto.escribir( " (*this).laCaracteristica.begin(); error = " );
      Globales::elPuerto.escribir( error );
    }

  }; // fin de la clase Caracteristica
  
  // ====================================================================
  // MIEMBROS PRIVADOS DE ServicioEnEmisora
  // ====================================================================

private:

  // UUID del servicio - 16 bytes (convertido desde un nombre de texto)
  uint8_t uuidServicio[16] = {
    '0', '1', '2', '3', 
    '4', '5', '6', '7', 
    '8', '9', 'A', 'B', 
    'C', 'D', 'E', 'F'
  };

  // Objeto BLE que representa el servicio en el hardware
  BLEService elServicio;

  // Vector (array dinámico) que almacena punteros a todas las características
  // de este servicio
  std::vector< Caracteristica * > lasCaracteristicas;

public:

  // ====================================================================
  // CONSTRUCTOR
  // ====================================================================
  // Crea un servicio BLE con un nombre (que se convierte a UUID)
  // ====================================================================
  ServicioEnEmisora( const char * nombreServicio_ )
    :
    // Inicializar elServicio con el UUID convertido del nombre
    elServicio( stringAUint8AlReves( nombreServicio_, 
                                     &uuidServicio[0], 
                                     16 ) )
  {
    // Servicio creado
  }
  
  // ====================================================================
  // MÉTODO: escribeUUID()
  // ====================================================================
  // Muestra el UUID del servicio en el puerto serie para debugging
  // Imprime los 16 bytes del UUID
  // ====================================================================
  void escribeUUID() {
    Serial.println ( "**********" );
    for (int i=0; i<= 15; i++) {
      Serial.print( (char) uuidServicio[i] );
    }
    Serial.println ( "\n**********" );
  }

  // ====================================================================
  // MÉTODO: anyadirCaracteristica()
  // ====================================================================
  // Añade una característica a este servicio
  // Las características se almacenan en el vector lasCaracteristicas
  //
  // Parámetro:
  //   car: Referencia a la característica a añadir
  // ====================================================================
  void anyadirCaracteristica( Caracteristica & car ) {
    (*this).lasCaracteristicas.push_back( & car );  // Añadir al vector
  }

  // ====================================================================
  // MÉTODO: activarServicio()
  // ====================================================================
  // Activa el servicio y todas sus características en el hardware BLE
  // Debe llamarse DESPUÉS de configurar el servicio y sus características
  //
  // Pasos:
  //   1. Activar el servicio en el hardware BLE
  //   2. Activar cada una de las características del servicio
  // ====================================================================
  void activarServicio( ) {
    // Activar el servicio en el hardware
    err_t error = (*this).elServicio.begin();
    Serial.print( " (*this).elServicio.begin(); error = " );
    Serial.println( error );

    // Activar cada característica del servicio
    for( auto pCar : (*this).lasCaracteristicas ) {
      (*pCar).activar();  // Llamar a activar() en cada característica
    }
  }

  // ====================================================================
  // OPERADOR DE CONVERSIÓN: operator BLEService&()
  // ====================================================================
  // Permite usar un objeto ServicioEnEmisora en lugares donde se espera
  // un BLEService
  //
  // Ejemplo: Si una función espera "BLEService&", puedo pasar un
  // "ServicioEnEmisora&" y se convertirá automáticamente
  // ====================================================================
  operator BLEService&() {
    return elServicio;  // Retornar referencia al BLEService interno
  }
  
}; // fin de la clase ServicioEnEmisora

#endif

// ====================================================================
// FIN DEL ARCHIVO
// ====================================================================
