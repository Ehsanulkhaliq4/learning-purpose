#!/bin/sh


DEBEZIUM_URL="http://debezium:8083"
CONNECTOR_FILE="/connectors/blog-postgres-connector.json"

echo "Waiting for Debezium Kafka Connect..."

until curl -sf "$DEBEZIUM_URL/" > /dev/null; do
    echo "Debezium is not ready yet..."
    sleep 5
done

echo "Debezium is ready."

CONNECTOR_NAME="blog-postgres-connector"

echo "Checking connector: $CONNECTOR_NAME"

if curl -sf "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME" > /dev/null; then
    echo "Connector already exists. Updating configuration..."

    curl -sf -X PUT \
        "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME/config" \
        -H "Content-Type: application/json" \
        --data-binary @"$CONNECTOR_FILE"

    echo
    echo "Connector configuration updated."
else
    echo "Connector does not exist. Creating..."

    curl -sf -X POST \
        "$DEBEZIUM_URL/connectors" \
        -H "Content-Type: application/json" \
        --data-binary @"$CONNECTOR_FILE"

    echo
    echo "Connector created."
fi

echo
echo "Connector registration completed."