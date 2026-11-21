package br.com.uniube.todo.config;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;

//Adapter para ensinar Gson a converter data

public class LocalDateAdapter {

    //LocalDate para JSON
    public static final JsonSerializer<LocalDate> SERIALIZER =
            (data, tipo, contexto) -> new JsonPrimitive(data.toString());

    //JSON para LocalDate
    public static final JsonDeserializer<LocalDate> DESERIALIZER =
            (json, tipo, contexto) -> LocalDate.parse(json.getAsString());
}
