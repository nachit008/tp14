# realicé una simple interfaz grafica con todo listo para utilizar el programa, faltaría hacer la validacion de IPs y los metodos para enviar pings y mostrar resultados en una tabla.

# ahora me centre en validar las IPs. Con operaciones en binario convierto las IPs en un numero decimal y asi las comparo facilmente. Ahora lo que me queda es el ping a los equipos, la barra de progreso real y la tabla para ver los resultados.

# Por ultimo terminé de perfeccionar todas las clases y añadí el controlador que se encarga de ejecutar todas las operaciones de red



## Requisitos
- Java 8 o superior
- Sistema operativo: Windows, Linux o macOS

## Instalación
1. Clonar el repositorio
2. Compilar: `javac -d bin src/**/*.java`
3. Ejecutar: `java -cp bin Main`

## Uso
1. Ingresar IP inicial y final del rango a escanear
2. Configurar tiempo de espera (ms)
3. Click en "Escanear"
4. Los resultados se mostrarán en la tabla
5. Opcional: Guardar resultados en CSV

