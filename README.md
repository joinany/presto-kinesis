# Kinesis Connector

> This project is derived from the project [qubole/presto-kinesis] (https://github.com/qubole/presto-kinesis), which is not compatible with the latest Presto version.

Kinesis is Amazon’s fully managed cloud-based service for real-time processing of large, distributed data streams.

Analogous to Kafka connector, this connector allows the use of Kinesis streams as tables in Presto, such that each data-blob in kinesis stream is presented as a row in Presto.
Streams can be live: rows will appear as data is pushed into the stream, and disappear as they are dropped once their time expires. (A message is held up for 24 hours by kinesis streams).

> This connector is Read-Only connector. It can only fetch data from kinesis streams, but can not create streams or push data into the already existing streams.

# Building

    mvn clean package

This will create ``target/presto-kinesis-<version>.zip`` file which contains the connector code and its dependency jars.

# Installation

You will need to augment your presto installation on coordinator and worker nodes to make sure the connector is loaded and configured properly.
We will use $PRESTO_HOME to refer to the presto installation directory (e.g. /usr/lib/presto) and $PRESTO_CONF_HOME to refer to the presto configuration directory (e.g. /etc/presto/conf).

* Copy contents of the zipped file to ``$PRESTO_HOME/plugin/presto-kinesis`` directory (create it if necessary)
* Create a ``kinesis.properties`` file in ``$PRESTO_CONF_HOME/catalog`` directory. See [Connector Configuration] (https://github.com/qubole/presto-kinesis/wiki/Connector-Configuration)
* Create directory ``$PRESTO_CONF_HOME/kinesis`` and create a json table definition file for every presto-kinesis table. See [Table Definition] (https://github.com/qubole/presto-kinesis/wiki/Table-Definitions)

# Configuration Example
catalog/kinesis.properties:

    connector.name=kinesis
    kinesis.table-names=raw_events
    kinesis.access-key=********************
    kinesis.secret-key=****************************************
    kinesis.aws-region=us-west-2
    kinesis.table-description-dir=/etc/presto/conf/kinesis

kinesis/raw_events.json:

    {
        "tableName": "raw_events",
        "schemaName": "default",
        "streamName": "raw_events",
        "message": {
            "dataFormat": "json",
            "fields": [
                {"name": "data_version", "mapping": "data_version", "type": "VARCHAR"},
                {"name": "app_id", "mapping": "app_id", "type": "VARCHAR"},
                {"name": "ts", "mapping": "ts", "type": "VARCHAR"},
                {"name": "event", "mapping": "event", "type": "VARCHAR"},
                {"name": "user_id", "mapping": "user_id", "type": "VARCHAR"},
                {"name": "session_id", "mapping": "session_id", "type": "VARCHAR"},
                {"name": "properties", "mapping": "properties", "type": "JSON"}
            ]}
    }

# Presto Query Example
Make a query in the Presto and verify the connector:

    select * from kinesis.default.raw_events order by ts desc limit 10
