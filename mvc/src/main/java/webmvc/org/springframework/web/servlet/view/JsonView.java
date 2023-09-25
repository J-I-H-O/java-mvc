package webmvc.org.springframework.web.servlet.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.org.springframework.http.MediaType;
import webmvc.org.springframework.web.servlet.View;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonView implements View {
    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, HttpServletResponse response) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(model);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        if (model.size() == 1) {
            final List<?> objects = new ArrayList<>(model.values());
            writeResponse(response, objects.get(0));
            return;
        }
        writeResponse(response, jsonResult);
    }

    private void writeResponse(final HttpServletResponse response, final Object object) throws IOException {
        try (final PrintWriter writer = response.getWriter()) {
            writer.println(object);
        }
    }
}
