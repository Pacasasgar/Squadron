# Squadron TD Clone (Backend)

Un juego estilo "Rogue-like / Tower Defense" por oleadas, desarrollado en Java 21 y Spring Boot.

## Tecnologías Principales
- **Java 21**: Lenguaje principal.
- **Spring Boot 3**: Framework para crear la aplicación web y la API REST.
- **Spring Data JPA**: Facilita la interacción con la base de datos (H2).
- **H2 Database**: Base de datos en memoria, ideal para desarrollo y prototipos.
- **Lombok**: Para reducir el código repetitivo (getters, setters, constructores).
- **OpenAPI / Swagger**: Para documentar y probar los endpoints visualmente.

## Mecánicas Básicas
1. Tienes un **oro** inicial y unos ingresos (**income**) asegurados tras cada oleada.
2. Usas oro para colocar defensas (unidades).
3. Puedes usar oro para mejorar tus ingresos, pensando en rondas futuras.
4. Cuando estás listo, empieza la oleada y el servidor simula el combate entre tus defensas y los enemigos.
5. Si tus defensas no pueden frenarlos, pierdes vida en la base. Si llega a 0, pierdes la partida.

## Estructura del Proyecto
- `models`: Entidades de la base de datos (Game, DefensePlacement) y clases de configuración (tipos de unidades).
- `repositories`: Interfaces mágicas de Spring Data para guardar/leer datos de H2 sin escribir SQL.
- `services`: La lógica de negocio. Reglas como "restar dinero al comprar" o el "algoritmo de batalla".
- `controllers`: Los puntos de entrada a la API REST (endpoints) que consumirá el Frontend.
