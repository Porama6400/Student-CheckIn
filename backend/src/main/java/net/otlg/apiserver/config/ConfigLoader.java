package net.otlg.apiserver.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;

public class ConfigLoader {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Object load(File file, Type type) throws FileNotFoundException {
        try (FileReader fileReader = new FileReader(file)) {
            return GSON.fromJson(new FileReader(file), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object loadOrSaveDefault(File file, Object defaultObject) throws IOException {
        if (!file.exists()) {
            save(file, defaultObject);
        }

        return load(file, defaultObject.getClass());
    }

    public static void save(File file, Object out) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            GSON.toJson(out, fileWriter);
            fileWriter.flush();
        }
    }
}
