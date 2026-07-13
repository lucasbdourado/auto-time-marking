package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "bmaquiosque")
@Validated
public class BmaquiosqueProperties {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String maxEntryTime;

    @NotNull
    @Min(0)
    private Integer jitterMinutes;

    private String timezone = "America/Sao_Paulo";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMaxEntryTime() {
        return maxEntryTime;
    }

    public void setMaxEntryTime(String maxEntryTime) {
        this.maxEntryTime = maxEntryTime;
    }

    public Integer getJitterMinutes() {
        return jitterMinutes;
    }

    public void setJitterMinutes(Integer jitterMinutes) {
        this.jitterMinutes = jitterMinutes;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "BmaquiosqueProperties{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", maxEntryTime='" + maxEntryTime + '\'' +
                ", jitterMinutes=" + jitterMinutes +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
