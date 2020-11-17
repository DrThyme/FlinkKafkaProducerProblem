package org.myorg.quickstart;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaJsonSerialize implements KafkaSerializationSchema<ObjectNode> {
    private ObjectMapper mapper;
    private String topic;

    public KafkaJsonSerialize(String topic) {
        super();
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(ObjectNode element, Long timestamp) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        try {
            return new ProducerRecord<>(topic, mapper.writeValueAsBytes(element));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
