# BluePrints
📖 Actividades del laboratorio

### 1. Familiarización con el código base
   
#### - Revisa el paquete model con las clases Blueprint y Point.

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

#### - Entiende la capa persistence con InMemoryBlueprintPersistence.


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

#### - Analiza la capa services (BlueprintsServices) y el controlador BlueprintsAPIController.

La clase BlueprintsAPIController es la capa web, que utiliza API REST del proyecto, esta recibe peticiones con los metodos
GET,POST,PUT. tambien estrae y valida estas peticiones, si cumple se las pasa a la clase
BlueprintsServices, esta es la capa de servicio actúa como intermediaria entre el Controller y la
capa de Persistence. En la mayoría de sus métodos simplemente delega la llamada directamente
a persistence (guardar, traer todos, traer por autor, agregar punto)

### **2. Migración a persistencia en PostgreSQL**

#### - Configura una base de datos PostgreSQL (puedes usar Docker).

1) Creamos el archivo docker-compose.yml
2) levantamos el contenedor.
> docker compose up -d
3) Verificamos que este corriendo.
> docker ps
4) confirmamos que podamos conectarnos
>  docker exec -it blueprints-postgres psql -U blueprints_user -d blueprints_db
5) Comandos utiles
>Para salir de esa terminal
> \q
> listar tablas
> \dt
> listar todas las bases de datos
> \l  


#### - Implementa un nuevo repositorio PostgresBlueprintPersistence que reemplace la versión en memoria.

1) Agregar dependencias al pom.xml: incorporamos spring-boot-starter-data-jpa para 
trabajar con JPA y Hibernate, y el driver postgresql para que Java pudiera comunicarse
con la base de datos.

2) Configurar la conexión en application.properties
3) Crear las entidades JPA: cree PointEntity y BlueprintEntity en un 
paquete nuevo llamado entity, ya que las clases originales del modelo 
(Point como record inmutable) no eran compatibles con los requisitos 
de JPA, que necesita clases mutables con constructor vacío e identificador. 
Quedaron relacionadas mediante una asociación uno a muchos entre Blueprint y sus puntos.

4) Crear el repositorio JPA: Se definio la interfaz BlueprintJpaRepository, 
que extiende JpaRepository y declara métodos de búsqueda que Spring Data
implementa automáticamente sin necesidad de escribir SQL manualmente.

5) Crear PostgresBlueprintPersistence: se construyó  la clase dentro del paquete persistence,
implementando la misma interfaz BlueprintPersistence que ya usaba la versión en memoria. 
Utiliza el repositorio JPA para guardar y consultar datos, e incluye métodos de mapeo 
para traducir entre las clases del modelo de dominio y las entidades JPA.

6) Aplicar profile a la clase **InMemoryBlueprintPersistence** y **PostgresBlueprintPersistence**.

7) Corremos la aplicación:

> mvn spring-boot:run