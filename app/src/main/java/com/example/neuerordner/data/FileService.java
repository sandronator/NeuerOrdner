
package com.example.neuerordner.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class FileService<K, V, T> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private final OutputStreamWriter writer;
    private final InputStreamReader reader;

    public FileService(OutputStream outputStream) throws IOException {
        this.writer = new OutputStreamWriter(outputStream);
        this.reader = null;
    }

    public FileService(InputStream inputStream) throws IOException {
        this.writer = null;
        this.reader = new InputStreamReader(inputStream);
    }

    public void Write(Map<K, V> dictonary) throws IOException{
        if (writer == null)  {
            return;
        }
        gson.toJson(dictonary, this.writer);
        writer.flush();
        writer.close();
    }

    public <T> T fetchJson(TypeToken ttoken) throws FileNotFoundException {
        return reader == null ? new Gson().fromJson("{}", ttoken.getType()) : gson.fromJson(reader, ttoken.getType());
    }

    public void dump(Map<String, List<Item>> mapList, OutputStream outputStream) throws IOException {
        FileService<String, List<Item>, Map<Location, List<Item>>> file = new FileService(outputStream);
        file.Write(mapList);
    }

}
