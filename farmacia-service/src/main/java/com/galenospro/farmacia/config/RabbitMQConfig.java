package com.galenospro.farmacia.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${galenos.rabbitmq.exchange}")     private String exchange;
    @Value("${galenos.rabbitmq.cola.solicitud}") private String colaSolicitud;
    @Value("${galenos.rabbitmq.cola.despacho}")  private String colaDespacho;

    @Bean
    public DirectExchange galenosexchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue colaSolicitudNueva() {
        return new Queue(colaSolicitud, true);
    }

    @Bean
    public Queue colaDespachoConfirmado() {
        return new Queue(colaDespacho, true);
    }

    @Bean
    public Binding bindingSolicitud(Queue colaSolicitudNueva, DirectExchange galenosexchange) {
        return BindingBuilder.bind(colaSolicitudNueva).to(galenosexchange).with("solicitud.nueva");
    }

    @Bean
    public Binding bindingDespacho(Queue colaDespachoConfirmado, DirectExchange galenosexchange) {
        return BindingBuilder.bind(colaDespachoConfirmado).to(galenosexchange).with("despacho.confirmado");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
