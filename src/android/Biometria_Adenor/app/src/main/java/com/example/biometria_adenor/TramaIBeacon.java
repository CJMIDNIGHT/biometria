package com.example.biometria_adenor;
import java.util.Arrays;

// -----------------------------------------------------------------------------------
// Clase que representa y parsea una trama iBeacon
// iBeacon es un protocolo de Apple para transmitir identificadores via Bluetooth
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class TramaIBeacon {
    // Prefijo de la trama iBeacon (contiene flags, header, companyID, type, length) - 9 bytes
    private byte[] prefijo = null;
    
    // Identificador único universal del beacon - 16 bytes
    private byte[] uuid = null;
    
    // Identificador mayor (para agrupar beacons) - 2 bytes
    private byte[] major = null;
    
    // Identificador menor (para identificar beacons individuales) - 2 bytes
    private byte[] minor = null;
    
    // Potencia de transmisión calibrada (para calcular distancia) - 1 byte
    private byte txPower = 0;

    // Array completo de bytes de la trama iBeacon
    private byte[] losBytes;

    // Flags de advertising Bluetooth (indica tipo de dispositivo) - 3 bytes
    private byte[] advFlags = null;
    
    // Cabecera de advertising (longitud y tipo de datos) - 2 bytes
    private byte[] advHeader = null;
    
    // Identificador de la compañía (0x004C para Apple) - 2 bytes
    private byte[] companyID = new byte[2];
    
    // Tipo de iBeacon (siempre 0x02 para iBeacon) - 1 byte
    private byte iBeaconType = 0;
    
    // Longitud de los datos iBeacon (siempre 0x15 = 21 bytes) - 1 byte
    private byte iBeaconLength = 0;

    // -------------------------------------------------------------------------------
    // Devuelve el prefijo de la trama (primeros 9 bytes)
    // -------------------------------------------------------------------------------
    public byte[] getPrefijo() {
        return prefijo;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el UUID del beacon (identificador único de 16 bytes)
    // -------------------------------------------------------------------------------
    public byte[] getUUID() {
        return uuid;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el Major del beacon (identificador de grupo de 2 bytes)
    // -------------------------------------------------------------------------------
    public byte[] getMajor() {
        return major;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el Minor del beacon (identificador individual de 2 bytes)
    // -------------------------------------------------------------------------------
    public byte[] getMinor() {
        return minor;
    }

    // -------------------------------------------------------------------------------
    // Devuelve la potencia de transmisión calibrada (TxPower) en dBm
    // Se usa para estimar la distancia al beacon
    // -------------------------------------------------------------------------------
    public byte getTxPower() {
        return txPower;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el array completo de bytes de la trama
    // -------------------------------------------------------------------------------
    public byte[] getLosBytes() {
        return losBytes;
    }

    // -------------------------------------------------------------------------------
    // Devuelve los flags de advertising (primeros 3 bytes del prefijo)
    // -------------------------------------------------------------------------------
    public byte[] getAdvFlags() {
        return advFlags;
    }

    // -------------------------------------------------------------------------------
    // Devuelve la cabecera de advertising (bytes 3-4 del prefijo)
    // -------------------------------------------------------------------------------
    public byte[] getAdvHeader() {
        return advHeader;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el ID de la compañía (bytes 5-6 del prefijo)
    // Para iBeacon de Apple siempre es 0x004C
    // -------------------------------------------------------------------------------
    public byte[] getCompanyID() {
        return companyID;
    }

    // -------------------------------------------------------------------------------
    // Devuelve el tipo de iBeacon (byte 7 del prefijo)
    // Siempre es 0x02 para iBeacon
    // -------------------------------------------------------------------------------
    public byte getiBeaconType() {
        return iBeaconType;
    }

    // -------------------------------------------------------------------------------
    // Devuelve la longitud de los datos iBeacon (byte 8 del prefijo)
    // Siempre es 0x15 (21 en decimal) para iBeacon
    // -------------------------------------------------------------------------------
    public byte getiBeaconLength() {
        return iBeaconLength;
    }

    // -------------------------------------------------------------------------------
    // Constructor que parsea un array de bytes y extrae todos los campos iBeacon
    // @param bytes - array de bytes recibido del escaneo Bluetooth
    // -------------------------------------------------------------------------------
    public TramaIBeacon(byte[] bytes ) {
        // Verifica si los bytes ya incluyen los flags de advertising al inicio
        // Los flags son: 0x02, 0x01, 0x06
        if (bytes.length >= 3 &&
                (bytes[0] & 0xFF) == 0x02 &&
                (bytes[1] & 0xFF) == 0x01 &&
                (bytes[2] & 0xFF) == 0x06) {
            // Ya tiene flags, usa los bytes tal cual
            this.losBytes = bytes;
        } else {
            // No tiene flags, hay que añadirlos al inicio
            byte[] flags = new byte[]{ 0x02, 0x01, 0x06 };
            
            // Crea un nuevo array con espacio para flags + datos originales
            this.losBytes = new byte[flags.length + bytes.length];
            
            // Copia los flags al inicio del nuevo array
            System.arraycopy(flags, 0, this.losBytes, 0, flags.length);
            
            // Copia los bytes originales después de los flags
            System.arraycopy(bytes, 0, this.losBytes, flags.length, bytes.length);
        }

        // Extrae el prefijo (bytes 0-8, total 9 bytes)
        prefijo = Arrays.copyOfRange(losBytes, 0, 8+1);
        
        // Extrae el UUID (bytes 9-24, total 16 bytes)
        uuid = Arrays.copyOfRange(losBytes, 9, 24+1);
        
        // Extrae el Major (bytes 25-26, total 2 bytes)
        major = Arrays.copyOfRange(losBytes, 25, 26+1);
        
        // Extrae el Minor (bytes 27-28, total 2 bytes)
        minor = Arrays.copyOfRange(losBytes, 27, 28+1);
        
        // Extrae el TxPower (byte 29, total 1 byte)
        txPower = losBytes[ 29 ];

        // Del prefijo extrae los advFlags (bytes 0-2, total 3 bytes)
        advFlags = Arrays.copyOfRange( prefijo, 0, 2+1);
        
        // Del prefijo extrae el advHeader (bytes 3-4, total 2 bytes)
        advHeader = Arrays.copyOfRange( prefijo, 3, 4+1);
        
        // Del prefijo extrae el companyID (bytes 5-6, total 2 bytes)
        companyID = Arrays.copyOfRange( prefijo, 5, 6+1);
        
        // Del prefijo extrae el iBeaconType (byte 7, total 1 byte)
        iBeaconType = prefijo[ 7 ];
        
        // Del prefijo extrae el iBeaconLength (byte 8, total 1 byte)
        iBeaconLength = prefijo[ 8 ];

    } // Constructor
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
