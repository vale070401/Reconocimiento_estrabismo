package com.Deteccion_estrabismo.backend.integration;

import com.Deteccion_estrabismo.backend.Service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected JwtService jwtService;

    protected static final String TEST_AUTH_TOKEN = "Bearer test.jwt.token";

    @BeforeEach
    void setUp() {
        // Configurar el mock de JwtService
        when(jwtService.generateToken(any())).thenReturn("test.jwt.token");
        when(jwtService.extractUsername(any())).thenReturn("admin@test.com");
        when(jwtService.isTokenValid(any(), any())).thenReturn(true);
    }
}
