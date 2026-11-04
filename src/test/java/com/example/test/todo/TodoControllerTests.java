package com.example.test.todo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.test.security.User;
import com.example.test.security.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:todo-tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "server.servlet.context-path=/todo",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@AutoConfigureMockMvc
@Transactional
class TodoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ToolCallbackProvider todoToolCallbackProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void registerTestUser() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-user","password":"a-strong-test-password"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void createsUpdatesCompletesAndDeletesTodo() throws Exception {
        String created = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  Buy milk  ","description":"  Oat milk  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.description").value("Oat milk"))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(put("/api/todos/{id}", id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Buy groceries","description":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.description").doesNotExist());

        mockMvc.perform(patch("/api/todos/{id}", id)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(get("/api/todos/{id}", id).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy groceries"));

        mockMvc.perform(delete("/api/todos/{id}", id).header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos/{id}", id).header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyShowsAndAllowsAccessToTheAuthenticatedUsersTodos() throws Exception {
        String created = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Private task","description":"Owned by first user"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long todoId = objectMapper.readTree(created).get("id").asLong();

        String secondTokenResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"second-user","password":"another-strong-password"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String secondToken = objectMapper.readTree(secondTokenResponse).get("token").asText();

        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Second user's task","description":""}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/todos").header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Second user's task"));
        mockMvc.perform(get("/api/todos/{id}", todoId).header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completed":true}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Stolen task","description":""}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/todos/{id}", todoId).header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/todos/{id}", todoId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Private task"));
    }

    @Test
    void rejectsBlankTodoTitle() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   ","description":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectsTodoEndpointsAndRejectsInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/todos").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void showsTheAuthenticatedUsernameAndProtectsCurrentUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test-user"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registersAndAuthenticatesWithoutStoringPlaintextPassword() throws Exception {
        User account = userRepository.findByUsername("test-user").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(account.getPasswordHash())
                .isNotEqualTo("a-strong-test-password");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(
                "a-strong-test-password", account.getPasswordHash())).isTrue();
        org.assertj.core.api.Assertions.assertThat(account.getCreatedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(account.getLastLoginAt()).isNull();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"TEST-USER","password":"a-strong-test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        org.assertj.core.api.Assertions.assertThat(account.getLastLoginAt()).isNotNull();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registersOptionalContactDetails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"contact-user",
                                  "password":"a-strong-test-password",
                                  "email":"  contact@example.com ",
                                  "mobileNumber":" +12345678901 "
                                }
                                """))
                .andExpect(status().isCreated());

        User account = userRepository.findByUsername("contact-user").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(account.getEmail()).isEqualTo("contact@example.com");
        org.assertj.core.api.Assertions.assertThat(account.getMobileNumber()).isEqualTo("+12345678901");
        org.assertj.core.api.Assertions.assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void servesTodoPageAtHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/todos.html"));

        mockMvc.perform(get("/todos.html"))
                .andExpect(status().isOk());
    }

    @Test
    void exposesEnvironmentNameForThePage() throws Exception {
        mockMvc.perform(get("/api/environment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").isNotEmpty());
    }

    @Test
    void registersTodoMcpTools() {
        org.assertj.core.api.Assertions.assertThat(todoToolCallbackProvider.getToolCallbacks())
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder(
                        "listTodos",
                        "getTodo",
                        "createTodo",
                        "updateTodo",
                        "setTodoCompletion",
                        "deleteTodo");
    }

    @Test
    void mcpToolsOperateOnTodoService() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test-user", null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        ToolCallback[] callbacks = todoToolCallbackProvider.getToolCallbacks();
        String created = tool(callbacks, "createTodo")
                .call("""
                        {"title":"MCP task","description":"Created through MCP"}
                        """);
        long id = objectMapper.readTree(created).get("id").asLong();

        String completed = tool(callbacks, "setTodoCompletion")
                .call("{\"id\":" + id + ",\"completed\":true}");
        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(completed).get("completed").asBoolean())
                .isTrue();

        String updated = tool(callbacks, "updateTodo")
                .call("{\"id\":" + id + ",\"title\":\"Updated MCP task\",\"description\":\"Updated note\"}");
        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(updated).get("title").asText())
                .isEqualTo("Updated MCP task");

        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(tool(callbacks, "listTodos").call("{}"))
                        .findValue("id").asLong())
                .isEqualTo(id);

        String fetched = tool(callbacks, "getTodo").call("{\"id\":" + id + "}");
        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(fetched).get("title").asText())
                .isEqualTo("Updated MCP task");

        tool(callbacks, "deleteTodo").call("{\"id\":" + id + "}");
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> tool(callbacks, "getTodo").call("{\"id\":" + id + "}"))
                .hasMessageContaining("Todo not found");
    }

    @Test
    void exposesMcpStreamableHttpEndpoint() throws Exception {
        mockMvc.perform(post("/mcp")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .content("""
                                {
                                  "jsonrpc": "2.0",
                                  "id": 1,
                                  "method": "initialize",
                                  "params": {
                                    "protocolVersion": "2024-11-05",
                                    "capabilities": {},
                                    "clientInfo": {"name": "todo-app-test", "version": "1.0.0"}
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.protocolVersion").exists());
    }

    private ToolCallback tool(ToolCallback[] callbacks, String name) {
        return Arrays.stream(callbacks)
                .filter(callback -> callback.getToolDefinition().name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("MCP tool not registered: " + name));
    }

    private String bearerToken() {
        return "Bearer " + authToken;
    }
}
