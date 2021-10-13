# amigo-platform

This is the home of the *amigo-platform* which serves as the main service for the amigo multimedia platform.

* Authentication with JWT Token
* REST Api for Messages, Albums, Multimedia and NFC
* Connector to Jitsi for video conferences

## Setup in Docker context for PRODUCTION

### 1. Install docker and docker-compose

https://docs.docker.com/compose/install/

### 2. Set the proper environment for docker

Copy "system.env.default" and **change SECRETs and DB passwords** before the use in a production environment!

Set *at least* the following variables,
JITSI_JWT_APP_SECRET should have 32 chars!
```
AMIGO_ACCESS_TOKEN_SECRET=547fewtabd4w68b4w6
AMIGO_REFRESH_TOKEN_SECRET=547fewtabd4w68b4w7

JITSI_JWT_APP_ID=jitsi
JITSI_JWT_APP_SECRET=bbbbvvxxywqqqdddccsssxgasdfzaaaa
JITSI_ROOT_URL=https://amigo-dev.ossi-austria.org/
```

### 2. initialise docker container

This will start the amigo-platform and postgres services **with the applied system.env**

```
docker-compose -f docker-production.yml up -d

```

## Development Environment

1. Clone the project and checkout *develop* branch

```
git clone git@gitlab.com:ossi-austria/amigo-server.git
cd amigo-server
git checkout develop
```

2. Open project in favorite IDE (we use Intellij IDEA)

3. Start services and database needed for development (currently only postgres database)

```
docker-compose up
```

**Attention:**

As the development Environment will only rely on POSTGRES within docker,
you need to add the variables defined in system.env to the environment which runs spring boot server.

Do that a) as ENV VARs or better b) in IDE running config

4. Start the "RestApplication" via Intellij with "dev" spring env profile active

**Caution**: The database is recreated on startup when schema changes happen. As we do not offer a stable version yet,
we can live with this, but you should not.

Please be patient.

It may crash on first start for missing secrets, add those variables into the environment settings:
Defaults for postgres might work on your machine, but not when localhost is not accessible.
Map necessary env vars when defaults wont apply.

At least Jitsi info is needed in IDE run config:

````
JITSI_JWT_APP_SECRET=***;
JITSI_JWT_APP_ID=amigo-platform-dev;
JITSI_ROOT_URL=https://***/
````
4. Add firebase-config

Firebase is need for FCM to support push notifications.
Additional configs can be provided, in a $ROOT/configs directory 
which is ignored by git and NOT compiled into the app, but merely used as runtime config.

* Ask someone for more information
* Add **firebase-service-account.json** into that directory

# Create OpenApi client from openApi

Note: Works with openApi v2 currently.

````
docker run --rm -v "${PWD}:/local" openapitools/openapi-generator-cli generate -i http://192.168.0.20:8080/v2/api-docs -g dart -o /local/out/dart
````
https://springframework.guru/spring-boot-restful-api-documentation-with-swagger-2/

# Create Testdata via Swagger

* http://amigo-dev.ossi-austria.org:8080/swagger-ui.html
* loging with your Test user, via auth-api login (click on "Try out")
`  {
  "email": "string",
  "password": "string"
  }`
* remember your data: **personId**, **accessToken**, eventually groupId
* On every Request, provide **personId** as **Amigo-Person-Id** and **accessToken** as **Authorization**
