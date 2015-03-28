#!/bin/bash

echo -e "Setting up user 'tpcw_user1' for 'tpcw' database"
mysql -h localhost -u root -e "GRANT ALL PRIVILEGES on tpcw.* to 'tpcw_user1'@'localhost' WITH GRANT OPTION;"
mysql -h localhost -u root -e "SET PASSWORD FOR 'tpcw_user1'@'localhost' = PASSWORD('tpcw_user1');"


echo -e "Creating database 'tpcw'"
mysql -h localhost -u root -e "DROP DATABASE IF EXISTS tpcw;"
mysql -h localhost -u root -e "CREATE DATABASE tpcw;"

echo -e "Creating database.properties file"

echo "db_url jdbc:mysql://localhost/tpcw?relaxAutoCommit=true
db_driver com.mysql.jdbc.Driver
db_username tpcw_user1
db_password tpcw_user1" > ../scripts/database.properties