package com.shubchynskyi.tictactoeapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Slf4j
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
            log.debug("Retrieved locale from session: {}", locale);
        }

        if (locale == null) {
            locale = acceptHeaderLocaleResolver.resolveLocale(request);
            log.debug("No session locale found, using Accept-Language header: {}", locale);
        }

        log.info("Resolved locale: {}", locale);
        return locale;
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
        log.info("Setting locale to: {}", locale);
        sessionLocaleResolver.setLocale(request, response, locale);
    }
}