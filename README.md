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

```
SPRING_PROFILES_ACTIVE=dev
DB_HOST=amigodb
DB_PORT=5432
DB_USER=amigo
DB_PASS=password                                             
DB_NAME=amigo_platform
POSTGRES_USER=amigo
POSTGRES_PASSWORD=password                                           
POSTGRES_DB=amigo_platform
AMIGO_ACCESS_TOKEN_SECRET= define this
AMIGO_REFRESH_TOKEN_SECRET= define this
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


