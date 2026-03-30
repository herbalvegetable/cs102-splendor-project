package src.com.splendor.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** JSON row from the home page: {@code {"name":"...","human":true}} */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerSetupPayload {
    private String name;
    private boolean human;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHuman() {
        return human;
    }

    public void setHuman(boolean human) {
        this.human = human;
    }
}
