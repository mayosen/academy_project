package com.mayosen.academy.configs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Configuration
public class JacksonConfig {
    @Bean
    public Module module() {
        SimpleModule module = new SimpleModule("TimestampModule");
        module.addSerializer(Instant.class, new InstantSerializer());
        return module;
    }

    static class InstantSerializer extends StdSerializer<Instant> {
        private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendInstant(3).toFormatter();

        public InstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(formatter.format(value));
        }
    }
}
