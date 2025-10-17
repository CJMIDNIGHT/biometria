Esta carpeta se encarga de contener las programas del servidor.



La manera en la cual se inicializa es mediante node.js en el servidor Plesk.



Está compuesto de tres archivos : LogicaDeNegocio.js, database.js, logger.js, mi\_log.txt y api.js .



Todos las programas están comentados detalladamente, menos logger.js que está comentado ligeramente.



Para poder comprobar que funcionen con existo o para leer errores, hay que estar atentos de los mensajes de LogCat en el Android Studio 

para revisar los mensajes que renvia la api.js (que sea para confirmar o encontrar errores). También os vendría útil utilizar logger.js y 

mi\_log.txt, ya que logger.js redericciona los console.logs hacia mi\_log.txt, desde allí se puede revisar mensajes de error, éxito y proceso 

que esté pasando por cualquier parte en el servidor.



