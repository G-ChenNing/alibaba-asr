package com.github.gchenning.asr.config;

import com.alibaba.nls.client.AccessToken;
import com.github.gchenning.asr.socket.HappyChatMain;
import com.github.gchenning.asr.system.SpeechRecognizerDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(value = 10)

public class ApplicationInit implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInit.class);

    @Autowired
    private HappyChatMain happyChatMain;

    @Override
    public void run(String... args) throws IOException {
        SpeechRecognizerDemo.getToken();

        SpeechRecognizerDemo.getInstance();

        happyChatMain.startNetty();
    }

}
