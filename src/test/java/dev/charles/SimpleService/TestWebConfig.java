package dev.charles.SimpleService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@TestConfiguration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class TestWebConfig {
}
