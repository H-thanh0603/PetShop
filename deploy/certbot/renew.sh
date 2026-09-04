#!/bin/sh
# Renewal loop: every 12h certbot renew checks certificate expiry and only
# renews what is due. After a successful renewal the --deploy-hook reloads
# nginx in the petshop-nginx container so the new certificate is served
# immediately (requires /var/run/docker.sock mounted into this container).
trap exit TERM
while :; do
  sleep 12h & wait $!
  certbot renew --webroot -w /var/www/certbot \
    --deploy-hook "docker exec petshop-nginx nginx -s reload"
done
