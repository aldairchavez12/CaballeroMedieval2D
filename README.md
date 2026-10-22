
# Caballero Medieval 2D

### Videojuego de plataformas desarrollado en Java

**Universidad Peruana Cayetano Heredia (UPCH)**  
**Carrera:** Ingeniería Informática  
**Curso:** Programación Orientada a Objetos  
**Equipo:** 2

---

## Descripción del proyecto

**Caballero Medieval 2D** es un videojuego de plataformas y combate desarrollado en Java, utilizando Swing y Java2D para su interfaz gráfica.

El proyecto aplica los principios de programación orientada a objetos mediante una arquitectura modular que permite administrar personajes, enemigos, herramientas, consumibles y escenarios.

El jugador controla a un caballero medieval que debe superar enfrentamientos para liberar su reino.

## Historia: La Fortaleza Oscura

El reino de Valdoria ha sido invadido por las fuerzas del Señor de las Sombras.

Un caballero emprende una misión para recuperar la fortaleza y restaurar la paz.

La aventura se divide en tres capítulos secuenciales:

| Capítulo | Escenario | Enemigo |
|---|---|---|
| I | Bosque de las Sombras | Goblin |
| II | Fortaleza Abandonada | Guardián Oscuro |
| III | Trono de la Oscuridad | Señor de las Sombras (Boss) |

Para avanzar al siguiente capítulo, el jugador debe derrotar al enemigo correspondiente.

El juego contempla dos resultados: victoria al derrotar al jefe final y derrota cuando la vida del caballero llega a cero.

## Tecnologías utilizadas

- **Java:** lenguaje principal.
- **Java Swing:** creación de la ventana e interfaz gráfica.
- **Java2D:** renderizado del escenario, personajes y elementos visuales.
- **Git:** control de versiones.
- **GitHub:** colaboración e integración del equipo.
- **Visual Studio Code:** entorno de desarrollo.

## Arquitectura del proyecto

El videojuego está organizado en paquetes que separan las responsabilidades de cada módulo.

```text
src/
├── entidades/
│   ├── Entidad.java
│   ├── Caballero.java
│   ├── Aliado.java
│   ├── Enemigo.java
│   └── Boss.java
│
├── interfaces/
│   └── Atacable.java
│
├── items/
│   ├── Item.java
│   ├── Inventario.java
│   ├── herramientas/
│   │   ├── Herramienta.java
│   │   ├── Espada.java
│   │   ├── Arco.java
│   │   ├── Flecha.java
│   │   ├── Armadura.java
│   │   └── Escudo.java
│   └── consumibles/
│       ├── Consumible.java
│       ├── Comida.java
│       ├── Pocima.java
│       └── Habilidad.java
│
├── escenario/
│   └── Estructura.java
│
└── juego/
    ├── Main.java
    ├── Juego.java
    ├── VentanaJuego.java
    └── PanelJuego.java
```

### Responsabilidades principales

**Entidad:** clase abstracta que representa los comportamientos comunes de los personajes.

**Caballero:** protagonista del videojuego. Implementa movimiento, salto, ataque y administración de vida.

**Enemigo:** representa a los adversarios del jugador.

**Boss:** especialización de Enemigo que incorpora ataques especiales.

**Atacable:** interfaz que permite que diferentes objetos reciban daño.

**Item:** clase abstracta de los objetos utilizables.

**Inventario:** administra los objetos del personaje.

**Juego:** coordina las entidades, los capítulos, el combate y los estados de la partida.

**PanelJuego:** gestiona la entrada del teclado y el renderizado gráfico.

**VentanaJuego:** administra la ventana principal.

## Principios de programación orientada a objetos

### 1. Encapsulamiento

Los atributos privados se consultan y modifican mediante métodos definidos por las clases.

### 2. Herencia

`Caballero` y `Enemigo` heredan de `Entidad`, mientras que `Boss` hereda de `Enemigo`.

### 3. Polimorfismo

Los personajes pueden actualizarse mediante referencias de tipo `Entidad`.

Los ataques utilizan el contrato de la interfaz `Atacable`.

### 4. Abstracción

Las clases abstractas `Entidad`, `Item`, `Herramienta` y `Consumible` definen características comunes para sus especializaciones.

### 5. Relaciones entre objetos

El diseño UML contempla asociaciones, agregaciones y composiciones para representar la interacción y pertenencia entre los diferentes elementos del videojuego.

## Características del prototipo

- Ventana gráfica desarrollada con Swing.
- Escenario medieval renderizado mediante Java2D.
- Movimiento horizontal del caballero.
- Sistema de salto y gravedad.
- Combate mediante ataques cuerpo a cuerpo.
- Enemigos con detección y persecución por proximidad.
- Ataques enemigos con intervalo de espera.
- Jefe final con ataque especial.
- Sistema de vida y recepción de daño.
- Tres capítulos con progresión secuencial.
- Condiciones de victoria y derrota.
- Monedas coleccionables en el escenario.
- Elementos visuales como plataformas, escaleras y cofres.

**Nota:** el proyecto se encuentra en desarrollo. Algunos elementos visuales todavía no cuentan con colisiones o interacciones completas.

## Controles

| Tecla | Acción |
|---|---|
| A | Mover a la izquierda |
| D | Mover a la derecha |
| Espacio | Saltar |
| J | Atacar |
| Enter | Iniciar o continuar la historia |

## Requisitos

- JDK 17 o superior.
- Git (para clonar el repositorio).
- Un sistema operativo con entorno gráfico compatible con Java Swing.

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/aldairchavez12/CaballeroMedieval2D.git
```

### 2. Entrar al proyecto

```bash
cd CaballeroMedieval2D
```

### 3. Compilar

Desde PowerShell:

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null

javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

### 4. Ejecutar

```powershell
java -cp out juego.Main
```

## Equipo de desarrollo

El proyecto se desarrolla colaborativamente mediante ramas de Git.

| Integrante | Responsabilidad |
|---|---|
| Aldair Chávez | Integración, arquitectura y lógica del videojuego |
| Jherson | Personajes: Caballero y Aliado |
| Pablo | Enemigos, Boss y estructuras |
| Harold | Equipamiento, herramientas y consumibles |

### Estrategia de ramas

- `main`: versión integrada del proyecto.
- `feature/personajes`: módulo de personajes.
- `feature/enemigos`: módulo de enemigos.
- `feature/equipamiento`: sistema de herramientas y consumibles.
- `feature/integracion`: lógica, interfaz y funcionamiento general del videojuego.

Los módulos se integran mediante Pull Requests, conservando el historial de contribuciones.

## Estado del proyecto

**Estado:** prototipo jugable en desarrollo.

### Próximas mejoras

- Animaciones y sprites de personajes.
- Escenarios diferenciados por capítulo.
- Colisiones con plataformas y obstáculos.
- Implementación completa del sistema de inventario.
- Integración de armas y consumibles al combate.
- Efectos de sonido y música.
- Pantalla de inicio, pausa y reinicio.
- Pruebas de jugabilidad y optimización.

---

**Caballero Medieval 2D — Proyecto académico de Ingeniería Informática, UPCH.**
