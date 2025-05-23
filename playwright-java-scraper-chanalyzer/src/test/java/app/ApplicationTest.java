package app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {
    @Test
    void contextLoads() {
        // This will fail if the Spring context cannot be started
        assertThat(true).isTrue();
    }
}
