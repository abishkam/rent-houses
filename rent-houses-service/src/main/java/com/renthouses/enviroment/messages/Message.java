package com.renthouses.enviroment.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.ParseException;

public abstract class Message {


    public abstract SendMessage sendMessage(Update update) throws ParseException;

    public abstract boolean support(String cmd);
}
