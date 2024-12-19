package hiddenlayer;

public class Config {
    private String hlClientId;
    private String hlClientSecret;

    public Config(String hlClientId, String hlClientSecret) {
        this.hlClientId = hlClientId;
        this.hlClientSecret = hlClientSecret;
    }

    public String getHlClientId() {
        return hlClientId;
    }

    public String getHlClientSecret() {
        return hlClientSecret;
    }
}
