# wim100 - gestion de seleccion de candidatas

app movil para el programa wim100 (women in management). permite registrar candidatas, evaluarlas con una pauta ponderada y que direccion resuelva si quedan seleccionadas o no.

proyecto de la evaluacion parcial 4 de desarrollo de aplicaciones moviles (dsy1105), duoc uc.

## integrantes

- bastian jofre (Bast-JFR)
- renato colipe (RCL0666)

## funcionalidades

- login y registro de usuaria con formularios validados (correo, rut con digito verificador)
- roles evaluadora y direccion
- crud de candidatas conectado al microservicio: listar, crear, editar y eliminar desde la app
- evaluacion con puntaje ponderado (tecnico 50%, comunicacion 20%, experiencia 30%), se guarda en el servidor
- resolucion de direccion (seleccionada / no seleccionada), tambien se guarda en el servidor
- copia local en room: si el backend esta apagado la app sigue mostrando la ultima lista y avisa el error
- historial de acciones guardado en room
- api externa (randomuser.me) para sugerir un nombre y correo de ejemplo en el formulario
- notificaciones, llamar y compartir candidata (funciones nativas)

## arquitectura

app (kotlin + jetpack compose, mvvm):

```
pantalla (compose) -> SelectionViewModel -> CandidateRepository -> CandidataApi (retrofit) -> backend
                                                                -> CandidateDao (room, copia local)
                   -> RandomUserRepository -> RandomUserApi (retrofit) -> randomuser.me
```

paquetes de la app (`app/src/main/java/cl/duoc/wim100`):

- `model`: entidades de room (Candidate, HistoryEntry, UserSession)
- `data`: dao, base de datos room, repositorios y sesion
- `network`: retrofit, interfaces de las apis, dto, mapper y mensajes de error
- `domain`: validadores, notificaciones y acciones nativas
- `ui`: pantallas, componentes y viewmodel

backend (spring boot 3 + java 17 + mysql), carpeta `backend`:

- `model`: entidad Candidata (tabla candidatas)
- `repository`: CandidataRepository (jpa)
- `dto`: CandidataRequest con validaciones y ErrorResponse
- `service`: reglas de negocio (correo unico, estado inicial pendiente)
- `controller`: endpoints rest
- `exception`: excepciones propias y manejador global (400, 404, 409)
- `config`: carga de datos de prueba al iniciar

## endpoints

### microservicio propio

base: `http://localhost:8080/api`

| metodo | ruta | uso | respuestas |
|---|---|---|---|
| GET | /candidatas | listar | 200 |
| GET | /candidatas/{id} | obtener una | 200, 404 |
| POST | /candidatas | crear | 201, 400, 409 |
| PUT | /candidatas/{id} | editar, evaluar o resolver | 200, 400, 404, 409 |
| DELETE | /candidatas/{id} | eliminar | 204, 404 |

la coleccion de postman esta en `backend/postman/wim100.postman_collection.json`.

### api externa

`GET https://randomuser.me/api/?gender=female&nat=es&inc=name,email`

publica y sin key. se usa solo para sugerir datos en el formulario, no guarda nada en el microservicio.

diferencia: el microservicio es nuestro, lo controlamos y guarda los datos oficiales en mysql. la api externa es de terceros, solo se consulta y si se cae la app sigue funcionando.

## pasos para ejecutar

requisitos: android studio, intellij idea, jdk 17 o superior y mysql 8 (o docker).

1. levantar mysql
   - con docker: `docker run -d --name wim100-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0`
   - o con un mysql instalado, revisando usuario y clave en `backend/src/main/resources/application.properties`
2. levantar el backend: abrir la carpeta `backend` en intellij y ejecutar `BackendApplication`. la base `wim100` y la tabla se crean solas y se cargan 3 candidatas de prueba
3. ejecutar la app: abrir la raiz del proyecto en android studio, sincronizar gradle y ejecutar en el emulador
4. celular fisico: el celular y el pc tienen que estar en la misma red wifi. cambiar `BACKEND_URL` en `app/src/main/java/cl/duoc/wim100/network/RetrofitClient.kt` por la ip del pc (se ve con `ipconfig`), por ejemplo `http://192.168.1.50:8080/api/`, y permitir el puerto 8080 en el firewall de windows

## pruebas unitarias

estan en `app/src/test` y usan junit 4, mockk y kotlinx-coroutines-test. se mockea la api (retrofit), los dao (room), la sesion y las notificaciones, asi no se necesita emulador ni servidor.

- `CandidateRepositoryTest`: sincronizacion con el servidor, crear, editar y eliminar (incluye 404 y 500)
- `SelectionViewModelTest`: carga inicial, errores de red, crud, puntaje ponderado, resolucion, sesion y api externa
- `FormValidatorsTest`: rut, correo, nombre, telefono y cargo
- `CandidataMapperTest`, `ErroresTest`, `RandomUserRepositoryTest`

para correrlas: clic derecho en la carpeta `app/src/test` > run tests (o `run with coverage` para ver la cobertura).

## apk firmado

- llave: `app/wim100-release.jks` (alias `wim100`)
- la firma esta configurada en `app/build.gradle.kts` (signingConfigs release)
- para generar: build > generate signed app bundle or apk > apk, o en la terminal `gradlew assembleRelease`
- el apk queda en `app/build/outputs/apk/release/app-release.apk` y se copia a la carpeta `apk/` del repo

ojo: en un proyecto real la llave y sus claves no se suben a un repo publico, aca se deja porque la pauta lo pide.

capturas del apk y la llave: carpeta `docs/capturas`.

## planificacion

tablero de trello con las tareas repartidas entre los dos integrantes. el historial de commits del repo muestra el trabajo de cada uno.
