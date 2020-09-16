package ru.itis.jlab;

import com.beust.jcommander.JCommander;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Producer {

    private final static String EXCHANGE_NAME = "parameters";
    // тип FANOUT
    private final static String EXCHANGE_TYPE = "fanout";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создаем exchange
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            // открываем файл с картинками
            while (true) {
                User user = new User();
                String s[] = sc.nextLine().split(" ");
                JCommander.newBuilder()
                        .addObject(user)
                        .build()
                        .parse(s);
                // публикую в exchange
                channel.basicPublish(EXCHANGE_NAME, "", null, user.getBytes());
            }
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }

    }

}
//name Mem surname Lol passport 222 issue 16.03.2001
//name Tim surname Bik passport 111 issue 16.03.2000