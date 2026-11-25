# Kafka local development

Single-node KRaft combined broker/controller configuration for local development.

- Container listener: `kafka:9092`
- Host listener: `localhost:9094`
- Kafka UI: http://localhost:8090

This topology uses replication factor 1 and is not highly available. Production should use a multi-broker KRaft cluster, replication factor appropriate to cluster size, TLS/SASL, ACLs, monitoring, and tested retention/backup policies.
