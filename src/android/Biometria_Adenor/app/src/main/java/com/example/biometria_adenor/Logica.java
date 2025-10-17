package com.example.biometria_adenor;

import android.util.Log;

public class Logica {

    private int tipoMedida;
    private int valorMedida;

    public Logica(int tipo, int valor) {
        this.tipoMedida = tipo;
        this.valorMedida = valor;
    }

    public void guardarMedcion(){
        PeticionarioREST elPeticionario = new PeticionarioREST();

        String tipoStr = "";

        if(this.tipoMedida == 11){
            tipoStr = "gas";
        } else if (this.tipoMedida == 12) {
            tipoStr = "temperatura";
        }

        Log.d("PROBLEMA DE LA TEMPERATURA", "EL tipoStr = " + tipoStr);

        String cuerpo =  "{\"tipo\": \"" + tipoStr + "\", \"valor\": " + this.valorMedida + "}";

        elPeticionario.hacerPeticionREST("POST",  "https://amburet.upv.edu.es/api/medicion",
                cuerpo, new PeticionarioREST.RespuestaREST () {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        Log.d( "pruebasPeticionario", "TENGO RESPUESTA:\ncodigo = " + codigo + "\ncuerpo: \n" + cuerpo);

                    }
                }
        );
    }
}
