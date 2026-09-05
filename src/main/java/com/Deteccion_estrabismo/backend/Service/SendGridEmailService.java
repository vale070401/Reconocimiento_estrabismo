package com.Deteccion_estrabismo.backend.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    // Usa el MISMO email que registraste en SendGrid
    private final String FROM_EMAIL = "deteccionestrabismo@gmail.com";
    private final String FROM_NAME = "Sistema Detección Estrabismo";

    public void sendRegistrationEmail(String toEmail, String userName) {
        String subject = "Bienvenido a Detección Estrabismo";
        String htmlContent = buildWelcomeEmail(userName);

        sendEmail(toEmail, subject, htmlContent);
    }

    void sendEmail(String to, String subject, String htmlContent) {
        // Usar el email verificado en SendGrid
        Email fromEmail = new Email(FROM_EMAIL, FROM_NAME);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("SendGrid Status: " + response.getStatusCode());

            if (response.getStatusCode() == 202) {
                System.out.println("✅ Email enviado exitosamente");
            } else {
                System.err.println("❌ Error SendGrid: " + response.getBody());
            }

        } catch (IOException ex) {
            System.err.println("❌ Error: " + ex.getMessage());
        }
    }

    private String buildWelcomeEmail(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 0; border-radius: 10px; }
                        .header { background: #007bff; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 30px; line-height: 1.6; color: #333; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Bienvenido a Detección Estrabismo! 👁️</h1>
                        </div>
                        <div class="content">
                            <h2>Hola %s,</h2>
                            <p>Tu registro se ha completado exitosamente.</p>
                            <p>Ahora puedes acceder a nuestra plataforma de detección temprana de estrabismo.</p>
                            <p><strong>URL de acceso:</strong> https://reconocimiento-estrabismo.onrender.com</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 Detección Estrabismo</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(userName);
    }

}
