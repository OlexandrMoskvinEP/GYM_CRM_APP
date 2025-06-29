package com.gym.crm.app.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.app.domain.model.Trainee;
import com.gym.crm.app.domain.model.Trainer;
import com.gym.crm.app.domain.model.Training;
import com.gym.crm.app.exception.UnacceptableOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.gym.crm.app.storage.JsonStorageHandler.Namespace.TRAINEE;
import static com.gym.crm.app.storage.JsonStorageHandler.Namespace.TRAINER;
import static com.gym.crm.app.storage.JsonStorageHandler.Namespace.TRAINING;

@Component
public class JsonStorageHandler {
    @Value("${storage.filePath}")
    private String storageFilePath;

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<Namespace, Map<String, ?>> loadEntitiesFromFile() {
        Map<String, Object> fileStorage = loadRawStorage();

        Map<String, Trainer> trainers = parseSection(fileStorage, TRAINER.name(), Trainer.class);
        Map<String, Trainee> trainees = parseSection(fileStorage, TRAINEE.name(), Trainee.class);
        Map<String, Training> trainings = parseSection(fileStorage, TRAINING.name(), Training.class);

        Map<Namespace, Map<String, ?>> loadedData = new HashMap<>();
        loadedData.put(TRAINER, trainers);
        loadedData.put(TRAINEE, trainees);
        loadedData.put(TRAINING, trainings);

        return loadedData;
    }

    private HashMap<String, Object> loadRawStorage() {
        try (InputStream input = new FileInputStream(storageFilePath)) {
            return objectMapper.readValue(input, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON file", e);
        }
    }

    public <T> Map<String, T> parseSection(Map<String, Object> raw, String key, Class<T> clazz) {
        Map<String, T> result = new HashMap<>();
        Object rawSection = raw.get(key);

        if (rawSection instanceof Map<?, ?> sectionMap) {
            for (var entry : sectionMap.entrySet()) {
                String id = entry.getKey().toString();
                T obj = objectMapper.convertValue(entry.getValue(), clazz);
                result.put(id, obj);
            }
        }
        return result;
    }

    public void save(Map<Namespace, Map<String, ?>> storage) throws UnacceptableOperationException {
        Map<String, Object> toWrite = new HashMap<>();

        toWrite.put(TRAINER.name(), storage.get(TRAINER));
        toWrite.put(TRAINEE.name(), storage.get(TRAINEE));
        toWrite.put(TRAINING.name(), storage.get(TRAINING));

        try (Writer writer = new FileWriter(storageFilePath)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, toWrite);
        } catch (IOException e) {
            throw new UnacceptableOperationException("Error occurred while try to write into the file!");
        }
    }

    public enum Namespace {
        TRAINER,
        TRAINEE,
        TRAINING
    }
}
