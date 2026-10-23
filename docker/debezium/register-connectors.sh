#!/bin/sh

DEBEZIUM_URL="http://debezium:8083"
CASSANDRA_CONNECT_URL="http://kafka-connect-cassandra:8083"

echo "Waiting for Debezium Kafka Connect..."

until curl -sf "$DEBEZIUM_URL/" > /dev/null; do
    echo "Debezium is not ready yet..."
    sleep 5
done

echo "Debezium is ready."
echo


register_debezium_connector() {
    CONNECTOR_NAME="$1"
    CONNECTOR_FILE="$2"

    echo "=========================================="
    echo "Checking Debezium connector: $CONNECTOR_NAME"
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

register_cassandra_connector() {
    echo "Waiting for Kafka Connect (Cassandra)..."

    until curl -sf "$CASSANDRA_CONNECT_URL/" > /dev/null; do
        sleep 5
    done

    CONNECTOR_NAME="cassandra-log-sink"
    CONNECTOR_FILE="/kafka-connect/cassandra-sink-connector.json"

    echo "Registering connector: $CONNECTOR_NAME"

    if curl -sf "$CASSANDRA_CONNECT_URL/connectors/$CONNECTOR_NAME" \
        > /dev/null; then

        echo "Deleting existing connector..."
        curl -fsS -X DELETE \
            "$CASSANDRA_CONNECT_URL/connectors/$CONNECTOR_NAME" \
            || return 1
    fi

    echo "Creating connector..."

    curl -fsS -X POST \
        "$CASSANDRA_CONNECT_URL/connectors" \
        -H "Content-Type: application/json" \
        --data-binary @"$CONNECTOR_FILE" \
        || return 1

    echo
    echo "Connector registration succeeded."
}



# =========================================================
# Blog Service
# =========================================================

register_debezium_connector \
    "blog-postgres-connector" \
    "/connectors/blog-postgres-connector.json"


# =========================================================
# Exam Service
# =========================================================

register_debezium_connector \
    "exam-postgres-connector" \
    "/connectors/exam-postgres-connector.json"

# =========================================================
# Book Service
# =========================================================

register_debezium_connector \
    "book-postgres-connector" \
    "/connectors/book-postgres-connector.json"

# =========================================================
# Kafka Connector For Cassandra
# =========================================================

register_cassandra_connector

echo "=========================================="
echo "All connector registrations completed."
echo "=========================================="