package com.renthouses.enviroment.properties;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "")
public class TelegramProperties {
    /**
     * Имя бота.
     */
    @NotBlank
    private String botUserName;
    /**
     * Токен.
     */
    @NotBlank
    private String botToken;
}
