package com.renthouses.enviroment.buttons;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Button {

    public abstract EditMessageText editMessage(Update update);

    public abstract boolean support(String cmd);
}
