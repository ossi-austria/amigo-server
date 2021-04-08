#!/bin/sh
set -e
docker exec -t postgres bash -c 'PGPASSWORD=password psql -d amigo_platform -Uamigo -c " \
DROP TABLE IF EXISTS public.flyway_schema_history CASCADE;
"'
