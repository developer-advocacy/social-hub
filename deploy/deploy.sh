#!/usr/bin/env bash
set -e
set -o pipefail
export APP_NAME=social-hub
export SECRETS=${APP_NAME}-secrets
export SECRETS_FN=$HOME/${SECRETS}
export IMAGE_NAME=gcr.io/${GCLOUD_PROJECT}/${APP_NAME}
export RESERVED_IP_NAME=${NS_NAME}-${APP_NAME}-ip

echo "-----"
echo $RESERVED_IP_NAME
echo $IMAGE_NAME
echo $SECRETS_FN
echo $APP_NAME
echo "-----"


docker rmi -f $IMAGE_NAME || echo "couldn't delete the old image, $IMAGE_NAME. It doesn't exist."

cd $ROOT_DIR

gcloud compute addresses list --format json | jq '.[].name' -r | grep $RESERVED_IP_NAME || gcloud compute addresses create $RESERVED_IP_NAME --global

./gradlew bootBuildImage --imageName=$IMAGE_NAME
docker push $IMAGE_NAME

touch "$SECRETS_FN"
echo writing to "$SECRETS_FN "
cat <<EOF >${SECRETS_FN}
SOCIALHUB_DB_HOST=${SOCIALHUB_DB_HOST}
SOCIALHUB_DB_PASSWORD=${SOCIALHUB_DB_PASSWORD}
SOCIALHUB_DB_SCHEMA=${SOCIALHUB_DB_SCHEMA}
SOCIALHUB_DB_USERNAME=${SOCIALHUB_DB_USERNAME}
SOCIALHUB_ENCRYPTION_PASSWORD=${SOCIALHUB_ENCRYPTION_PASSWORD}
SOCIALHUB_ENCRYPTION_SALT=${SOCIALHUB_ENCRYPTION_SALT}
SOCIALHUB_MQ_HOST=${SOCIALHUB_MQ_HOST}
SOCIALHUB_MQ_PASSWORD=${SOCIALHUB_MQ_PASSWORD}
SOCIALHUB_MQ_PORT=${SOCIALHUB_MQ_PORT}
SOCIALHUB_MQ_USERNAME=${SOCIALHUB_MQ_USERNAME}
SOCIALHUB_MQ_VIRTUAL_HOST=${SOCIALHUB_MQ_VIRTUAL_HOST}
SOCIALHUB_URI=${SOCIALHUB_URI}
AYRSHARE_JOSHLONG_TOKEN=${AYRSHARE_JOSHLONG_TOKEN}
EOF
kubectl delete secrets $SECRETS || echo "no secrets to delete."
kubectl create secret generic $SECRETS --from-env-file "$SECRETS_FN"
kubectl delete -f "$ROOT_DIR"/deploy/k8s/deployment.yaml || echo "couldn't delete the deployment as there was nothing deployed."
kubectl apply -f "$ROOT_DIR"/deploy/k8s
