# Proyecto final ICC352 - 1910

## Esmery Vásquez - 10153425
## Jeverlin Ramos - 10154300

Este proyecto corresponde al proyecto final de la asignatura Programación Web, el cual es una aplicación web para la gestión de encuestas, desarrollada con Java, el microframework Javalin, MongoDB y gRPC. Permite crear, visualizar y sincronizar formularios, en modo online o offline.

## Aplicación en producción
https://jeverlinramos.me/

## Tecnologías del proyecto
- Java
- Javalin
- HTML, CSS y JavaScript
- gRPC
- API REST
- Docker y Docker Compose

## Funcionalidades
- Login
- Registro de formularios
- Visualización geográfica de los formularios en un mapa
- Soportada offline gracias a los services workers y web workers
- Uso de API REST y gRPC

## Nota
Es necesario crear un archivo *.env* con las siguientes variables de entorno:
- MONGO_URI = uriMongoDB
- JWT_SECRET = claveSecretaJWT
- PORT=7000
- GRPC_PORT=50051

Los valores de estas estarán en el reporte entregado en la PVA

### Para ejecutar con Docker
docker compose up -d --build
#### La aplicación estará disponible en el puerto 7000
