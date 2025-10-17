\# Android — Proyecto Biometría



Este directorio contiene la aplicación Android que detecta beacons (iBeacon/BLE), procesa tramas y envía mediciones al servidor REST del proyecto de biometría.



\## Propósito

\- Escanear dispositivos BLE / iBeacon.

\- Parsear la trama del iBeacon y extraer datos relevantes (por ejemplo, temperatura/gas si aplica).

\- Enviar mediciones al servidor REST mediante peticiones POST.



\## Archivos importantes

\- Logica.java — Validaciones y lógica local (normalización de datos).

\- MainActivity.java — Actividad principal; gestión de UI y ciclo de vida.

\- PeticionarioREST.java — Cliente HTTP para comunicarse con la API (POST/GET).

\- TramaIBeacon.java — Parsing y representación de la trama iBeacon.

\- Utilidades.java — Constantes (p. ej. URL del servidor), utilidades comunes y helpers.


Las clases de Java están todas comentadas detalladamente, porfavor leedlas.


\## Requisitos

\- Android SDK (recomendado: SDK 30+).

\- Java 8+ o Kotlin según configuración del proyecto.

\- Gradle (el wrapper del proyecto).

\- Dispositivo o emulador con soporte BLE para pruebas en campo físico.



\## Permisos necesarios (AndroidManifest.xml)

Asegurarse de declarar al menos:

\- android.permission.INTERNET

\- android.permission.ACCESS\_FINE\_LOCATION (o ACCESS\_COARSE\_LOCATION según target)

\- android.permission.BLUETOOTH

\- android.permission.BLUETOOTH\_ADMIN

\- (En Android 12+) BLUETOOTH\_SCAN, BLUETOOTH\_CONNECT con manejo de permisos en tiempo de ejecución.



\## Ejecutar / Depurar

1\. Abrir el proyecto en Android Studio.

2\. Sincronizar Gradle.

3\. Conectar un dispositivo físico con BLE o usar emulador compatible.

4\. Ejecutar la app desde Android Studio (Run).

5\. Ver logs (Logcat). Verificar llamadas HTTP en PeticionarioREST y respuestas del servidor.



\## Pruebas locales

\- Probar el envío de mediciones con valores sintéticos para validar la integración con el servidor.

\- Verificar que el servidor responde 201 al crear mediciones y que GET /api/medicion retorna la última medición.
