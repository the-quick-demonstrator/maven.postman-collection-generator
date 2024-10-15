package com.github.curriculeon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

@Configuration
public class PostmanCollectionGenerator {

    public static void main(String[] args) throws Exception {
        String basePackage = "com.example.controllers"; // Replace with your base package
        Reflections reflections = new Reflections(basePackage);

        // Find all classes annotated with @RestController
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);

        // Prepare Postman collection structure
        Map<String, Object> postmanCollection = new HashMap<>();
        postmanCollection.put("info", createCollectionInfo());
        List<Map<String, Object>> itemList = new ArrayList<>();

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                Map<String, Object> requestData = extractRequestData(method);
                if (requestData != null) {
                    itemList.add(requestData);
                }
            }
        }

        postmanCollection.put("item", itemList);

        // Serialize the Postman collection to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("postman-collection.json"), postmanCollection);
        System.out.println("Postman collection generated: postman-collection.json");
    }

    private static Map<String, Object> createCollectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Spring Boot API Collection");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private static Map<String, Object> extractRequestData(Method method) {
        // Check for mapping annotations
        String url = null;
        String methodType = null;
        if (method.isAnnotationPresent(GetMapping.class)) {
            url = method.getAnnotation(GetMapping.class).value()[0];
            methodType = "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            url = method.getAnnotation(PostMapping.class).value()[0];
            methodType = "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            url = method.getAnnotation(PutMapping.class).value()[0];
            methodType = "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            url = method.getAnnotation(DeleteMapping.class).value()[0];
            methodType = "DELETE";
        }

        if (url == null) {
            return null; // Not a mapped method
        }

        // Build the request data in Postman format
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("name", method.getName());

        Map<String, Object> request = new HashMap<>();
        request.put("method", methodType);

        Map<String, Object> urlData = new HashMap<>();
        urlData.put("raw", "{{baseUrl}}" + url);
        urlData.put("host", Collections.singletonList("{{baseUrl}}"));
        urlData.put("path", Arrays.asList(url.split("/")));
        request.put("url", urlData);

        Map<String, Object> item = new HashMap<>();
        item.put("name", method.getName());
        item.put("request", request);
        return item;
    }
}
