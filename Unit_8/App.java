import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Weather Information App - Complete GUI application for real-time weather data
 * Uses WeatherAPI.com with proper JSON parsing
 */
public class App extends Application {
    
    // ==================== CONFIGURATION ====================
    // IMPORTANT: Use your actual API key from https://www.weatherapi.com
    private static final String API_KEY = "fa7bad591d2d45b7a8740215260201";
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json";
    
    // ==================== GUI COMPONENTS ====================
    private Label cityLabel, tempLabel, humidityLabel, windLabel, conditionLabel, feelsLikeLabel, timeLabel;
    private TextField cityInput;
    private Button searchBtn, toggleTempBtn, toggleWindBtn;
    private ListView<String> historyList;
    private ArrayList<String> searchHistory = new ArrayList<>();
    private boolean isCelsius = true;
    private boolean isKmh = true;
    private double currentTempCelsius = 0.0;
    private double currentWindKph = 0.0;
    private double currentTempFahrenheit = 0.0;
    private double currentFeelsLikeCelsius = 0.0;
    private double currentFeelsLikeFahrenheit = 0.0;    

    // ==================== LOGGING ====================
    /**
     * Logs messages to a file for debugging
     */
    private void logToFile(String message) {
        try {
            // Get project directory (current directory when running from VSCode)
            String projectDir = System.getProperty("user.dir");
            String logFilePath = projectDir + "/weather_app.log";
            
            // Write log with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            FileWriter writer = new FileWriter(logFilePath, true);
            writer.write("[" + timestamp + "] " + message + "\n");
            writer.close();
            
            System.out.println("LOG: " + message + " [File: " + logFilePath + "]");
        } catch (Exception e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    // ==================== MAIN METHOD ====================
    public static void main(String[] args) {
        launch(args);
    }
    
    // ==================== GUI SETUP ====================
    @Override
    public void start(Stage primaryStage) {
        logToFile("Application started");
        
        // 1. CREATE GUI COMPONENTS
        Label title = new Label("🌤️ Weather Information App [Anh Tuan Ho Studio (c) ]");
        title.setFont(Font.font("Arial", 26));
        title.setTextFill(Color.DARKBLUE);
        
        // Input section
        HBox inputBox = new HBox(10);
        cityInput = new TextField();
        cityInput.setPromptText("Enter city name (e.g., Hanoi, London)...");
        cityInput.setPrefWidth(300);
        cityInput.setPrefHeight(35);
        
        searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        searchBtn.setPrefHeight(35);
        inputBox.getChildren().addAll(cityInput, searchBtn);
        inputBox.setAlignment(Pos.CENTER);
        
        // Weather display area
        cityLabel = new Label("📍 Waiting for data...");
        cityLabel.setFont(Font.font("Arial", 22));
        cityLabel.setTextFill(Color.DARKSLATEBLUE);
        
        timeLabel = new Label("Last update: --:--");
        timeLabel.setFont(Font.font("Arial", 12));
        timeLabel.setTextFill(Color.GRAY);
        
        // Weather icon (using emoji as placeholder)
        Label weatherIcon = new Label("⛅");
        weatherIcon.setFont(Font.font("Arial", 60));
        
        tempLabel = new Label("-- °C");
        tempLabel.setFont(Font.font("Arial", 42));
        tempLabel.setTextFill(Color.ORANGERED);
        
        conditionLabel = new Label("--");
        conditionLabel.setFont(Font.font("Arial", 20));
        conditionLabel.setTextFill(Color.DARKGREEN);
        
        feelsLikeLabel = new Label("Feels like: -- °C");
        feelsLikeLabel.setFont(Font.font("Arial", 14));
        
        humidityLabel = new Label("💧 Humidity: --%");
        humidityLabel.setFont(Font.font("Arial", 14));
        
        windLabel = new Label("💨 Wind Speed: -- km/h");
        windLabel.setFont(Font.font("Arial", 14));
        
        // Unit toggle buttons
        toggleTempBtn = new Button("Switch to °F");
        toggleTempBtn.setDisable(true); // Disable until we have data
        toggleWindBtn = new Button("Switch to mph");
        toggleWindBtn.setDisable(true); // Disable until we have data
        HBox unitBox = new HBox(15, toggleTempBtn, toggleWindBtn);
        unitBox.setAlignment(Pos.CENTER);
        
        // Weather info container
        VBox weatherInfoBox = new VBox(10, cityLabel, timeLabel, weatherIcon, 
                                       tempLabel, conditionLabel, feelsLikeLabel, 
                                       humidityLabel, windLabel, unitBox);
        weatherInfoBox.setAlignment(Pos.CENTER);
        weatherInfoBox.setPadding(new Insets(25));
        weatherInfoBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 15; -fx-background-radius: 15;");
        
        VBox weatherDisplayBox = new VBox(weatherInfoBox);
        weatherDisplayBox.setAlignment(Pos.CENTER);
        weatherDisplayBox.setPadding(new Insets(20));
        
        // Search history section
        Label historyTitle = new Label("📜 Search History");
        historyTitle.setFont(Font.font("Arial", 16));
        
        historyList = new ListView<>();
        historyList.setPrefWidth(250);
        historyList.setPrefHeight(400);
        
        VBox historyBox = new VBox(10, historyTitle, historyList);
        historyBox.setPadding(new Insets(15));
        historyBox.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-radius: 10;");
        
        // 2. MAIN LAYOUT
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #e3f2fd, #f3e5f5);");
        
        // Top: Title
        root.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(0, 0, 20, 0));
        
        // Center: Input and weather display
        VBox centerBox = new VBox(25, inputBox, weatherDisplayBox);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);
        
        // Right: History
        root.setRight(historyBox);
        BorderPane.setMargin(historyBox, new Insets(0, 0, 0, 20));
        
        // 3. EVENT HANDLERS
        searchBtn.setOnAction(e -> {
            logToFile("Search button clicked for city: " + cityInput.getText());
            fetchWeatherData();
        });
        cityInput.setOnAction(e -> {
            logToFile("Enter pressed for city: " + cityInput.getText());
            fetchWeatherData();
        });
        toggleTempBtn.setOnAction(e -> toggleTemperatureUnit());
        toggleWindBtn.setOnAction(e -> toggleWindSpeedUnit());
        
        // Click on history item to search again
        historyList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String city = newVal.split(" \\| ")[0];
                logToFile("History item selected: " + city);
                cityInput.setText(city);
                Platform.runLater(this::fetchWeatherData);
            }
        });
        
        // 4. CREATE SCENE AND SHOW WINDOW
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Weather Information App - JavaFX (WeatherAPI.com)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.show();
        
        logToFile("GUI setup complete");

        // Auto-search for Chicago on startup
        cityInput.setText("Chicago");
        fetchWeatherData();
    }
    
    // ==================== WEATHER API METHODS ====================
    
    /**
     * Fetches weather data from WeatherAPI.com
     */
    private void fetchWeatherData() {
        String city = cityInput.getText().trim();
        if (city.isEmpty()) {
            showError("Input Error", "Please enter a city name.");
            return;
        }
        
        logToFile("Starting API request for city: " + city);
        
        try {
            // Encode city name for URL
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String urlString = API_URL + "?key=" + API_KEY + "&q=" + encodedCity + "&aqi=no";
            
            logToFile("Request URL: " + urlString.replace(API_KEY, "API_KEY_HIDDEN"));
            
            // Create HTTP connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000); // 10 second timeout
            conn.setReadTimeout(10000);
            
            // Check response code
            int responseCode = conn.getResponseCode();
            logToFile("HTTP Response Code: " + responseCode);
            
            if (responseCode == 200) {
                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Log the raw response (first 500 chars)
                String responseStr = response.toString();
                logToFile("Raw API Response (first 500 chars): " + 
                          (responseStr.length() > 500 ? responseStr.substring(0, 500) + "..." : responseStr));
                
                // Parse JSON response
                WeatherData data = parseWeatherJson(responseStr);
                if (data != null) {
                    logToFile("Parsed data: " + data.cityName + ", Temp: " + data.temperatureC + "°C, Condition: " + data.condition);
                    processWeatherData(data);
                    addToHistory(city);
                } else {
                    logToFile("Failed to parse weather data");
                    showError("Data Error", "Failed to parse weather data. Check log file.");
                }
                
            } else if (responseCode == 400) {
                String errorMsg = "Bad request. City '" + city + "' not found or invalid.";
                logToFile("API Error 400: " + errorMsg);
                showError("City Not Found", errorMsg);
            } else if (responseCode == 401) {
                String errorMsg = "Invalid API Key. Please check your WeatherAPI key.";
                logToFile("API Error 401: " + errorMsg);
                showError("API Error", errorMsg);
            } else if (responseCode == 403) {
                String errorMsg = "Access forbidden. API key may be disabled or invalid.";
                logToFile("API Error 403: " + errorMsg);
                showError("API Error", errorMsg);
            } else {
                String errorMsg = "Failed to fetch data. Error code: " + responseCode;
                logToFile("API Error " + responseCode + ": " + errorMsg);
                showError("API Error", errorMsg);
            }
            
            conn.disconnect();
            
        } catch (Exception e) {
            String errorMsg = "Cannot connect to weather service: " + e.getMessage();
            logToFile("Connection Error: " + errorMsg);
            e.printStackTrace();
            showError("Connection Error", "Cannot connect to weather service. Check your internet connection.");
        }
    }
    
    /**
     * Simple data class to store weather information
     */
    private static class WeatherData {
        String cityName;
        double temperatureC;
        double temperatureF;
        int humidity;
        double feelsLikeC;
        double feelsLikeF;
        double windKph;
        String condition;
        String country;
    }
    
    /**
     * Manual JSON parsing for WeatherAPI.com response
     */
    private WeatherData parseWeatherJson(String jsonString) {
        try {
            logToFile("Starting JSON parsing...");
            WeatherData data = new WeatherData();
            
            // Parse location information
            if (jsonString.contains("\"location\":")) {
                int locStart = jsonString.indexOf("\"location\":") + 11;
                int locEnd = findMatchingBrace(jsonString, locStart);
                String locationJson = jsonString.substring(locStart, locEnd);
                
                // Extract city name
                data.cityName = extractJsonStringValue(locationJson, "\"name\":");
                
                // Extract country
                data.country = extractJsonStringValue(locationJson, "\"country\":");
                
                logToFile("Parsed city: " + data.cityName + ", country: " + data.country);
            }
            
            // Parse current weather information
            if (jsonString.contains("\"current\":")) {
                int currStart = jsonString.indexOf("\"current\":") + 10;
                int currEnd = findMatchingBrace(jsonString, currStart);
                String currentJson = jsonString.substring(currStart, currEnd);
                
                logToFile("Current JSON: " + currentJson.substring(0, Math.min(300, currentJson.length())) + "...");
                
                // Extract temperature in Celsius
                String tempCStr = extractJsonNumberValue(currentJson, "\"temp_c\":");
                if (tempCStr != null && !tempCStr.isEmpty()) {
                    data.temperatureC = Double.parseDouble(tempCStr);
                    logToFile("Parsed temp_c: " + data.temperatureC);
                }
                
                // Extract temperature in Fahrenheit
                String tempFStr = extractJsonNumberValue(currentJson, "\"temp_f\":");
                if (tempFStr != null && !tempFStr.isEmpty()) {
                    data.temperatureF = Double.parseDouble(tempFStr);
                }
                
                // Extract humidity
                String humidityStr = extractJsonNumberValue(currentJson, "\"humidity\":");
                if (humidityStr != null && !humidityStr.isEmpty()) {
                    data.humidity = Integer.parseInt(humidityStr);
                    logToFile("Parsed humidity: " + data.humidity);
                }
                
                // Extract feels like in Celsius
                String feelsLikeCStr = extractJsonNumberValue(currentJson, "\"feelslike_c\":");
                if (feelsLikeCStr != null && !feelsLikeCStr.isEmpty()) {
                    data.feelsLikeC = Double.parseDouble(feelsLikeCStr);
                }
                
                // Extract feels like in Fahrenheit
                String feelsLikeFStr = extractJsonNumberValue(currentJson, "\"feelslike_f\":");
                if (feelsLikeFStr != null && !feelsLikeFStr.isEmpty()) {
                    data.feelsLikeF = Double.parseDouble(feelsLikeFStr);
                }
                
                // Extract wind speed in kph
                String windKphStr = extractJsonNumberValue(currentJson, "\"wind_kph\":");
                if (windKphStr != null && !windKphStr.isEmpty()) {
                    data.windKph = Double.parseDouble(windKphStr);
                    logToFile("Parsed wind_kph: " + data.windKph);
                }
                
                // Extract condition text
                if (currentJson.contains("\"condition\":")) {
                    int condStart = currentJson.indexOf("\"condition\":") + 12;
                    int condEnd = findMatchingBrace(currentJson, condStart);
                    String conditionJson = currentJson.substring(condStart, condEnd);
                    
                    data.condition = extractJsonStringValue(conditionJson, "\"text\":");
                    logToFile("Parsed condition: " + data.condition);
                }
            }
            
            logToFile("JSON parsing completed successfully");
            logToFile("Parsed data: " + data.cityName + ", Temp: " + data.temperatureC + "°C, Humidity: " + 
                      data.humidity + "%, Wind: " + data.windKph + " km/h, Condition: " + data.condition);
            return data;
            
        } catch (Exception e) {
            logToFile("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to find the matching closing brace for a JSON object
     */
    private int findMatchingBrace(String json, int startIndex) {
        int braceCount = 0;
        boolean inQuotes = false;
        
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{' || c == '[') {
                    braceCount++;
                } else if (c == '}' || c == ']') {
                    braceCount--;
                    if (braceCount < 0) {
                        return i + 1;
                    }
                } else if (c == '}' && braceCount == 0) {
                    return i + 1;
                }
            }
        }
        return json.length();
    }

    /**
     * Helper method to extract string value from JSON
     */
    private String extractJsonStringValue(String json, String key) {
        try {
            if (!json.contains(key)) {
                return null;
            }
            
            int keyIndex = json.indexOf(key);
            if (keyIndex == -1) return null;
            
            int valueStart = json.indexOf("\"", keyIndex + key.length()) + 1;
            if (valueStart <= 0) return null;
            
            int valueEnd = json.indexOf("\"", valueStart);
            if (valueEnd <= valueStart) return null;
            
            return json.substring(valueStart, valueEnd);
        } catch (Exception e) {
            logToFile("Error extracting string value for key " + key + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to extract number value from JSON
     */
    private String extractJsonNumberValue(String json, String key) {
        try {
            if (!json.contains(key)) {
                return null;
            }
            
            int keyIndex = json.indexOf(key);
            if (keyIndex == -1) return null;
            
            int valueStart = keyIndex + key.length();
            int valueEnd = valueStart;
            
            // Find the end of the number (either comma or closing brace)
            while (valueEnd < json.length()) {
                char c = json.charAt(valueEnd);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                valueEnd++;
            }
            
            String numberStr = json.substring(valueStart, valueEnd).trim();
            // Remove any trailing commas
            if (numberStr.endsWith(",")) {
                numberStr = numberStr.substring(0, numberStr.length() - 1);
            }
            
            return numberStr;
        } catch (Exception e) {
            logToFile("Error extracting number value for key " + key + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Processes weather data and updates GUI
     */
    private void processWeatherData(WeatherData data) {
        logToFile("Processing weather data for display");
        
        // Store current data for unit conversion
        currentTempCelsius = data.temperatureC;
        currentTempFahrenheit = data.temperatureF;
        currentFeelsLikeCelsius = data.feelsLikeC;
        currentFeelsLikeFahrenheit = data.feelsLikeF;
        currentWindKph = data.windKph;
        
        // Update GUI with retrieved data
        updateWeatherDisplay(data);
        
        // Enable unit toggle buttons
        toggleTempBtn.setDisable(false);
        toggleWindBtn.setDisable(false);
    }
    
    /**
     * Updates all weather information labels in the GUI
     */
    private void updateWeatherDisplay(WeatherData data) {
        logToFile("Updating display with: " + data.cityName + ", " + data.temperatureC + "°C");
        
        // Update city and time
        if (data.country != null) {
            cityLabel.setText("📍 " + data.cityName + ", " + data.country);
        } else {
            cityLabel.setText("📍 " + data.cityName);
        }
        
        timeLabel.setText("Last update: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")));
        
        // Display temperature (default Celsius)
        if (isCelsius) {
            tempLabel.setText(String.format("%.1f °C", data.temperatureC));
            feelsLikeLabel.setText(String.format("Feels like: %.1f °C", data.feelsLikeC));
        } else {
            tempLabel.setText(String.format("%.1f °F", data.temperatureF));
            feelsLikeLabel.setText(String.format("Feels like: %.1f °F", data.feelsLikeF));
        }
        
        // Display weather condition
        if (data.condition != null) {
            conditionLabel.setText(capitalizeFirstLetter(data.condition));
        } else {
            conditionLabel.setText("--");
        }
        
        // Display humidity
        humidityLabel.setText("💧 Humidity: " + data.humidity + "%");
        
        // Display wind speed
        if (isKmh) {
            windLabel.setText(String.format("💨 Wind Speed: %.1f km/h", data.windKph));
        } else {
            windLabel.setText(String.format("💨 Wind Speed: %.1f mph", data.windKph * 0.621371));
        }
        
        // Update background based on time of day
        updateBackgroundByTime();
        
        logToFile("Display update complete");
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Adds a city to search history
     */
    private void addToHistory(String city) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String entry = city + " | " + timeStamp;
        
        // Add to beginning of list if not already present
        if (!searchHistory.contains(entry)) {
            searchHistory.add(0, entry);
            
            // Keep only last 10 searches
            if (searchHistory.size() > 10) {
                searchHistory.remove(10);
            }
            
            // Update ListView
            historyList.getItems().setAll(searchHistory);
            logToFile("Added to history: " + entry);
        }
    }
    
    /**
     * Toggles between Celsius and Fahrenheit
     */
    private void toggleTemperatureUnit() {
        isCelsius = !isCelsius;
        toggleTempBtn.setText(isCelsius ? "Switch to °F" : "Switch to °C");
        
        // Update temperature display using stored data
        if (isCelsius) {
            tempLabel.setText(String.format("%.1f °C", currentTempCelsius));
            feelsLikeLabel.setText(String.format("Feels like: %.1f °C", currentFeelsLikeCelsius));
        } else {
            tempLabel.setText(String.format("%.1f °F", currentTempFahrenheit));
            feelsLikeLabel.setText(String.format("Feels like: %.1f °F", currentFeelsLikeFahrenheit));
        }
        
        logToFile("Temperature unit toggled to: " + (isCelsius ? "Celsius" : "Fahrenheit"));
    }
    
    /**
     * Toggles between km/h and mph
     */
    private void toggleWindSpeedUnit() {
        isKmh = !isKmh;
        toggleWindBtn.setText(isKmh ? "Switch to mph" : "Switch to km/h");
        
        // Update wind speed display
        if (isKmh) {
            windLabel.setText(String.format("💨 Wind Speed: %.1f km/h", currentWindKph));
        } else {
            windLabel.setText(String.format("💨 Wind Speed: %.1f mph", currentWindKph * 0.621371));
        }
        
        logToFile("Wind speed unit toggled to: " + (isKmh ? "km/h" : "mph"));
    }
    
    /**
     * Changes background color based on time of day
     */
    private void updateBackgroundByTime() {
        int hour = LocalDateTime.now().getHour();
        String style = "-fx-background-color: ";
        
        if (hour >= 6 && hour < 12) {
            style += "linear-gradient(to bottom, #e3f2fd, #bbdefb);"; // Morning
        } else if (hour >= 12 && hour < 18) {
            style += "linear-gradient(to bottom, #fff3e0, #ffcc80);"; // Afternoon
        } else if (hour >= 18 && hour < 21) {
            style += "linear-gradient(to bottom, #f3e5f5, #e1bee7);"; // Evening
        } else {
            style += "linear-gradient(to bottom, #212121, #37474f);"; // Night
        }
        
        logToFile("Background updated for hour: " + hour);
    }
    
    /**
     * Capitalizes the first letter of a string
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Shows an error dialog
     */
    private void showError(String title, String message) {
        logToFile("Showing error: " + title + " - " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}