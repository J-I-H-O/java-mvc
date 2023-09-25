package com.techcourse;

import com.techcourse.controller.regacy.ForwardController;
import com.techcourse.controller.regacy.LoginController;
import com.techcourse.controller.regacy.LoginViewController;
import com.techcourse.controller.regacy.LogoutController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webmvc.org.springframework.web.servlet.mvc.asis.Controller;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerMapping;

public class ManualHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(ManualHandlerMapping.class);

    private static final Map<String, Controller> controllers = new HashMap<>();

    @Override
    public void initialize() {
        controllers.put("/", new ForwardController("/index.jsp"));
        controllers.put("/login", new LoginController());
        controllers.put("/login/view", new LoginViewController());
        controllers.put("/logout", new LogoutController());

        log.info("Initialized Handler Mapping!");
        controllers.keySet()
            .forEach(path -> log.info("Path : {}, Controller : {}", path, controllers.get(path).getClass()));
    }

    @Override
    public Optional<Object> getHandler(HttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();
        log.debug("Request Mapping Uri : {}", requestURI);
        return Optional.ofNullable(controllers.get(requestURI));
    }
}
