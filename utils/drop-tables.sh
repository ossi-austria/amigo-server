#!/bin/sh
set -e
docker exec -t postgres bash -c 'PGPASSWORD=password psql -d amigo_platform -Uamigo -c " \

"'
