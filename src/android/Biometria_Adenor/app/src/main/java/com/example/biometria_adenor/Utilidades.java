package com.example.biometria_adenor;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// -----------------------------------------------------------------------------------
// Clase con métodos estáticos de utilidad para conversión entre diferentes formatos
// (bytes, strings, UUID, enteros, hexadecimales, etc.)
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class Utilidades {

    // -------------------------------------------------------------------------------
    // Convierte un String a un array de bytes
    // @param texto - el texto a convertir
    // @return array de bytes que representa el texto
    // -------------------------------------------------------------------------------
    public static byte[] stringToBytes ( String texto ) {
        // Convierte el String a bytes usando la codificación por defecto
        return texto.getBytes();
    }

    // -------------------------------------------------------------------------------
    // Convierte un String de exactamente 16 caracteres a un objeto UUID
    // @param uuid - String de 16 caracteres que representa un UUID
    // @return objeto UUID creado a partir del String
    // @throws Error si el String no tiene exactamente 16 caracteres
    // -------------------------------------------------------------------------------
    public static UUID stringToUUID( String uuid ) {
        // Verifica que el String tenga exactamente 16 caracteres
        if ( uuid.length() != 16 ) {
            throw new Error( "stringUUID: string no tiene 16 caracteres ");
        }
        
        // Convierte el String completo a bytes
        byte[] comoBytes = uuid.getBytes();

        // Extrae los primeros 8 caracteres (parte más significativa del UUID)
        String masSignificativo = uuid.substring(0, 8);
        
        // Extrae los últimos 8 caracteres (parte menos significativa del UUID)
        String menosSignificativo = uuid.substring(8, 16);
        
        // Crea un UUID usando las dos partes (más y menos significativa) como long
        UUID res = new UUID( Utilidades.bytesToLong( masSignificativo.getBytes() ), 
                            Utilidades.bytesToLong( menosSignificativo.getBytes() ) );

        return res;
    }

    // -------------------------------------------------------------------------------
    // Convierte un objeto UUID a String
    // @param uuid - objeto UUID a convertir
    // @return representación en String del UUID
    // -------------------------------------------------------------------------------
    public static String uuidToString ( UUID uuid ) {
        // Obtiene los bits más y menos significativos del UUID y los convierte a String
        return bytesToString( dosLongToBytes( uuid.getMostSignificantBits(), 
                                             uuid.getLeastSignificantBits() ) );
    }

    // -------------------------------------------------------------------------------
    // Convierte un objeto UUID a String en formato hexadecimal
    // @param uuid - objeto UUID a convertir
    // @return representación hexadecimal del UUID (con ':' entre bytes)
    // -------------------------------------------------------------------------------
    public static String uuidToHexString ( UUID uuid ) {
        // Obtiene los bits más y menos significativos del UUID y los convierte a hex
        return bytesToHexString( dosLongToBytes( uuid.getMostSignificantBits(), 
                                                uuid.getLeastSignificantBits() ) );
    }

    // -------------------------------------------------------------------------------
    // Convierte un array de bytes a String
    // Cada byte se interpreta como un carácter ASCII
    // @param bytes - array de bytes a convertir
    // @return String resultante (vacío si bytes es null)
    // -------------------------------------------------------------------------------
    public static String bytesToString( byte[] bytes ) {
        // Si el array es null, devuelve String vacío
        if (bytes == null ) {
            return "";
        }

        // StringBuilder para construir el String eficientemente
        StringBuilder sb = new StringBuilder();
        
        // Recorre cada byte y lo añade como carácter al StringBuilder
        for (byte b : bytes) {
            sb.append( (char) b );
        }
        
        return sb.toString();
    }

    // -------------------------------------------------------------------------------
    // Convierte dos valores long (más y menos significativos) a un array de bytes
    // Se usa principalmente para convertir UUID a bytes
    // @param masSignificativos - los 8 bytes más significativos como long
    // @param menosSignificativos - los 8 bytes menos significativos como long
    // @return array de 16 bytes combinando ambos long
    // -------------------------------------------------------------------------------
    public static byte[] dosLongToBytes( long masSignificativos, long menosSignificativos ) {
        // Crea un buffer con espacio para 2 valores long (2 * 8 bytes = 16 bytes)
        ByteBuffer buffer = ByteBuffer.allocate( 2 * Long.BYTES );
        
        // Escribe el primer long en el buffer
        buffer.putLong( masSignificativos );
        
        // Escribe el segundo long en el buffer
        buffer.putLong( menosSignificativos );
        
        // Devuelve el contenido del buffer como array de bytes
        return buffer.array();
    }

    // -------------------------------------------------------------------------------
    // Convierte un array de bytes a un entero (int)
    // Usa BigInteger para la conversión
    // @param bytes - array de bytes a convertir
    // @return valor entero representado por los bytes
    // -------------------------------------------------------------------------------
    public static int bytesToInt( byte[] bytes ) {
        // Crea un BigInteger a partir de los bytes y lo convierte a int
        return new BigInteger(bytes).intValue();
    }

    // -------------------------------------------------------------------------------
    // Convierte un array de bytes a un long
    // Usa BigInteger para la conversión
    // @param bytes - array de bytes a convertir
    // @return valor long representado por los bytes
    // -------------------------------------------------------------------------------
    public static long bytesToLong( byte[] bytes ) {
        // Crea un BigInteger a partir de los bytes y lo convierte a long
        return new BigInteger(bytes).longValue();
    }

    // -------------------------------------------------------------------------------
    // Convierte un array de bytes a entero (int) manejando signo y bytes uno por uno
    // Versión alternativa de bytesToInt con manejo manual de bits
    // @param bytes - array de bytes a convertir (máximo 4 bytes)
    // @return valor entero con signo representado por los bytes
    // @throws Error si hay más de 4 bytes (un int solo tiene 4 bytes)
    // -------------------------------------------------------------------------------
    public static int bytesToIntOK( byte[] bytes ) {
        // Si el array es null, devuelve 0
        if (bytes == null ) {
            return 0;
        }

        // Verifica que no haya más de 4 bytes (tamaño máximo de un int)
        if ( bytes.length > 4 ) {
            throw new Error( "demasiados bytes para pasar a int ");
        }
        
        // Variable para acumular el resultado
        int res = 0;

        // Recorre cada byte del array
        for( byte b : bytes ) {
            // Desplaza el resultado actual 8 bits a la izquierda (multiplica por 256)
            res =  (res << 8)
                    // Añade el byte actual (& 0xFF asegura que se trata como unsigned)
                    + (b & 0xFF);
        }

        // Verifica si el número es negativo (bit más significativo = 1)
        if ( (bytes[ 0 ] & 0x8) != 0 ) {
            // Aplica complemento a 2 para obtener el valor negativo correcto
            res = -(~(byte)res)-1;
        }

        return res;
    }

    // -------------------------------------------------------------------------------
    // Convierte un array de bytes a String en formato hexadecimal
    // Cada byte se representa como 2 dígitos hex separados por ':'
    // @param bytes - array de bytes a convertir
    // @return String hexadecimal (ej: "a1:b2:c3") o vacío si bytes es null
    // -------------------------------------------------------------------------------
    public static String bytesToHexString( byte[] bytes ) {

        // Si el array es null, devuelve String vacío
        if (bytes == null ) {
            return "";
        }

        // StringBuilder para construir el String hexadecimal
        StringBuilder sb = new StringBuilder();
        
        // Recorre cada byte del array
        for (byte b : bytes) {
            // Convierte el byte a 2 dígitos hexadecimales (formato "%02x")
            sb.append(String.format("%02x", b));
            
            // Añade ':' como separador entre bytes
            sb.append(':');
        }
        
        return sb.toString();
    }
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
