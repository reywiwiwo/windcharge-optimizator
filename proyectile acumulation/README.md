# WindChargeOptimizer Plugin para Minecraft 1.21.1

Un plugin de optimización para Minecraft 1.21.1 que combina múltiples cargas de viento en una sola entidad para reducir el lag y mejorar el TPS, manteniendo la potencia de explosión equivalente al número de cargas originales.

## Características

- **Optimización de rendimiento**: Combina cargas de viento cercanas en una sola entidad
- **Acumulación automática**: Las cargas se acumulan continuamente en entidades existentes
- **Herencia de propiedades**: Las entidades combinadas heredan características de la primera carga (velocidad, posición estática, etc.)
- **Tags visuales**: Muestra el número de cargas acumuladas encima de cada entidad combinada
- **Potencia conservada**: La explosión resultante tiene la potencia equivalente a todas las cargas combinadas
- **Configurable**: Radio de búsqueda, intervalo de verificación y más ajustes personalizables
- **Efectos visuales**: Partículas y sonidos cuando se combinan las cargas
- **Múltiples mundos**: Soporte para habilitar/deshabilitar en mundos específicos
- **Debug mode**: Opción para ver información detallada en consola

## Instalación

1. Compila el plugin con Maven:
   ```bash
   mvn clean package
   ```

2. Copia el archivo JAR generado en `target/WindChargeOptimizer-1.0.0.jar` a la carpeta `plugins` de tu servidor Minecraft.

3. Reinicia el servidor o carga el plugin con `/reload`.

## Configuración

El archivo `config.yml` se genera automáticamente en `plugins/WindChargeOptimizer/`:

```yaml
# Radio de búsqueda para combinar cargas de viento (en bloques)
search-radius: 10.0

# Intervalo de verificación en ticks (20 ticks = 1 segundo)
check-interval: 5

# Número mínimo de cargas para combinar
minimum-charges: 2

# Máximo número de cargas por entidad combinada
max-charges-per-entity: 50

# Modo de acumulación automática (siempre activo)
auto-accumulate: true

# Mostrar mensajes de debug en consola
debug-mode: false

# Mundos donde el plugin está activo (vacío = todos los mundos)
enabled-worlds: []

# Partículas visuales cuando se combinan cargas
show-particles: true

# Sonido cuando se combinan cargas
play-sound: true

# Mostrar tag con número de cargas encima de la entidad
show-charge-tag: true

# Color del tag (formato hexadecimal)
tag-color: "#FFAA00"

# Heredar características de la primera carga (velocidad, dirección, etc.)
inherit-first-charge-properties: true

# Actualizar tag continuamente cuando se añaden más cargas
update-tag-continuously: true
```

## Comandos

- `/windcharge reload` - Recargar la configuración
- `/windcharge info` - Mostrar información del plugin y configuración actual
- `/windcharge status` - Mostrar estado del optimizador
- `/windcharge toggle` - Activar/desactivar optimización (en desarrollo)

## Permisos

- `windcharge.admin` - Acceso a comandos administrativos (defecto: OP)
- `windcharge.use` - Uso de la optimización (defecto: true)

## Cómo funciona

### Modo de Acumulación Automática (Predeterminado)

1. **Detección continua**: El plugin escanea periódicamente todas las cargas de viento en los mundos habilitados.
2. **Búsqueda de entidades existentes**: Primero busca entidades combinadas existentes.
3. **Acumulación**: Las cargas normales cercanas se añaden automáticamente a las entidades combinadas existentes.
4. **Herencia de propiedades**: La entidad combinada hereda las características de la primera carga (velocidad, posición estática, etc.).
5. **Tags visuales**: Muestra el número total de cargas acumuladas encima de cada entidad.
6. **Actualización dinámica**: Los tags se actualizan continuamente cuando se añaden más cargas.

### Herencia de Propiedades

- **Velocidad**: La entidad combinada mantiene la velocidad de la primera carga
- **Posición estática**: Si la primera carga es estática (velocidad ~0), todas las añadidas también serán estáticas
- **Ubicación**: Se mantiene la posición exacta de la primera carga
- **Propiedades adicionales**: Se heredan otras características como el estado de fuego

### Sistema de Tags

- Cada entidad combinada muestra un tag con formato: `⚡ Nx` (donde N es el número de cargas)
- El color del tag es configurable mediante código hexadecimal
- Los tags se actualizan en tiempo real cuando se añaden más cargas

### Sistema de Explosión

- La potencia de explosión se calcula como: `número_de_cargas × 3.0`
- Se mantiene el mismo comportamiento explosivo que las cargas originales
- La explosión ocurre en la posición de la entidad combinada

## Requisitos

- Minecraft 1.21.1 con PaperMC
- Java 21
- Maven para compilar

## Compilación

```bash
# Clonar o descargar el proyecto
cd WindChargeOptimizer

# Compilar con Maven
mvn clean package

# El JAR compilado estará en target/
```

## Notas de rendimiento

- El plugin reduce significativamente el número de entidades procesadas por el servidor
- Las explosiones combinadas pueden ser muy potentes, ajusta `max-charges-per-entity` según tus necesidades
- El intervalo de verificación predeterminado (5 ticks) balancea rendimiento y respuesta

## Soporte

Para reportar bugs o solicitar características, por favor abre un issue en el repositorio del proyecto.

## Licencia

Este plugin está distribuido bajo la licencia MIT.
