#!/bin/sh
# Restore the nightly backups produced by the `backup` service in
# docker-compose.prod.yml (DB dump + uploads tarball).
#
# Usage (on the prod host, from the repo root):
#   export MYSQL_ROOT_PASSWORD=...
#   scripts/restore_db.sh backups/petvaccine_20260904_020000.sql.gz \
#                         backups/uploads_20260904_020000.tar.gz
#
# The uploads argument is optional. WARNING: the DB restore replaces the
# petvaccine schema contents — run it only on a stopped/quiesced app:
#   docker compose -f docker-compose.prod.yml stop app
set -eu

dump=${1:?usage: restore_db.sh <backups/petvaccine_*.sql.gz> [backups/uploads_*.tar.gz]}
: "${MYSQL_ROOT_PASSWORD:?export MYSQL_ROOT_PASSWORD first}"

[ -f "$dump" ] || { echo "no such file: $dump"; exit 1; }

echo ">> Restoring database from $dump"
gunzip -c "$dump" | docker exec -i petshop-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" petvaccine
echo ">> Database restored"

if [ "${2:-}" ]; then
  [ -f "$2" ] || { echo "no such file: $2"; exit 1; }
  echo ">> Restoring uploads from $2"
  cat "$2" | docker exec -i petshop-app sh -c 'tar -xzf - -C /app/uploads'
  echo ">> Uploads restored"
fi

echo ">> Start the app again: docker compose -f docker-compose.prod.yml start app"
