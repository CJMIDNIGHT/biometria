package com.example.biometria_adenor;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// ------------------------------------------------------------------------
// Clase que realiza peticiones HTTP REST de forma asíncrona
// Extiende AsyncTask para ejecutar operaciones de red en segundo plano
// ------------------------------------------------------------------------
public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    // --------------------------------------------------------------------
    // Variables de instancia
    // --------------------------------------------------------------------
    // Almacena el método HTTP a usar (GET, POST, PUT, DELETE, etc.)
    private  String elMetodo;
    
    // Almacena la URL a la que se enviará la petición
    private String urlDestino;
    
    // Almacena el contenido/body de la petición (null si no hay cuerpo)
    private String elCuerpo = null;
    
    // Interfaz callback para devolver la respuesta al código que hizo la petición
    private RespuestaREST laRespuesta;
    
    // Almacena el código de estado HTTP de la respuesta (200, 404, 500, etc.)
    private int codigoRespuesta;
    
    // Almacena el contenido/body de la respuesta del servidor
    private String cuerpoRespuesta = "";

    // --------------------------------------------------------------------
    // Constructor vacío de la clase
    // --------------------------------------------------------------------
    public PeticionarioREST() {
        Log.d("clienterestandroid", "constructor()");
    }

    // --------------------------------------------------------------------
    // Método público para iniciar una petición REST
    // @param metodo - tipo de petición HTTP (GET, POST, etc.)
    // @param urlDestino - URL del servidor a donde enviar la petición
    // @param cuerpo - contenido JSON a enviar (puede ser null para GET)
    // @param laRespuesta - objeto callback que recibirá la respuesta
    // --------------------------------------------------------------------
    public void hacerPeticionREST(String metodo, String urlDestino, String cuerpo, RespuestaREST laRespuesta) {
        // Guarda el método HTTP en la variable de instancia
        this.elMetodo = metodo;
        
        // Guarda la URL destino en la variable de instancia
        this.urlDestino = urlDestino;
        
        // Guarda el cuerpo de la petición en la variable de instancia
        this.elCuerpo = cuerpo;
        
        // Guarda la referencia al callback en la variable de instancia
        this.laRespuesta = laRespuesta;

        // Ejecuta la tarea asíncrona (llama automáticamente a doInBackground en otro hilo)
        this.execute();
    }

    // --------------------------------------------------------------------
    // Método que se ejecuta en segundo plano (en otro hilo, no en el hilo principal)
    // Aquí se realiza la conexión HTTP y se envía/recibe información
    // @return Boolean - true si todo salió bien, false si hubo error
    // --------------------------------------------------------------------
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {

            // ---- ENVÍO LA PETICIÓN ----

            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            // Crea un objeto URL con la dirección del servidor
            URL url = new URL(urlDestino);

            // Abre una conexión HTTP hacia esa URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Establece el tipo de contenido como JSON con codificación UTF-8
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            
            // Establece el método HTTP (GET, POST, etc.)
            connection.setRequestMethod(this.elMetodo);

            // Habilita la recepción de datos desde el servidor
            connection.setDoInput(true);

            // Si NO es GET y hay un cuerpo para enviar
            if (!this.elMetodo.equals("GET") && this.elCuerpo != null) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                
                // Habilita el envío de datos hacia el servidor
                connection.setDoOutput(true);
                
                // Crea un stream para escribir datos hacia el servidor
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                Log.d("clienterestandroid","doInBackground(): lo que se escribe: " + this.elCuerpo );

                // Guarda el cuerpo en una variable temporal
                String elCuerpoFormateado = this.elCuerpo;
                
                // Convierte el texto del cuerpo a bytes usando codificación UTF-8
                byte[] postData = elCuerpoFormateado.getBytes(StandardCharsets.UTF_8);
                
                // Escribe los bytes en el stream (envía al servidor)
                dos.write(postData);
                
                // Asegura que todos los datos se envíen inmediatamente
                dos.flush();
                
                // Cierra el stream de salida
                dos.close();

            }

            // ---- YA HE ENVIADO LA PETICIÓN ----
            Log.d("clienterestandroid", "doInBackground(): peticin enviada ");

            // ---- AHORA OBTENGO LA RESPUESTA ----

            // Obtiene el código de respuesta HTTP (200, 404, 500, etc.)
            int rc = connection.getResponseCode();
            
            // Obtiene el mensaje de respuesta HTTP ("OK", "Not Found", etc.)
            String rm = connection.getResponseMessage();
            
            // Combina código y mensaje en una cadena para logging
            String respuesta = "" + rc + " : " + rm;
            Log.d("clienterestandroid", "doInBackground() recibo respuesta = " + respuesta);
            
            // Guarda el código de respuesta para devolverlo después
            this.codigoRespuesta = rc;

            try {

                // Obtiene el stream de entrada con los datos de respuesta del servidor
                InputStream is = connection.getInputStream();
                
                // Crea un lector de texto sobre el stream
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                Log.d("clienterestandroid", "leyendo cuerpo");
                
                // StringBuilder para ir acumulando las líneas de la respuesta
                StringBuilder acumulador = new StringBuilder();
                
                // Variable temporal para cada línea leída
                String linea;
                
                // Lee línea por línea hasta que no haya más (null)
                while ((linea = br.readLine()) != null) {
                    Log.d("clienterestandroid", linea);
                    // Añade cada línea al acumulador
                    acumulador.append(linea);
                }
                Log.d("clienterestandroid", "FIN leyendo cuerpo");

                // Convierte todo el contenido acumulado a String y lo guarda
                this.cuerpoRespuesta = acumulador.toString();
                Log.d("clienterestandroid", "cuerpo recibido=" + this.cuerpoRespuesta);

                // Cierra la conexión HTTP
                connection.disconnect();

            } catch (IOException ex) {
                // Esta excepción ocurre cuando la respuesta no tiene cuerpo
                // (por ejemplo, respuestas 204 No Content)
                Log.d("clienterestandroid", "doInBackground() : parece que no hay cuerpo en la respuesta");
            }

            // Devuelve true indicando que todo el proceso terminó correctamente
            return true;

        } catch (Exception ex) {
            // Captura cualquier otra excepción no prevista
            Log.d("clienterestandroid", "doInBackground(): ocurrio alguna otra excepcion: " + ex.getMessage());
        }

        // Devuelve false indicando que hubo un error en el proceso
        return false;
    }

    // --------------------------------------------------------------------
    // Método que se ejecuta automáticamente después de doInBackground()
    // Se ejecuta en el hilo principal (UI thread)
    // @param comoFue - el valor Boolean que devolvió doInBackground()
    // --------------------------------------------------------------------
    protected void onPostExecute(Boolean comoFue) {
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        
        // Llama al método callback de la interfaz RespuestaREST
        // Devuelve el código de respuesta y el cuerpo al código que hizo la petición
        this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
    }

    // --------------------------------------------------------------------
    // Interfaz que define el método callback para recibir la respuesta
    // Debe ser implementada por quien quiera recibir la respuesta de la petición
    // --------------------------------------------------------------------
    public interface RespuestaREST {
        // Método que se ejecutará cuando llegue la respuesta del servidor
        // @param codigo - código HTTP de respuesta (200, 404, etc.)
        // @param cuerpo - contenido de la respuesta en formato String
        void callback(int codigo, String cuerpo);
    }

} // class
