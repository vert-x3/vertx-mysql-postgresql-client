#!/usr/bin/env bash
set -e

echo "SSL INIT SCRIPT RUNNING..."
sed -i 's/^host/hostssl/g' "$PGDATA"/pg_hba.conf

cp /docker-entrypoint-initdb.d/server.{crt,key} "$PGDATA"
chown postgres:postgres "$PGDATA"/server.{crt,key}
chmod 0600 "$PGDATA"/server.key

echo "pg_hba.conf now:"
cat "$PGDATA"/pg_hba.conf

sed -ri "s/^#?(ssl\s*=\s*)\S+/\1'on'/" "$PGDATA/postgresql.conf"

cat "$PGDATA"/postgresql.conf

echo "SSL INIT SCRIPT DONE."
