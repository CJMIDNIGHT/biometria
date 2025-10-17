package com.example.biometria_adenor;
// ------------------------------------------------------------------
// Clase principal de la aplicación que escanea beacons BLE
// ------------------------------------------------------------------

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.UUID;

// ------------------------------------------------------------------
// MainActivity: Actividad principal que gestiona el escaneo de
// dispositivos Bluetooth Low Energy (BLE) tipo iBeacon
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // CONSTANTES
    // --------------------------------------------------------------
    
    // Etiqueta usada para filtrar mensajes de log en Logcat
    // El prefijo ">>>>" hace fácil buscar los logs de esta app
    private static final String ETIQUETA_LOG = ">>>>";

    // Código único para identificar la solicitud de permisos
    // Se usa en onRequestPermissionsResult() para saber qué respuesta procesar
    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // --------------------------------------------------------------
    // VARIABLES DE INSTANCIA
    // --------------------------------------------------------------
    
    // Objeto que permite escanear dispositivos Bluetooth Low Energy
    // Se obtiene del BluetoothAdapter y se usa para iniciar/detener búsquedas
    private BluetoothLeScanner elEscanner;

    // Callback que define qué hacer cuando se detectan dispositivos BLE
    // Contiene los métodos onScanResult(), onBatchScanResults(), onScanFailed()
    // Se inicializa a null y se crea cuando se inicia un escaneo
    private ScanCallback callbackDelEscaneo = null;

    // Almacena el último contador recibido del Arduino/beacon
    // Se usa para evitar procesar mediciones duplicadas
    // Cada nueva medición del Arduino tiene un contador incrementado
    private int contadorAndroid = 0;

    // --------------------------------------------------------------
    // MÉTODO: buscarTodosLosDispositivosBTLE()
    // Inicia un escaneo SIN FILTROS para detectar TODOS los 
    // dispositivos BLE cercanos
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        // Creamos un callback anónimo que define qué hacer cuando se detecten dispositivos
        this.callbackDelEscaneo = new ScanCallback() {
            
            // Se ejecuta cada vez que se detecta UN dispositivo BLE
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                // Muestra toda la información del dispositivo detectado en el log
                mostrarInformacionDispositivoBTLE( resultado );
            }

            // Se ejecuta cuando se detectan VARIOS dispositivos a la vez (batch)
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");
                // Actualmente no procesa los resultados, solo registra el evento
            }

            // Se ejecuta si el escaneo falla por algún error
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");
                // Solo registra el error, no toma acción correctiva
            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        // Inicia el escaneo sin filtros (detecta cualquier dispositivo BLE)
        // Usa parámetros por defecto (velocidad media, sin tiempo límite)
        this.elEscanner.startScan( this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // MÉTODO: mostrarInformacionDispositivoBTLE()
    // Extrae y muestra en el log toda la información de un beacon
    // detectado, incluyendo el análisis de la trama iBeacon
    // --------------------------------------------------------------
    private void mostrarInformacionDispositivoBTLE( ScanResult resultado ) {

        // Obtiene el objeto del dispositivo Bluetooth detectado
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        
        // Obtiene los bytes crudos del advertising packet (datos transmitidos)
        byte[] bytes = resultado.getScanRecord().getBytes();
        
        // Obtiene RSSI (Received Signal Strength Indicator)
        // Valor negativo en dBm: más cercano a 0 = señal más fuerte
        // Se usa para estimar distancia al beacon
        int rssi = resultado.getRssi();

        // Muestra información básica del dispositivo
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());
        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );
        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        // Crea objeto que parsea/analiza el formato iBeacon
        // Extrae automáticamente las diferentes secciones del paquete
        TramaIBeacon tib = new TramaIBeacon(bytes);

        // Muestra la estructura detallada del iBeacon
        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        
        // PREFIJO: Primeros 9 bytes que identifican el tipo de paquete
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        
        // advFlags: Banderas BLE (3 bytes) - indican tipo de dispositivo
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        
        // advHeader: Cabecera del advertising (2 bytes)
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        
        // companyID: Identificador del fabricante (2 bytes)
        // Apple usa 0x004C, otros fabricantes tienen sus propios IDs
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        
        // iBeacon type: Tipo de beacon (1 byte) - 0x02 para iBeacon
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        
        // iBeacon length: Longitud del payload (1 byte) - debería ser 21 (0x15)
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        
        // UUID: Identificador único del beacon (16 bytes)
        // Identifica la aplicación o grupo de beacons
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));

        // MAJOR: En este proyecto se usa de forma no estándar (2 bytes)
        byte[] major = tib.getMajor();
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(major) + "( "
                + Utilidades.bytesToInt(major) + " ) ");

        // El primer byte del major indica el TIPO DE MEDICIÓN
        // Por ejemplo: 0=temperatura, 1=CO2, 2=humedad, etc.
        // & 0xFF convierte el byte con signo a entero sin signo (0-255)
        int tipoMedicion = major[0] & 0xFF ;
        Log.d(ETIQUETA_LOG, " tipo medicion  = " + tipoMedicion);

        // El segundo byte del major es un CONTADOR
        // Se incrementa con cada nueva medición para detectar duplicados
        int contador = major[1] & 0xFF;
        Log.d(ETIQUETA_LOG, " contador  = " + contador);

        // MINOR: Contiene el VALOR de la medición (2 bytes)
        // Por ejemplo: 235 podría representar 23.5°C
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " medicion  = " + Utilidades.bytesToInt(tib.getMinor()));

        // txPower: Potencia de transmisión calibrada (1 byte)
        // Se mide en dBm a 1 metro de distancia
        // Usado para calcular la distancia aproximada al beacon
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // --------------------------------------------------------------
    // MÉTODO: buscarEsteDispositivoBTLE()
    // Inicia un escaneo CON FILTRO para buscar un dispositivo
    // específico por nombre. Además de mostrar info, GUARDA las mediciones
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        // Creamos callback similar al anterior pero que también guarda mediciones
        this.callbackDelEscaneo = new ScanCallback() {
            
            // Se ejecuta cada vez que se detecta el dispositivo buscado
            @Override
            public void onScanResult( int callbackType, ScanResult resultado ) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                // Muestra la información del beacon en el log
                mostrarInformacionDispositivoBTLE( resultado );
                
                // IMPORTANTE: También guarda la medición en la base de datos
                // Esto solo se hace en búsquedas filtradas (nuestro dispositivo)
                guardarMedicion( resultado );
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");
            }
        };

        // Crea un filtro para buscar solo dispositivos con el nombre especificado
        // Solo los beacons cuyo nombre coincida exactamente serán detectados
        ScanFilter sf = new ScanFilter.Builder().setDeviceName( dispositivoBuscado ).build();

        // Añade el filtro a una lista (aunque solo tengamos uno)
        List<ScanFilter> filtros = new java.util.ArrayList<>();
        filtros.add(sf);

        // Configura los parámetros del escaneo
        // SCAN_MODE_LOW_LATENCY = escaneo rápido y frecuente
        // Consume más batería pero detecta beacons más rápidamente
        // Otras opciones: LOW_POWER (ahorra batería) o BALANCED (equilibrado)
        android.bluetooth.le.ScanSettings settings =
                new android.bluetooth.le.ScanSettings.Builder()
                        .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado );

        // Inicia el escaneo con filtros y configuración personalizada
        this.elEscanner.startScan(filtros, settings, this.callbackDelEscaneo );
    } // ()

    // --------------------------------------------------------------
    // MÉTODO: detenerBusquedaDispositivosBTLE()
    // Detiene el escaneo BLE actual (si hay uno activo)
    // Importante para ahorrar batería
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        // Si no hay callback, significa que no hay escaneo activo
        if ( this.callbackDelEscaneo == null ) {
            return;
        }

        // Detiene el escaneo usando el callback que se usó para iniciarlo
        this.elEscanner.stopScan( this.callbackDelEscaneo );
        
        // Limpia el callback (lo pone a null)
        // Esto libera memoria e indica que no hay escaneo activo
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // MÉTODO: guardarMedicion()
    // Extrae los datos de medición del beacon y los guarda en BD
    // Implementa lógica para evitar guardar mediciones duplicadas
    // --------------------------------------------------------------
    private void guardarMedicion( ScanResult resultado){

        // Obtiene los bytes crudos del advertising packet
        byte[] bytes = resultado.getScanRecord().getBytes();
        
        // Parsea los bytes como trama iBeacon
        TramaIBeacon tib = new TramaIBeacon(bytes);

        // Extrae el major (2 bytes) que contiene info codificada
        byte[] major = tib.getMajor();
        
        // Primer byte del major = TIPO DE MEDICIÓN
        // Identifica qué sensor: temperatura, CO2, humedad, etc.
        // & 0xFF convierte byte con signo a entero sin signo (0-255)
        int tipoMedicion = major[0] & 0xFF ;
        
        // Segundo byte del major = CONTADOR
        // Se incrementa con cada nueva medición del Arduino
        // Permite detectar cuando llega una medición nueva vs. repetida
        int contadorArduino = major[1] & 0xFF;

        // Minor (2 bytes) = VALOR DE LA MEDICIÓN
        // Contiene el dato del sensor (ej: 235 = 23.5°C)
        int valorMedicion = Utilidades.bytesToInt(tib.getMinor());

        // VERIFICACIÓN DE DUPLICADOS:
        // Si el contador es igual al anterior, es la misma medición
        // Los beacons transmiten continuamente, así que recibiremos
        // el mismo paquete varias veces hasta que Arduino envíe uno nuevo
        if ( contadorArduino == this.contadorAndroid ) {
            Log.d(ETIQUETA_LOG, "Se repitio el contador no se envia este becon");
            return; // Salir sin guardar (es duplicado)
        }

        // Si llegamos aquí, es una medición NUEVA
        // Actualizamos nuestro contador local para futuras comparaciones
        this.contadorAndroid = contadorArduino;

        // Crea objeto de lógica de negocio con los datos extraídos
        Logica logica = new Logica(tipoMedicion, valorMedicion);
        
        // Guarda la medición en la base de datos
        // (Nota: hay un typo en el nombre del método original)
        logica.guardarMedcion();
    }

    // --------------------------------------------------------------
    // MÉTODOS PÚBLICOS - Conectados a botones de la interfaz
    // --------------------------------------------------------------
    
    // Se ejecuta cuando el usuario pulsa el botón "Buscar Todos"
    // Busca cualquier dispositivo BLE sin filtros
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // Se ejecuta cuando el usuario pulsa "Buscar Nuestro Dispositivo"
    // Busca específicamente el beacon llamado "GTI"
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        
        // Líneas comentadas muestran intentos anteriores:
        // - Intentaron buscar por UUID convertido desde string
        // - Probaron con el nombre completo "EPSG-GTI-PROY-3A"
        // - Finalmente usan el nombre corto "GTI"
        
        this.buscarEsteDispositivoBTLE( "GTI");
    } // ()

    // --------------------------------------------------------------
    // Se ejecuta cuando el usuario pulsa "Detener Búsqueda"
    // Detiene el escaneo activo para ahorrar batería
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // MÉTODO: inicializarBlueTooth()
    // Configura todo lo necesario para usar Bluetooth en la app:
    // - Obtiene el adaptador BT
    // - Verifica/activa Bluetooth
    // - Obtiene el escáner BLE
    // - Solicita permisos necesarios
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        // Obtiene el adaptador Bluetooth del dispositivo
        // Representa el hardware Bluetooth del teléfono/tablet
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        // Verifica si Bluetooth está activado
        if (!bta.isEnabled()) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Bluetooth desactivado, solicitando activación...");

            // Si está desactivado, muestra diálogo del sistema para activarlo
            // El usuario puede aceptar o rechazar
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, CODIGO_PETICION_PERMISOS);
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Bluetooth ya está activado");
        }

        // Muestra estado actual del Bluetooth
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        // Obtiene el escáner BLE del adaptador
        // Este objeto permite buscar dispositivos BLE
        this.elEscanner = bta.getBluetoothLeScanner();

        // Verifica que se obtuvo correctamente el escáner
        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");
            // Si es null, Bluetooth no está disponible o activo
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        // SOLICITUD DE PERMISOS (Android 6.0+)
        // Verifica si la app tiene los permisos necesarios
        if (
                // BLUETOOTH_SCAN: Permiso para escanear dispositivos BLE (Android 12+)
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                        // BLUETOOTH_CONNECT: Permiso para conectarse a dispositivos (Android 12+)
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        // ACCESS_FINE_LOCATION: Requerido porque BLE puede revelar ubicación
                        // Google lo requiere por privacidad, aunque no usemos GPS
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            // Si falta algún permiso, solicita todos los necesarios
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,      // Escanear BLE
                            Manifest.permission.BLUETOOTH_CONNECT,   // Conectar BLE
                            Manifest.permission.ACCESS_FINE_LOCATION // Ubicación (obligatorio para BLE)
                    },
                    CODIGO_PETICION_PERMISOS // Código para identificar esta solicitud
            );

        }
        else {
            // Si ya tiene todos los permisos, continúa normalmente
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");
        }
    } // ()

    // --------------------------------------------------------------
    // MÉTODO DEL CICLO DE VIDA: onCreate()
    // Primer método que se ejecuta cuando se crea la actividad
    // Es el punto de entrada de la aplicación
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama al onCreate de la clase padre (AppCompatActivity)
        // Ejecuta inicialización básica de Android
        super.onCreate(savedInstanceState);
        
        // Establece el layout (interfaz visual) desde el XML
        // Carga botones, textos, y otros elementos visuales
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        // Inicializa Bluetooth: obtiene adaptador, escáner, pide permisos
        // Es crucial hacerlo aquí para que todo esté listo antes de escanear
        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // --------------------------------------------------------------
    // CALLBACK: onRequestPermissionsResult()
    // Se ejecuta automáticamente cuando el usuario responde a la
    // solicitud de permisos (acepta o rechaza)
    // --------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // Llama al método del padre
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        // Verifica qué solicitud de permisos es (usando el código)
        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                
                // Verifica si hay resultados y si el primer permiso fue concedido
                // grantResults contiene PERMISSION_GRANTED o PERMISSION_DENIED
                // para cada permiso solicitado
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // PERMISOS CONCEDIDOS: La app puede usar Bluetooth BLE
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    
                    // Aquí podría iniciarse el escaneo automáticamente
                    // o mostrar mensaje de éxito al usuario
                    
                }  else {
                    // PERMISOS DENEGADOS: La app NO puede usar BLE
                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                    // LIMITACIÓN: El código actual solo registra el error
                    // Debería mostrar un diálogo explicando por qué son necesarios
                    // o desactivar funcionalidad BLE
                }
                return;
        }
        // Si hubiera otras solicitudes de permisos, se procesarían aquí
        // con más casos en el switch
    } // ()

} // class MainActivity
// --------------------------------------------------------------
// FIN DE LA CLASE
// --------------------------------------------------------------
