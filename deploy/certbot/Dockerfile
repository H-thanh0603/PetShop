# certbot + docker CLI so the renewal deploy-hook can reload nginx in the
# sibling container. Pinned base image (official certbot/certbot is floating).
FROM certbot/certbot:v5.8.0
RUN apk add --no-cache docker-cli
COPY renew.sh /usr/local/bin/renew.sh
ENTRYPOINT ["/bin/sh", "/usr/local/bin/renew.sh"]
