package com.oficina.oficinaapp.config;
import com.oficina.oficinaapp.controller.ClienteController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Trava o Atendimento: só passa quem validou o token nesta sessão.
 * Vale para a página (atendimento.html) e para a API (/api/atendimentos),
 * então não adianta digitar o endereço direto.
 */
@Configuration
public class AtendimentoWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
                    throws IOException {
                HttpSession sessao = req.getSession(false);
                boolean liberado = sessao != null && sessao.getAttribute(ClienteController.SESSAO_CLIENTE) != null;
                if (liberado) return true;

                if (req.getRequestURI().startsWith("/api/")) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"mensagem\":\"Cadastro não validado.\"}");
                } else {
                    res.sendRedirect("/cadastro.html");
                }
                return false;
            }
        }).addPathPatterns("/atendimento.html", "/api/atendimentos", "/api/atendimentos/**");
    }
}
