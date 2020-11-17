/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.myorg.quickstart;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setRestartStrategy(RestartStrategies.failureRateRestart(10, Time.of(5, TimeUnit.MINUTES), Time.of(10, TimeUnit.SECONDS)));
		env.enableCheckpointing(10000);
		env.setParallelism(1);
		env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5000);

		Properties consumerProperties = new Properties();
		consumerProperties.setProperty("bootstrap.servers", "IP_GOES_HERE");
		consumerProperties.setProperty("isolation.level", "read_committed");
		consumerProperties.setProperty("group.id", "flink-tester-tim");

		FlinkKafkaConsumer<ObjectNode> kafkaConsumer = new FlinkKafkaConsumer<>(
			"playerSessions", new KafkaJsonDeserializer(true), consumerProperties
		);
		kafkaConsumer.setStartFromEarliest();

		Properties producerProperties = new Properties();
		producerProperties.setProperty("bootstrap.servers", "IP_GOES_HERE");

		FlinkKafkaProducer<ObjectNode> kafkaProducer = new FlinkKafkaProducer<ObjectNode>(
				"enrichedPlayerSessionsTest",
				new KafkaJsonSerialize("enrichedPlayerSessionsTest"),
				producerProperties,
				FlinkKafkaProducer.Semantic.EXACTLY_ONCE
		);
		kafkaProducer.setWriteTimestampToKafka(true);

		env
				.addSource(kafkaConsumer)
				.name("GetFromKafka")
				.uid("GetFromKafka")
				.addSink(kafkaProducer)
				.name("WriteToKafka")
				.uid("WriteToKafka");

		// execute program
		env.execute("Flink Streaming Java API Skeleton");
	}
}
