#!/usr/bin/env bash
set -u

APP_CONTEXT="${APP_CONTEXT:-PetShop}"
APP_DIR="/var/lib/tomcat10/webapps/${APP_CONTEXT}"
WAR_FILE="/var/lib/tomcat10/webapps/${APP_CONTEXT}.war"
CONFIG_DIR="/root/petshop-prod-config"

echo "== PetShop VPS Fix =="
date

echo
echo "== System =="
whoami
java -version 2>&1 | head -n 3
systemctl is-active tomcat10 || true
systemctl is-active mysql || true
systemctl is-active nginx || true

echo
echo "== Files =="
ls -lah /root/PetShop.war 2>/dev/null || true
ls -lah "${WAR_FILE}" 2>/dev/null || true
ls -lah "${APP_DIR}" 2>/dev/null || true
ls -lah "${APP_DIR}/WEB-INF/classes" 2>/dev/null || true
ls -lah "${CONFIG_DIR}" 2>/dev/null || true

echo
echo "== Ensure MySQL is running =="
systemctl enable mysql >/dev/null 2>&1 || true
systemctl restart mysql || true

echo
echo "== Ensure WAR is in Tomcat webapps =="
if [ -f /root/PetShop.war ]; then
  systemctl stop tomcat10 || true
  rm -rf "${APP_DIR}" "${WAR_FILE}"
  cp /root/PetShop.war "${WAR_FILE}"
  chown tomcat:tomcat "${WAR_FILE}"
  chmod 0644 "${WAR_FILE}"
  systemctl start tomcat10 || true
  echo "Waiting for Tomcat to expand WAR..."
  for i in $(seq 1 60); do
    if [ -d "${APP_DIR}/WEB-INF/classes" ]; then
      break
    fi
    sleep 1
  done
else
  echo "Missing /root/PetShop.war. Upload it before running this script."
fi

echo
echo "== Copy production config into expanded app =="
mkdir -p "${APP_DIR}/WEB-INF/classes"
if [ -f "${CONFIG_DIR}/app.properties" ]; then
  cp "${CONFIG_DIR}/app.properties" "${APP_DIR}/WEB-INF/classes/app.properties"
else
  echo "Missing ${CONFIG_DIR}/app.properties"
fi
if [ -f "${CONFIG_DIR}/db.properties" ]; then
  cp "${CONFIG_DIR}/db.properties" "${APP_DIR}/WEB-INF/classes/db.properties"
else
  echo "Missing ${CONFIG_DIR}/db.properties"
fi
chown -R tomcat:tomcat "${APP_DIR}"
find "${APP_DIR}" -type d -exec chmod 0755 {} \;
find "${APP_DIR}" -type f -exec chmod 0644 {} \;

echo
echo "== App config preview =="
grep -E "^(app.base-url|app.context-path|app.cookies.secure|payment.bank.webhook-secret|db.host|db.username|db.dbname)" \
  "${APP_DIR}/WEB-INF/classes/app.properties" "${APP_DIR}/WEB-INF/classes/db.properties" 2>/dev/null || true

echo
echo "== Database connectivity =="
DB_USER="$(grep -E '^db.username' "${APP_DIR}/WEB-INF/classes/db.properties" 2>/dev/null | cut -d= -f2- | xargs || true)"
DB_PASS="$(grep -E '^db.password' "${APP_DIR}/WEB-INF/classes/db.properties" 2>/dev/null | cut -d= -f2- | xargs || true)"
DB_NAME="$(grep -E '^db.dbname' "${APP_DIR}/WEB-INF/classes/db.properties" 2>/dev/null | cut -d= -f2- | xargs || true)"
if [ -n "${DB_USER}" ] && [ -n "${DB_PASS}" ] && [ -n "${DB_NAME}" ]; then
  mysql -u"${DB_USER}" -p"${DB_PASS}" -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SELECT DATABASE();" "${DB_NAME}" || true
else
  echo "Cannot read DB credentials from db.properties."
fi

echo
echo "== Restart Tomcat =="
systemctl restart tomcat10 || true
sleep 8

echo
echo "== HTTP checks =="
curl -k -I http://127.0.0.1:8080/${APP_CONTEXT} 2>/dev/null | head -n 10 || true
curl -k -I https://melipet.shop/${APP_CONTEXT} 2>/dev/null | head -n 10 || true

echo
echo "== Recent Tomcat journal =="
journalctl -u tomcat10 -n 120 --no-pager || true

echo
echo "== Tomcat log files =="
find /var/log/tomcat10 -maxdepth 1 -type f -printf "%p\n" 2>/dev/null || true

echo
echo "== Recent Tomcat logs with errors =="
for f in /var/log/tomcat10/*.log /var/log/tomcat10/catalina.out; do
  [ -f "$f" ] || continue
  echo "--- $f ---"
  grep -Ei "SEVERE|ERROR|Exception|Caused by|startup failed|listener" "$f" | tail -n 80 || true
done

echo
echo "== Done =="
