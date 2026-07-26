package com.lucasbdourado.autotimemarking.modules.configuration.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bmaquiosque")
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

    @NotBlank
    private String url;

    @NotNull
    private Selectors selectors = new Selectors();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Selectors getSelectors() {
        return selectors;
    }

    public void setSelectors(Selectors selectors) {
        this.selectors = selectors;
    }

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

    public static class Selectors {

        @NotBlank
        private String username;

        @NotBlank
        private String password;

        @NotBlank
        private String loginButton;

        @NotBlank
        private String markingsContainer;

        @NotBlank
        private String punchButton;

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

        public String getLoginButton() {
            return loginButton;
        }

        public void setLoginButton(String loginButton) {
            this.loginButton = loginButton;
        }

        public String getMarkingsContainer() {
            return markingsContainer;
        }

        public void setMarkingsContainer(String markingsContainer) {
            this.markingsContainer = markingsContainer;
        }

        public String getPunchButton() {
            return punchButton;
        }

        public void setPunchButton(String punchButton) {
            this.punchButton = punchButton;
        }

        @Override
        public String toString() {
            return "Selectors{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", loginButton='" + loginButton + '\'' +
                    ", markingsContainer='" + markingsContainer + '\'' +
                    ", punchButton='" + punchButton + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BmaquiosqueProperties{" +
                "url='" + url + '\'' +
                ", selectors=" + selectors +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", maxEntryTime='" + maxEntryTime + '\'' +
                ", jitterMinutes=" + jitterMinutes +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
