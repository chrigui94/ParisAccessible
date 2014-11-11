package com.bzanni.parisaccessible.indexer.controller;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.bzanni.parisaccessible.indexer.service.TrottoirIndexerService;

@Configuration
@Configurable
@ComponentScan
@EnableAutoConfiguration
@ImportResource({ "classpath:/META-INF/spring/servlet-context.xml" })
public class Application {

	

	@Value("${rabbitmq_host}")
	private String rabbitmqHost;

	@Value("${rabbitmq_port}")
	private Integer rabbitmqPort;

	@Value("${rabbitmq_username}")
	private String rabbitmqUsername;

	@Value("${rabbitmq_password}")
	private String rabbitmqPassword;

	final static String workflowQueueName = "workflow";
	final static String locationQueueName = "location";
	final static String pathQueueName = "path";

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(
				rabbitmqHost, rabbitmqPort);
		connectionFactory.setUsername(rabbitmqUsername);
		connectionFactory.setPassword(rabbitmqPassword);
		return connectionFactory;
	}

	@Bean
	Queue workflowQueue() {
		return new Queue(Application.workflowQueueName, false);
	}

	@Bean
	Queue locationQueue() {
		return new Queue(Application.locationQueueName, false);
	}

	@Bean
	Queue pathQueue() {
		return new Queue(Application.pathQueueName, false);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange("parisaccessible");
	}

	@Bean
	Binding workflowQueueBinding(Queue workflowQueue, TopicExchange exchange) {
		return BindingBuilder.bind(workflowQueue).to(exchange)
				.with(workflowQueue.getName());
	}

	@Bean
	Binding locationQueueBinding(Queue locationQueue, TopicExchange exchange) {
		return BindingBuilder.bind(locationQueue).to(exchange)
				.with(locationQueue.getName());
	}

	@Bean
	Binding pathQueueBinding(Queue pathQueue, TopicExchange exchange) {
		return BindingBuilder.bind(pathQueue).to(exchange)
				.with(pathQueue.getName());
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
		return jsonMessageConverter;
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(
				Application.class, args);

		CommandLineParser parser = new BasicParser();

		// create the Options
		Options options = new Options();
		options.addOption("index_trottoir", "index_trottoir", false,
				"do not hide entries starting with .");

		options.addOption("index_worker", "index_worker", true,
				"do not hide entries starting with .");

		options.addOption("total_worker", "total_worker", true,
				"do not hide entries starting with .");

		options.addOption("index_trip", "index_trip", false,
				"do not hide entries starting with .");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("index_trottoir")) {
				final TrottoirIndexerService trottoirIndexer = run
						.getBean(TrottoirIndexerService.class);

				String index_worker = line.getOptionValue("index_worker");
				String total_worker = line.getOptionValue("total_worker");

				Integer index_worker_int = Integer.valueOf(index_worker);
				Integer total_worker_int = Integer.valueOf(total_worker);

				trottoirIndexer.indexTrottoir(index_worker_int,
						total_worker_int);
			} else if (line.hasOption("index_trip")) {

			}

		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		run.close();
		System.exit(0);
	}
}