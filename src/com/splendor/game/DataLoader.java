package src.com.splendor.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

public class DataLoader {

    private static Properties properties = new Properties();

    public DataLoader() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            // Load the properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key){
        return properties.getProperty(key, null);
    }

    public String readResourceFile(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource file " + fileName, e);
        }
    }
}
