#!/bin/bash

function create_new_dev_database_container {
  docker run \
    --name debiki-dev-database \
    -p 5432:5432 \
    debiki-dev-database-data:v3 \
    /usr/lib/postgresql/9.3/bin/postgres -D /var/lib/postgresql/9.3/main -c config_file=/etc/postgresql/9.3/main/postgresql.conf
}

# Ensure image with imported dump exists.
any_row=`docker images | egrep 'debiki-dev-database-data\s+v3\s+'`
if [ -z "$any_row" ]; then
  echo 'Please first run ./docker-import-dev-database.sh /path/to/database/dumps/'
  exit 1
fi

# Attach to, or create, a database container:
any_row=`docker ps | grep debiki-dev-database-data:v3`
if [ -n "$any_row" ]; then
  echo 'Attaching to debiki-dev-database container...'
  docker attach debiki-dev-database
  exit
fi
any_row=`docker ps -a | grep debiki-dev-database-data:v3`
if [ -n "$any_row" ]; then
  echo 'Starting and attaching to debiki-dev-database container...'
  docker start debiki-dev-database
  docker attach debiki-dev-database
  exit
fi
echo 'Creating new debiki-dev-database container...'
create_new_dev_database_container


# vim: fdm=marker et ts=2 sw=2 tw=0 list
