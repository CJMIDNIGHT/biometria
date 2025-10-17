package com.example.biometria_adenor;
import android.util.Log;

public class Logica {
    // Variable que almacena el tipo de medida (11 para gas, 12 para temperatura)
    private int tipoMedida;
    
    // Variable que almacena el valor numérico de la medida tomada
    private int valorMedida;
    
    /**
     * Constructor de la clase Logica
     * @param tipo - el tipo de medida a guardar (11=gas, 12=temperatura)
     * @param valor - el valor numérico de la medida
     */
    public Logica(int tipo, int valor) {
        // Inicializa el tipo de medida con el parámetro recibido
        this.tipoMedida = tipo;
        // Inicializa el valor de medida con el parámetro recibido
        this.valorMedida = valor;
    }
    
    /**
     * Método que guarda la medición realizando una petición POST al servidor
     */
    public void guardarMedcion(){
        // Crea un objeto PeticionarioREST para hacer peticiones HTTP
        PeticionarioREST elPeticionario = new PeticionarioREST();
        
        // Variable para almacenar el tipo de medida como texto
        String tipoStr = "";
        
        // Si el tipo de medida es 11, corresponde a "gas"
        if(this.tipoMedida == 11){
            tipoStr = "gas";
        // Si el tipo de medida es 12, corresponde a "temperatura"
        } else if (this.tipoMedida == 12) {
            tipoStr = "temperatura";
        }
        
        // Registra en el log el tipo de medida convertido a texto (para depuración)
        Log.d("PROBLEMA DE LA TEMPERATURA", "EL tipoStr = " + tipoStr);
        
        // Construye el cuerpo de la petición en formato JSON con el tipo y valor de la medida
        String cuerpo =  "{\"tipo\": \"" + tipoStr + "\", \"valor\": " + this.valorMedida + "}";
        
        // Realiza una petición POST al servidor con los datos de la medición
        elPeticionario.hacerPeticionREST("POST",  "https://amburet.upv.edu.es/api/medicion",
                cuerpo, new PeticionarioREST.RespuestaREST () {
                    /**
                     * Método callback que se ejecuta cuando se recibe respuesta del servidor
                     * @param codigo - el código de respuesta HTTP (ej: 200, 404, 500)
                     * @param cuerpo - el contenido de la respuesta del servidor
                     */
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        // Registra en el log la respuesta recibida del servidor (para depuración)
                        Log.d( "pruebasPeticionario", "TENGO RESPUESTA:\ncodigo = " + codigo + "\ncuerpo: \n" + cuerpo);
                    }
                }
        );
    }
}
