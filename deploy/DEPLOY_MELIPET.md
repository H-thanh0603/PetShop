# Melipet Production Deploy

Target:

- Domain: `melipet.shop`
- VPS IP: `160.250.246.238`
- App context: `/PetShop`
- Webhook URL: `https://melipet.shop/PetShop/api/payment/bank-webhook`

## 1. SSH into VPS

```bash
ssh root@160.250.246.238
```

## 2. Generate secrets on the VPS

```bash
openssl rand -base64 32
openssl rand -base64 32
```

Use one value as `DB_PASSWORD` and the other as `WEBHOOK_SECRET`.

## 3. Upload files from local Windows

Run these from PowerShell on your local machine:

```powershell
scp D:\Petshop2\PetShop\deploy\bootstrap-melipet-ubuntu.sh root@160.250.246.238:/root/bootstrap-melipet-ubuntu.sh
scp D:\Petshop2\PetShop\build\libs\PetShop.war root@160.250.246.238:/root/PetShop.war
```

## 4. Bootstrap server

Run this on the VPS:

```bash
chmod +x /root/bootstrap-melipet-ubuntu.sh
DB_PASSWORD='PASTE_DB_PASSWORD_HERE' WEBHOOK_SECRET='PASTE_WEBHOOK_SECRET_HERE' /root/bootstrap-melipet-ubuntu.sh
```

## 5. Deploy WAR

Run this on the VPS:

```bash
systemctl stop tomcat10
rm -rf /var/lib/tomcat10/webapps/PetShop /var/lib/tomcat10/webapps/PetShop.war
cp /root/PetShop.war /var/lib/tomcat10/webapps/PetShop.war
chown tomcat:tomcat /var/lib/tomcat10/webapps/PetShop.war
systemctl start tomcat10
sleep 30
cp /root/petshop-prod-config/app.properties /var/lib/tomcat10/webapps/PetShop/WEB-INF/classes/app.properties
cp /root/petshop-prod-config/db.properties /var/lib/tomcat10/webapps/PetShop/WEB-INF/classes/db.properties
chown tomcat:tomcat /var/lib/tomcat10/webapps/PetShop/WEB-INF/classes/app.properties
chown tomcat:tomcat /var/lib/tomcat10/webapps/PetShop/WEB-INF/classes/db.properties
systemctl restart tomcat10
```

Wait 20-40 seconds, then open:

```text
https://melipet.shop/PetShop
```

## 6. Configure SePay

Use this webhook URL:

```text
https://melipet.shop/PetShop/api/payment/bank-webhook
```

Set the SePay secret/auth value to the same `WEBHOOK_SECRET`.

The app accepts:

- `X-Bank-Webhook-Secret`
- `X-Secret-Key`
- `Authorization: Bearer <secret>`

## 7. Smoke test

Create one bank-transfer order, scan the QR, transfer a small real amount with the exact generated content, then check the order payment status.
