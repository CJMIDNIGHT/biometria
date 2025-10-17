\# HolaMundoIBeacon — Módulo Arduino (Proyecto Biometría)



Este directorio contiene el firmware Arduino que implementa una emisora BLE (iBeacon) y lógica para leer/publicar mediciones (por ejemplo temperatura/gas) como parte del proyecto de biometría. 



\## Estructura relevante

\- `HolaMundoIBeacon.ino` — sketch principal; arranca los servicios y orquesta módulos.

\- `EmisoraBLE.h` — lógica para configurar la emisora iBeacon/BLE.

\- `ServicioEnEmisora.h` — definición de servicios/características BLE (si aplica).

\- `Medidor.h` — lectura y normalización de sensores (temperatura, gas, etc.).

\- `Publicador.h` — lógica para enviar/formatar mediciones (por ejemplo por BLE o por puerto serie).

\- `PuertoSerie.h` — utilidades para comunicación serie / debug.

\- `LED.h` — control del LED de estado.

\- `.\_HolaMundoIBeacon.ino` — archivo oculto / metadata (no necesario).



\## Requisitos

\- Entorno Arduino IDE o PlatformIO.

\- Placa con soporte BLE (p. ej. ESP32, nRF52 o cualquier placa con BLE), según el hardware usado en el proyecto.

\- Bibliotecas BLE apropiadas para la placa:

&nbsp;   - Para ESP32: `ESP32 BLE` / `BLEDevice` (instalar desde Gestor de Bibliotecas o PlatformIO).

&nbsp;   - Para placas con BLE nativo: `ArduinoBLE`.

\- Dependencias de sensor según el hardware (por ejemplo librerías para sensores de gas/temperatura).



\## Instalación / Preparación

1\. Abrir `HolaMundoIBeacon.ino` en Arduino IDE o importar el directorio en PlatformIO.

2\. Seleccionar la placa y puerto correctos.

3\. Instalar las bibliotecas BLE y las dependencias de los sensores que use el proyecto.

4\. Revisar y ajustar las constantes de configuración en el sketch (p. ej. nombre del dispositivo, UUIDs, baud rate en `PuertoSerie.h`, pines de sensores/LED).



\## Ejecutar / Subir

\- Compilar y subir el sketch a la placa desde el IDE.

\- Abrir el monitor serie (velocidad definida en el sketch, típicamente 115200) para ver logs y estado.

\- Verificar que la emisora BLE (iBeacon) sea detectable con un escáner BLE y que las mediciones se publiquen a través de los peticiones 
de POST del Android.




