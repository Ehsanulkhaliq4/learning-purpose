#!/bin/sh
set -u

DEBEZIUM_URL="http://debezium:8083"
CASSANDRA_CONNECT_URL="http://kafka-connect-cassandra:8083"

# ---------------------------------------------------------
# Helper: wait until an endpoint is reachable
# ---------------------------------------------------------
wait_for() {
    url="$1"
    label="$2"
    until curl -sf "$url" > /dev/null 2>&1; do
        echo "  ... $label is not ready yet"
        sleep 5
    done
}

# ---------------------------------------------------------
# Debezium connectors (unchanged behaviour)
# ---------------------------------------------------------
register_debezium_connector() {
    CONNECTOR_NAME="$1"
    CONNECTOR_FILE="$2"

    echo "=========================================="
    echo "Registering Debezium connector: $CONNECTOR_NAME"
    echo "=========================================="

    if curl -sf "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME" > /dev/null 2>&1; then
        echo "Connector already exists. Updating configuration..."
        curl -sf -X PUT \
            "$DEBEZIUM_URL/connectors/$CONNECTOR_NAME/config" \
            -H "Content-Type: application/json" \
            --data-binary @"$CONNECTOR_FILE" > /dev/null \
            || { echo "FAILED to update $CONNECTOR_NAME"; return 1; }
        echo "Updated: $CONNECTOR_NAME"
    else
        echo "Connector does not exist. Creating..."
        curl -sf -X POST \
            "$DEBEZIUM_URL/connectors" \
            -H "Content-Type: application/json" \
            --data-binary @"$CONNECTOR_FILE" > /dev/null \
            || { echo "FAILED to create $CONNECTOR_NAME"; return 1; }
        echo "Created: $CONNECTOR_NAME"
    fi

    echo
}

# ---------------------------------------------------------
# Cassandra sink connector (idempotent, uses PUT)
# ---------------------------------------------------------
register_cassandra_connector() {
    CONNECTOR_NAME="cassandra-log-sink"
    CONNECTOR_FILE="/kafka-connect/cassandra-sink-connector.json"

    echo "Waiting for Kafka Connect (Cassandra)..."
    wait_for "$CASSANDRA_CONNECT_URL/" "Kafka Connect (Cassandra)"

    echo "Waiting for Cassandra sink plugin to be loaded..."
    until curl -sf "$CASSANDRA_CONNECT_URL/connector-plugins" 2>/dev/null \
            | grep -q "CassandraSinkConnector"; do
        echo "  ... CassandraSinkConnector plugin not visible yet"
        sleep 5
    done

    echo "=========================================="
    echo "Registering Cassandra connector: $CONNECTOR_NAME"
    echo "=========================================="

    # PUT is idempotent: creates if missing, updates if present.
    if curl -sf -X PUT \
            "$CASSANDRA_CONNECT_URL/connectors/$CONNECTOR_NAME/config" \
            -H "Content-Type: application/json" \
            --data-binary @"$CONNECTOR_FILE" > /dev/null; then
        echo "Cassandra connector registered: $CONNECTOR_NAME"
    else
        echo "FAILED to register Cassandra connector"
        return 1
    fi

    echo
    echo "Status:"
    curl -s "$CASSANDRA_CONNECT_URL/connectors/$CONNECTOR_NAME/status"
    echo
}

# =========================================================
# Wait for Debezium, then register its connectors
# =========================================================
echo "Waiting for Debezium Kafka Connect..."
wait_for "$DEBEZIUM_URL/" "Debezium"
echo "Debezium is ready."
echo

# Blog Service
register_debezium_connector \
    "blog-postgres-connector" \
    "/connectors/blog-postgres-connector.json"

# Exam Service
register_debezium_connector \
    "exam-postgres-connector" \
    "/connectors/exam-postgres-connector.json"

# Book Service
register_debezium_connector \
    "book-postgres-connector" \
    "/connectors/book-postgres-connector.json"

# =========================================================
# Cassandra sink
# =========================================================
register_cassandra_connector

echo "=========================================="
echo "All connector registrations completed."
echo "=========================================="