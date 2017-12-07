#!/usr/bin/env bash
./start-mysql.sh
sleep 5
./start-postgres.sh
sleep 5
./start-postgres-ssl.sh
sleep 5
# Since the migration to docker-machine, the startup takes much more time.
