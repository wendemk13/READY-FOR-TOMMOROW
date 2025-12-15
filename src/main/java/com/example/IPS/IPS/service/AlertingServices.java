package com.example.IPS.IPS.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AlertingServices {

    private static final Logger logger = LoggerFactory.getLogger(AlertingServices.class);
    private final JavaMailSender mailSender;

    public void sendAlert(LocalDate date, long totalFailures, double failedAmount, double failurePercentage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("wendemk13@gmail.com");
        message.setSubject("Critical Transaction Alert: Action Needed");
        message.setText(
                "⚠️ Transaction Failure Report ⚠️\n\n" +
                        "📅 Date: " + date + "\n" +
                        "❌ Total Failed Transactions: " + totalFailures + "\n" +
                        "💰 Total Failed Amount: $" + failedAmount + "\n" +
                        "📊 Failure Percentage: " + failurePercentage + "%\n\n" +
                        "Please review these failures immediately to avoid potential issues."
        );


        mailSender.send(message);
        logger.info("IPS Alert email sent on {}: totalFailures={}, failedAmount={}, failurePercentage={}",
                date, totalFailures, failedAmount, failurePercentage);
    }


    //    send alerting for IPS
    public void sendIPSAlert(LocalDate date, long totalFailures, double failedAmount, double failurePercentage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("wendemk13@gmail.com");
        message.setSubject("Transaction Alert - Failures Exceeded");
        message.setText(
                "Date: " + date +
                        "\nTotal Failures: " + totalFailures +
                        "\nFailed Amount: $" + failedAmount +
                        "\nFailure Percentage: " + failurePercentage + "%"
        );

        mailSender.send(message);
        System.out.println("Alert email sent!");
    }

}
