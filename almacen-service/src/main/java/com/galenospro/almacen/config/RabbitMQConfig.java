package com.galenospro.almacen.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${galenos.rabbitmq.exchange}") private String exchange;
    @Value("${galenos.rabbitmq.cola.solicitud}") private String colaSolicitud;
    @Value("${galenos.rabbitmq.cola.despacho}") private String colaDespacho;
    @Value("${galenos.rabbitmq.cola.stock-critico}") private String colaStockCritico;

    @Bean
    public DirectExchange galenasExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue colaSolicitudNueva() {
        return QueueBuilder.durable(colaSolicitud).build();
    }

    @Bean
    public Queue colaDespachoConfirmado() {
        return QueueBuilder.durable(colaDespacho).build();
    }

    @Bean
    public Queue colaStockCritico() {
        return QueueBuilder.durable(colaStockCritico).build();
    }

    @Bean
    public Binding bindingSolicitud(Queue colaSolicitudNueva, DirectExchange galenasExchange) {
        return BindingBuilder.bind(colaSolicitudNueva).to(galenasExchange).with("solicitud.nueva");
    }

    @Bean
    public Binding bindingDespacho(Queue colaDespachoConfirmado, DirectExchange galenasExchange) {
        return BindingBuilder.bind(colaDespachoConfirmado).to(galenasExchange).with("despacho.confirmado");
    }

    @Bean
    public Binding bindingStockCritico(Queue colaStockCritico, DirectExchange galenasExchange) {
        return BindingBuilder.bind(colaStockCritico).to(galenasExchange).with("stock.critico");
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
