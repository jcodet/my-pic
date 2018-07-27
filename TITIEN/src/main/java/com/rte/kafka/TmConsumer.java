package com.rte.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class TmConsumer {
	private final static String TOPIC = "tm-topic";
    private final static String BOOTSTRAP_SERVERS =
            "192.168.56.101:9092,192.168.56.101:9093,192.168.56.101:9094";
    
    
    private static Consumer<Long, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                    BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                                    "TmConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<Long, String> consumer =
                                    new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }
    
    static void runConsumer() throws InterruptedException {
        final Consumer<Long, String> consumer = createConsumer();

        final int giveUp = 1000000000;
        int noRecordsCount = 0;
        Long debut = 0L;
        Long fin = 0L;
        ConsumerRecord<Long, String> record;
        List<Double> listOfTm = new ArrayList<Double>();
        int i = 0;
        
        Iterator<ConsumerRecord<Long, String>> it = null;
        
        while (true) {
            final ConsumerRecords<Long, String> consumerRecords = consumer.poll(10);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else {
                	continue;
                }
            }
            
            for(it = consumerRecords.iterator(); it.hasNext(); ){
            	record = it.next();
            	if("START".equals(record.value())) {
            		debut = System.currentTimeMillis();
            		System.out.println("--- \nDébut transfert 70K TM " + new Date(debut));
            	}
            	else if("END".equals(record.value())) {
	            	fin = System.currentTimeMillis();
	            	System.out.println("Fin transfert " + listOfTm.size() + " TM " + new Date(fin) + "\n---");
	            	System.out.println("Temps passé:"+(fin-debut)+ " ms");
	            	System.out.println("DONE");
	            	System.out.println("Moyenne des " + listOfTm.size() + " TM reçues:"+TmConsumer.moyenne(listOfTm));       	
            	}
            	else
            		listOfTm.add(Double.valueOf(record.value()).doubleValue());
            	
            }
            consumer.commitAsync();

            listOfTm.clear();
        }
        consumer.close();

    }
    
    /**
     * @param args
     * @return moyenne arithmétique
     */
    public static double moyenne(List<Double> args ){
    	if(args.size() == 0 ) {
    		return 0;
    	}
    	double total = 0;
    	
    	for(double d : args) {
    		total += d;
    	}
    	return total/args.size();
    }
    
  public static void main(String... args) throws Exception {
      runConsumer();
  }
}
