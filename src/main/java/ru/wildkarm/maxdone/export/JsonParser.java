package ru.wildkarm.maxdone.export;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonParser {

    public static TypeToken<?> buildType(Type mainClass, Type... paramClasses) {
        return TypeToken.getParameterized(mainClass, paramClasses);
    }

    public static <T> T desJson(String file, Type mainClass, Type... paramClasses) throws FileNotFoundException {
        log.warn("desJson START for file  {}", file);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, OFFSET_DESERIALIZER);
        Gson gson = builder.create();
        
        Optional<T> resObj = Optional.empty();
        try (Reader reader = new BufferedReader(new FileReader(file))) {

            Type type = buildType(mainClass, paramClasses).getType();
            resObj = Optional.ofNullable(gson.fromJson(reader, type));

        } catch (Exception e) {
            log.error("desJson Reader ERROR: ", e);
        }

        log.info("desJson succeed = {}\n\n", resObj);
        return resObj.orElseThrow(FileNotFoundException::new);
    }

    public static <T> T parseJsonSimple(String input, Type mainClass, Type... paramClasses) {
        return new Gson()
                .fromJson(
                        input, 
                        TypeToken.getParameterized(mainClass, paramClasses).getType());
    }

    public static void testJsonParsing() {
        Integer intInput = 3456;        
        List<Integer> listInput = List.of(777, 888, 999);
        Map<Integer, String> mapInput = Map.of(11, "AA", 22, "BB", 33, "CC");
        List<Map<Integer, String>> mapListInput = List.of(
                Map.of(11, "AA", 22, "BB", 33, "CC"), 
                Map.of(44, "AA", 55, "BB", 66, "CC"));
        
        Gson gson = new Gson();
        String intInputString = gson.toJson(intInput);
        String listInputString = gson.toJson(listInput);
        String mapInputString = gson.toJson(mapInput);
        String mapListInputString = gson.toJson(mapListInput);
        
        log.info("intInputString = {}", intInputString);
        log.info("listInputString = {}", listInputString);
        log.info("mapInputString = {}", mapInputString);
        log.info("mapListInputString = {}", mapListInputString);

        Integer intObjet = parseJsonSimple(intInputString, Integer.class);
        List<Integer> listObject = parseJsonSimple(listInputString, List.class, Integer.class);
        Map<Integer, String> mapObject = parseJsonSimple(mapInputString, Map.class, Integer.class, String.class);
        List<Map<Integer, String>> mapListObject = parseJsonSimple(mapListInputString, List.class, new TypeToken<Map<Integer, String>>(){}.getType());
        
        log.info("intObjet = {}", intObjet);
        log.info("listObject = {}", listObject);
        log.info("mapObject = {}", mapObject);
        log.info("mapListObject = {}", mapListObject);
    }

    public static final DateTimeFormatter EXT_ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .parseLenient()
            .appendLiteral("+0000")
            .parseStrict().toFormatter();

    public static final JsonSerializer<LocalDateTime> OFFSET_SERIALIZER =
            (src, typeOfSrc, context) -> src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    public static final JsonDeserializer<LocalDateTime> OFFSET_DESERIALIZER = (json, typeOfT, context) -> {
        JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();

        try {
            if (jsonPrimitive.isString()) {
                return LocalDateTime.parse(jsonPrimitive.getAsString(), EXT_ISO_OFFSET_DATE_TIME);
            }

            if (jsonPrimitive.isNumber()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonPrimitive.getAsLong()), ZoneId.systemDefault());
            }
        } catch (RuntimeException var5) {
            throw new JsonParseException("Unable to parse LocalDateTime", var5);
        }

        throw new JsonParseException("Unable to parse LocalDateTime");
    };

    public static final DateTimeFormatter READABLE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"));

}
