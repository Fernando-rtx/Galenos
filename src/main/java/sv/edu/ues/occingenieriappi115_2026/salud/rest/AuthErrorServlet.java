package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Traduce rechazos del contenedor antes de JAX-RS sin perder CORS ni el estado. */
@WebServlet("/rest-auth-error")
public class AuthErrorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Inject private ApiSettings settings;
    @Override protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        int code = status instanceof Integer value && value == 403 ? 403 : 401;
        String origin = request.getHeader("Origin");
        if (origin != null && settings.allowsOrigin(origin)) response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Vary", "Origin");
        response.setHeader("Cache-Control", "no-store");
        if (code == 401) response.setHeader("WWW-Authenticate", "Basic realm=\"GalenosDemo\"");
        response.setStatus(code);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(code == 401 ? "{\"mensaje\":\"Autenticación requerida\"}"
                : "{\"mensaje\":\"No tiene permiso para esta operación\"}");
    }
}
