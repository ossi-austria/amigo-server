# amigo-platform

This is the home of the *amigo-platform* which serves as the main service for the amigo multimedia platform.

* Authentication with JWT Token
* REST Api for Messages, Albums, Multimedia and NFC
* Connector to MinIO for file handling
* Connector to Jitsi for video conferences

## Setup in Docker context

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

3. Start the "RestApplication" via Intellij with "dev" spring env profile active

**Caution**: The database is recreated on startup when schema changes happen. As we do not offer a stable version yet,
we can live with this, but you should not.

Please be patient.

It may crash on first start for missing secrets, add those variables into the environment settings:

Ask someone for that information.
````
JITSI_JWT_APP_SECRET=***;JITSI_JWT_APP_ID=amigo-platform-dev;JITSI_ROOT_URL=https://***/
````
4. Add firebase-config

Additional configs can be provided, in a $ROOT/configs directory which is ignored by git.

* Ask someone for more information
* Add **firebase-service-account.json** into that directory
