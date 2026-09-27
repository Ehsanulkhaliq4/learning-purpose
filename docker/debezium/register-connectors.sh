#!/bin/sh

DEBEZIUM_URL="http://debezium:8083"

echo "Waiting for Debezium Kafka Connect..."

until curl -sf "$DEBEZIUM_URL/" > /dev/null; do
    echo "Debezium is not ready yet..."
    sleep 5
done

echo "Debezium is ready."
echo


register_connector() {
    CONNECTOR_NAME="$1"
    CONNECTOR_FILE="$2"

    echo "=========================================="
    echo "Checking connector: $CONNECTOR_NAME"
    echo "=========================================="

    if curl -sf "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME" > /dev/null; then

        echo "Connector already exists. Updating configuration..."

        curl -sf -X PUT \
            "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME/config" \
            -H "Content-Type: application/json" \
            --data-binary @"$CONNECTOR_FILE"

        echo
        echo "Connector configuration updated: $CONNECTOR_NAME"

    else

        echo "Connector does not exist. Creating..."

        curl -sf -X POST \
            "$DEBEZIUM_URL/connectors" \
            -H "Content-Type: application/json" \
            --data-binary @"$CONNECTOR_FILE"

        echo
        echo "Connector created: $CONNECTOR_NAME"

    fi

    echo
}


# =========================================================
# Blog Service
# =========================================================

register_connector \
    "blog-postgres-connector" \
    "/connectors/blog-postgres-connector.json"


# =========================================================
# Exam Service
# =========================================================

register_connector \
    "exam-postgres-connector" \
    "/connectors/exam-postgres-connector.json"


echo "=========================================="
echo "All connector registrations completed."
echo "=========================================="