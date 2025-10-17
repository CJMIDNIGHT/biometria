Este directorio contiene la parte cliente (front-end) del proyecto de biometría. Es una aplicación estática en JavaScript y HTML que consume la API REST del servidor.

Es una pequeña interfaz para obtener y mostrar la última medición (temperatura o gas) publicada por el servidor.

Contiene lo siguiente : index.html, Logica.js, y app.js .

Los programas están todos comentados detalladamente, porfavor leedlas bien.

Para poder revisar si funciona bien, es obligatorio que operáis en con una plataforma que permite ejecutar sitios web localmente.

Asegúrate de que tu servidor API esté configurado para permitir solicitudes REST que provengan de la dirección web 
de tu frontend (que se almacena en la variable FRONTEND_URL en el archivo .env del servidor).

Para verificar por mensajes de depuración, use la consola del navegador para logs: Logica.js ya incluyen mensajes de depuración.

\## Endpoints esperados (consistencia con servidor)

\- GET /api/medicion — devuelve la última medición en formato:

&nbsp;   ```json

&nbsp;   {

&nbsp;       "data": {

&nbsp;           "tipo": "temperatura" | "gas",

&nbsp;           "valor": 23.5,

&nbsp;           "fecha": "..."

&nbsp;       }

&nbsp;   }

&nbsp;   ```

\- GET /api/health — opcional para comprobación de estado.


Lo que se debería visualizar en el HTML son los variables no-vacíos del JSON mencionado anteriormente.