// -*- mode: c++ -*-

// ====================================================================
// CLASE MEDIDOR - Lectura de sensores
//
// Descripción: Clase que encapsula la lectura de sensores
// Actualmente retorna valores fijos (para pruebas)
// En producción, estos métodos leerían datos de sensores reales
// ====================================================================

#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ====================================================================
// CLASE MEDIDOR
// ====================================================================
class Medidor {

  // ====================================================================
  // MIEMBROS PRIVADOS
  // ====================================================================
  // (Vacío por ahora, podría contener punteros a sensores, puertos, etc)
  
private:

public:

  // ====================================================================
  // CONSTRUCTOR
  // ====================================================================
  // Se ejecuta cuando se crea una nueva instancia de Medidor
  // Por ahora no hace nada (inicialización mínima)
  // Las configuraciones más complejas se hacen en iniciarMedidor()
  // ====================================================================
  Medidor() {
    // Constructor vacío por ahora
  }

  // ====================================================================
  // MÉTODO: iniciarMedidor()
  // ====================================================================
  // Realiza configuraciones que no se pueden hacer en el constructor
  // 
  // Usos potenciales:
  //   - Inicializar puertos serie para comunicación con sensores
  //   - Configurar pines de entrada/salida
  //   - Calibrar sensores
  //   - Establecer interrupciones
  // 
  // Por ahora está vacío (a implementar)
  // ====================================================================
  void iniciarMedidor() {
    // Las cosas que no se puedan hacer en el constructor, if any
    // TODO: Implementar inicialización de sensores aquí
  }

  // ====================================================================
  // MÉTODO: medirCO2()
  // ====================================================================
  // Lee el valor actual del sensor de CO2
  // 
  // Retorna: Valor del CO2 (tipo int)
  // 
  // IMPORTANTE: Actualmente retorna un valor fijo (133)
  // En producción, este método debería:
  //   1. Comunicarse con el sensor de CO2 mediante puerto serie
  //   2. Leer el valor analógico del sensor
  //   3. Convertir el valor a ppm (partes por millón)
  //   4. Retornar el valor medido
  // 
  // Valores típicos de CO2 en aire exterior: 400-410 ppm
  // ====================================================================
  int medirCO2() {
    // Por ahora retorna un valor fijo para pruebas
    return 133;
    
    // TODO: Implementar lectura real del sensor
    // int valorCO2 = Serial1.read();  // Ejemplo: leer del sensor
    // return valorCO2;
  }

  // ====================================================================
  // MÉTODO: medirTemperatura()
  // ====================================================================
  // Lee el valor actual del sensor de temperatura
  // 
  // Retorna: Valor de temperatura (tipo int, en grados Celsius)
  // 
  // IMPORTANTE: Actualmente retorna un valor fijo (-12)
  // En producción, este método debería:
  //   1. Comunicarse con el sensor de temperatura
  //   2. Leer el valor analógico o digital
  //   3. Convertir el valor a grados Celsius o Fahrenheit
  //   4. Retornar la temperatura medida
  // 
  // El comentario "qué frío !" es humorístico (-12°C es muy frío)
  // ====================================================================
  int medirTemperatura() {
    // Por ahora retorna un valor fijo para pruebas
    return -12;  // ¡Qué frío!
    
    // TODO: Implementar lectura real del sensor
    // int valorTemperatura = analogRead(SENSOR_TEMP);  // Ejemplo
    // int celsius = (valorTemperatura - 32) * 5/9;      // Conversión
    // return celsius;
  }
  
}; // fin de la clase Medidor

// ====================================================================
// FIN DEL ARCHIVO
// ====================================================================
#endif
