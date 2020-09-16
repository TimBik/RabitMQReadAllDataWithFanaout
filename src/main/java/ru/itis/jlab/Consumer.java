package ru.itis.jlab;

import com.beust.jcommander.JCommander;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.printing.Scaling;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Consumer {

    private final static String EXCHANGE_NAME = "parameters";

    private final static String EXCHANGE_TYPE = "fanout";

    static private String[] templatesFiles = new String[]{
            "applyForLoan.txt",
            "dismissed.txt"
    };

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        String packageName = "pdf_package№" + RandomStringUtils.random(5, false, true);
        Scanner sc = new Scanner(System.in);
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);

            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            // создаем временную очередь со случайным названием
            String queue = channel.queueDeclare().getQueue();
            System.out.println("выберете предложенный файл:");
            for (int i = 0; i < templatesFiles.length; i++) {
                System.out.println((i + 1) + " - " + templatesFiles[i]);
            }
            int idTemplate = sc.nextInt() - 1;
            String template = templatesFiles[idTemplate];
            // привязали очередь к EXCHANGE_NAME
            channel.queueBind(queue, EXCHANGE_NAME, "");

            DeliverCallback deliverCallback = (consumerTag, message) -> {
                try {
                    User user = new User();
                    String userString = new String(message.getBody());

                    String s[] = userString.split(" ");
                    JCommander.newBuilder()
                            .addObject(user)
                            .build()
                            .parse(s);

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("templates/" + template)));
                    String helpFileName = RandomStringUtils.random(5, true, true) + ".txt";
                    PrintWriter pw = new PrintWriter(new FileWriter("created/help/" + helpFileName));
                    while (br.ready()) {
                        String line = br.readLine();
                        Map<String, Object> map = user.getParameters();
                        for (Map.Entry<String, Object> e :
                                map.entrySet()) {

                            line = line.replaceAll("\\{" + e.getKey() + "}", e.getValue().toString());
                        }
                        pw.println(line);
                    }
                    br.close();
                    pw.close();
                    System.out.println("created help file");
                    String folderName = "created/" + packageName;
                    String allFileName = folderName + "/" + RandomStringUtils.random(5, true, true) + ".pdf";
                    File folder = new File(folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(allFileName));

                    document.open();
                    Font font = FontFactory.getFont(FontFactory.COURIER, 4, BaseColor.BLACK);

                    BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("created/help/" + helpFileName)));
                    while (br2.ready()) {
                        String brString = br2.readLine();
                        System.out.println(brString);
                        Chunk chunk = new Chunk(brString, font);
                        document.add(chunk);
                    }
                    document.close();
                    br2.close();
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException | DocumentException e) {
                    System.err.println("FAILED");
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }
            };
//            while (true) {
            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {
            });
//            }
        } catch (TimeoutException | IOException e) {
            throw new IllegalArgumentException();
        }
    }
}
