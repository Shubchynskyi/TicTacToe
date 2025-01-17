package com.shubchynskyi.tictactoeapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@RequiredArgsConstructor
public class AppLocaleResolver implements LocaleResolver {
    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;
    private final SessionLocaleResolver sessionLocaleResolver;

    @Override
    @NonNull
    public Locale resolveLocale(@NonNull HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Locale locale = null;
        if (session != null) {
            locale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        }

        if (locale == null) {
            locale = acceptHeaderLocaleResolver.resolveLocale(request);
        }

        return locale;
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
        sessionLocaleResolver.setLocale(request, response, locale);
    }
}