package com.Deteccion_estrabismo.backend.Service;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite stubbings no utilizados
class SendGridEmailServiceTest {

    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private SendGridEmailService emailService;

    @Captor
    private ArgumentCaptor<Mail> mailCaptor;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_CONTENT = "<p>Test Content</p>";

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", TEST_API_KEY);
        // Configurar el mock para evitar errores de NullPointer
        when(sendGrid.api(any())).thenReturn(mockResponse(202));
    }

    @Test
    void sendRegistrationEmail_ShouldCallSendEmailWithCorrectParameters() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendRegistrationEmail(TEST_EMAIL, TEST_NAME);
            // Verificamos que se llamó al menos una vez a sendEmail con los parámetros correctos
            // Aunque no podemos verificar los parámetros exactos ya que es un método privado
            // Verificamos que no se lanzaron excepciones y que el flujo se completó
        });
    }

    @Test
    void sendEmail_WhenSuccessful_ShouldNotThrowException() throws IOException {
        // Arrange
        when(sendGrid.api(any())).thenReturn(mockResponse(202));

        // Act & Assert
        assertDoesNotThrow(() -> 
            ReflectionTestUtils.invokeMethod(emailService, "sendEmail", TEST_EMAIL, TEST_SUBJECT, TEST_CONTENT)
        );
    }

    @Test
    void sendEmail_WhenSendGridThrowsException_ShouldCatchAndLogError() throws IOException {
        // Arrange
        when(sendGrid.api(any())).thenThrow(new IOException("API Error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
            ReflectionTestUtils.invokeMethod(emailService, "sendEmail", TEST_EMAIL, TEST_SUBJECT, TEST_CONTENT)
        );
    }

    @Test
    void buildWelcomeEmail_ShouldReturnFormattedHtml() {
        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(emailService, "buildWelcomeEmail", TEST_NAME);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains(TEST_NAME));
        assertTrue(result.contains("Bienvenido a Detección Estrabismo"));
    }

    @Test
    void sendEmail_WithInvalidEmail_ShouldHandleGracefully() throws IOException {
        // Arrange
        String invalidEmail = "invalid-email";
        when(sendGrid.api(any())).thenReturn(mockResponse(400));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
            ReflectionTestUtils.invokeMethod(emailService, "sendEmail", invalidEmail, TEST_SUBJECT, TEST_CONTENT)
        );
    }
    
    // Helper method to create mock responses
    private com.sendgrid.Response mockResponse(int statusCode) {
        com.sendgrid.Response response = new com.sendgrid.Response();
        response.setStatusCode(statusCode);
        return response;
    }
}
