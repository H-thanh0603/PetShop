#!/usr/bin/env bash
set -euo pipefail

DOMAIN="${DOMAIN:-melipet.shop}"
APP_CONTEXT="${APP_CONTEXT:-PetShop}"
DB_NAME="${DB_NAME:-petvaccine}"
DB_USER="${DB_USER:-petshop}"
DB_PASSWORD="${DB_PASSWORD:?Set DB_PASSWORD before running this script}"
WEBHOOK_SECRET="${WEBHOOK_SECRET:?Set WEBHOOK_SECRET before running this script}"

apt update
apt upgrade -y
apt install -y openjdk-17-jdk tomcat10 nginx mysql-server certbot python3-certbot-nginx ufw

ufw allow OpenSSH
ufw allow 'Nginx Full'
ufw --force enable

mysql <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL

mkdir -p /root/petshop-prod-config

cat > /root/petshop-prod-config/app.properties <<EOF_APP
app.base-url=https://${DOMAIN}/${APP_CONTEXT}
app.context-path=/${APP_CONTEXT}
app.cookies.secure=true
app.static.cache.max-age-seconds=86400
api.provinces.base-url=https://provinces.open-api.vn/api/v1
payment.bank.pending-minutes=10
payment.bank.id=VPB
payment.bank.account-number=0368600557
payment.bank.account-name=NGUYEN HUU THANH
payment.bank.display-name=VP Bank
payment.bank.transfer-prefix=PETSHOP
payment.bank.currency=VND
payment.bank.verification-mode=webhook
payment.bank.webhook-secret=${WEBHOOK_SECRET}
payment.momo.mode=demo
ratelimit.login=10
ratelimit.register=6
ratelimit.forgot-password=6
ratelimit.checkout=8
ratelimit.bank-webhook=60
ratelimit.add-review=5
ratelimit.search-autocomplete=8
EOF_APP

cat > /root/petshop-prod-config/db.properties <<EOF_DB
db.host=localhost
db.port=3306
db.username=${DB_USER}
db.password=${DB_PASSWORD}
db.dbname=${DB_NAME}
db.option=useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Ho_Chi_Minh
EOF_DB

cat > "/etc/nginx/sites-available/${DOMAIN}" <<EOF_NGINX
server {
    listen 80;
    server_name ${DOMAIN} www.${DOMAIN};

    location / {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF_NGINX

ln -sf "/etc/nginx/sites-available/${DOMAIN}" "/etc/nginx/sites-enabled/${DOMAIN}"
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable mysql nginx tomcat10
systemctl restart mysql tomcat10 nginx

certbot --nginx -d "${DOMAIN}" -d "www.${DOMAIN}" --redirect --agree-tos --no-eff-email -m "admin@${DOMAIN}"
systemctl restart tomcat10 nginx

echo "Melipet server bootstrap complete."
echo "Deploy WAR to: /var/lib/tomcat10/webapps/${APP_CONTEXT}.war"
echo "Production config files are in: /root/petshop-prod-config"
echo "Webhook URL: https://${DOMAIN}/${APP_CONTEXT}/api/payment/bank-webhook"
