# BluePrints
📖 Actividades del laboratorio
1. Familiarización con el código base
   Revisa el paquete model con las clases Blueprint y Point.

La clase Blueprint representa un plano, que contiene una lista de
puntos (points) asociados a un autor (author), el cual identifica a
la persona dueña de ese plano. Además del autor, la clase también 
guarda un name para identificar el plano en sí.

Para acceder a estos datos, Blueprint expone sus getters 
correspondientes: getAuthor(), getName() y getPoints(). Este último 
no entrega la lista interna directamente, sino una vista de solo 
lectura, evitando que se modifique desde fuera de la clase.
La única forma de agregar puntos al plano es a través del método 
addPoint(Point p), que actúa como punto de control sobre la lista:
cualquier modificación debe pasar obligatoriamente por él, 
garantizando que el estado interno del Blueprint se mantenga 
consistente y protegido (encapsulamiento).

Cada elemento de esa lista es un objeto Point, definido como 
un record: una clase inmutable y compacta que solo almacena un 
par de coordenadas (x, y), y que obtiene automáticamente su
constructor, getters, equals(), hashCode() y toString().

2. Entiende la capa persistence con InMemoryBlueprintPersistence.


Está compuesta por cuatro archivos: la interfaz
**BlueprintPersistence**, que define el contrato con las operaciones
disponibles (guardar un plano, buscarlo por autor y nombre, buscar 
todos los planos de un autor, traer todos los planos, y agregar un 
punto a un plano existente), para su implementación concreta esta la clase
**InMemoryBlueprintPersistence**, que guarda los datos en memoria 
usando un ConcurrentHashMap<String, Blueprint> esto nos permite tener 
varios usuarios al mismo tiempo, donde la clave es un texto 
generado como "author:name" mediante el método auxiliar keyOf, 
y que además precarga tres planos de ejemplo al iniciar la 
aplicación, tambien tenemos dos excepciones propias, **BlueprintNotFoundException**
(lanzada cuando se busca un plano que no existe) y 
**BlueprintPersistenceException** (lanzada cuando ocurre un error al 
guardar, como un plano duplicado), que permiten manejar los errores 
de forma específica en lugar de usar excepciones genéricas o devolver
null.

> La idea central de este diseño es que el resto de la aplicación
dependa únicamente de la interfaz BlueprintPersistence y no de la
clase concreta que la implementa, de modo que en el futuro se pueda
reemplazar el almacenamiento en memoria por una base de datos.

3. Analiza la capa services (BlueprintsServices) y el controlador BlueprintsAPIController.

La clase BlueprintsAPIController es la capa web, que utiliza API REST del proyecto, esta recibe peticiones con los metodos
GET,POST,PUT. tambien estrae y valida estas peticiones, si cumple se las pasa a la clase
BlueprintsServices, esta es la capa de servicio actúa como intermediaria entre el Controller y la
capa de Persistence. En la mayoría de sus métodos simplemente delega la llamada directamente
a persistence (guardar, traer todos, traer por autor, agregar punto)