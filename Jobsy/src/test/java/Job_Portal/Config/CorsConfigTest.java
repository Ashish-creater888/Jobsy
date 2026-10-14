package Job_Portal.Config;

import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorsConfigTest {

    @Test
    void allowsViteOnAnAutomaticallySelectedLoopbackPort() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/api/admin/jobs/Create");
        request.addHeader("Origin", "http://localhost:5175");
        String allowedOrigin = new CorsConfig()
                .corsConfigurationSource()
                .getCorsConfiguration(request)
                .checkOrigin(request.getHeader("Origin"));

        assertEquals("http://localhost:5175", allowedOrigin);
    }

    @Test
    void allowsViteOnThePrivateLanAddress() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/api/admin/jobs/Create");
        request.addHeader("Origin", "http://192.168.0.174:5174");
        String allowedOrigin = new CorsConfig()
                .corsConfigurationSource()
                .getCorsConfiguration(request)
                .checkOrigin(request.getHeader("Origin"));

        assertEquals("http://192.168.0.174:5174", allowedOrigin);
    }

    @Test
    void rejectsOriginsOutsideAllowedDevelopmentHosts() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/api/admin/jobs/Create");
        request.addHeader("Origin", "https://example.com");
        String allowedOrigin = new CorsConfig()
                .corsConfigurationSource()
                .getCorsConfiguration(request)
                .checkOrigin(request.getHeader("Origin"));

        assertNull(allowedOrigin);
    }
}
