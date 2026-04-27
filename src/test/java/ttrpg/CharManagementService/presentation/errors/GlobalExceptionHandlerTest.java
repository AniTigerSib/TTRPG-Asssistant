package ttrpg.CharManagementService.presentation.errors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import ttrpg.CharManagementService.domain.exception.ExternalExceptions.InvalidDataProvidedException;
import ttrpg.CharManagementService.domain.exception.InternalExceptions.InvalidArgumentException;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void mapsExternalExceptionToBadRequest() throws Exception {
        mockMvc.perform(get("/external"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mapsInternalExceptionToServerError() throws Exception {
        mockMvc.perform(get("/internal"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("Internal server error"))
            .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void mapsValidationErrorsToBadRequest() throws Exception {
        mockMvc.perform(post("/validate")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_FAILED"))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @RestController
    static class TestController {
        @GetMapping("/external")
        String external() {
            throw new InvalidDataProvidedException("bad request");
        }

        @GetMapping("/internal")
        String internal() {
            throw new InvalidArgumentException("broken");
        }

        @PostMapping(path = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
        String validate(@Valid @RequestBody TestRequest request) {
            return request.name();
        }
    }

    record TestRequest(@NotBlank String name) {}
}
