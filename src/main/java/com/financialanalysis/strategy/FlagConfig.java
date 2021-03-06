package com.financialanalysis.strategy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class FlagConfig {
    // EXIT
    int flagLookAheadDays = 5;
    double percentageMaxTarget = 0.27;
    double allowableGapUp = 0.99;
    double minPvoHist = 8.0;
    double zigzagThreshold = 0.2;

    // ENTRY

    // pvo
    int defaultFastPeriod = 12;
    int defaultSlowPeriod = 26;
    int defaultSignalPeriod = 9;

    // Flag
    // Determine the shape of flag top and flag pole
    int minFlagTopLen = 5;
    int maxFlagTopLen = 10;
    int minFlagPoleLen = 5;
    int maxFlagPoleLen = 30;
    int maxExtremaLookBackPeriod = 3;
    int maxExtremaLookForwardPeriod = 3;
    int minExtremaLookBackPeriod = 3;
    int minExtremaLookForwardPeriod = 3;

    // Determine the contraints on flag shape
    double flagPoleSlopeThreshold = 0.0;
    double flagPoleRSquareThreshold = 0.8;
    double trendBotSlopeThresholdLower = -1.0;
    double trendBotSlopeThresholdHigher = 0.0;
    double trendTopSlopeThresholdLower = -1.0;
    double trendTopSlopeThresholdHigher = 0.0;
    double trendRSquareThreshold = 0.8;
    double topBotSlopeDifferenceThreshold = 0.03;
    int numTopDataPoints = 2;
    int numBotDataPoints = 2;

    // Determine if there is enough stock movement
    int maxInsufficientMovementCloseToPrevClose = 2;
    int maxInsufficientMovementHighToLowCount = 3;

    private final static String FILE_NAME = "flag";
    static {
        createFlagConfig();
    }

    @SneakyThrows
    private static void createFlagConfig() {
        Path path = Paths.get(getFlagConfigDir());
        if(!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private static String getFlagConfigDir() {
        return "var/config/";
    }

    @SneakyThrows
    public static FlagConfig readFromFile() {
        File configFile = new File(getFlagConfigDir() + FILE_NAME);
        String json = FileUtils.readFileToString(configFile);

        Gson gson = new Gson();
        FlagConfig config = gson.fromJson(json, FlagConfig.class);

        return config;
    }

    public static FlagConfig defaultConfig() {
        FlagConfig config = new FlagConfig();
        return config;
    }

    @SneakyThrows
    public static void saveDefault() {
        FlagConfig config = new FlagConfig();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(config);

        File configFile = new File(getFlagConfigDir() + FILE_NAME);
        FileUtils.writeStringToFile(configFile, json);
    }
}
