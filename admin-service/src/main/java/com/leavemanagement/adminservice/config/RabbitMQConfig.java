package com.leavemanagement.adminservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "leave-management-exchange";
    public static final String TIMESHEET_QUEUE = "timesheet-approval-queue";
    public static final String LEAVE_QUEUE = "leave-approval-queue";
    public static final String TIMESHEET_ROUTING_KEY = "routing.timesheet.approve";
    public static final String LEAVE_ROUTING_KEY = "routing.leave.approve";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue timesheetQueue() {
        return new Queue(TIMESHEET_QUEUE);
    }

    @Bean
    public Queue leaveQueue() {
        return new Queue(LEAVE_QUEUE);
    }

    @Bean
    public Binding bindingTimesheet(Queue timesheetQueue, TopicExchange exchange) {
        return BindingBuilder.bind(timesheetQueue).to(exchange).with(TIMESHEET_ROUTING_KEY);
    }

    @Bean
    public Binding bindingLeave(Queue leaveQueue, TopicExchange exchange) {
        return BindingBuilder.bind(leaveQueue).to(exchange).with(LEAVE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
