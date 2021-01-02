package ru.wildkarm.maxdone.export.model;



public interface JsonToPojoConverter<T> {
    T convert (String jsonObject);
}
