package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.*;
import br.edu.ifc.bikes.jwt.JwtToken;
import br.edu.ifc.bikes.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioIT {
    @LocalServerPort
    int port;
    WebTestClient testClient;

    @BeforeEach
    void setUp() {
        testClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void requisitosClientesBicicletasLocacoesDevolucoesEJasperFuncionamPontaAPonta() {
        testClient.get().uri("/api/v1/bicicletas").exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.message").isEqualTo(
                        "Token ausente, inválido ou expirado. Faça login novamente e envie Authorization: Bearer <token>.");

        UsuarioResponseDTO admin = createUser("admin@ifc.edu.br", "135790", "ADMIN");
        String adminToken = authenticate("admin@ifc.edu.br", "135790");

        UsuarioResponseDTO user = createUser("cliente@ifc.edu.br", "123456", null);
        assertThat(user.role()).isEqualTo("CLIENTE");
        String clientToken = authenticate("cliente@ifc.edu.br", "123456");

        ClienteResponseDTO customer = testClient.post().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Cliente de Teste", "52998224725"))
                .exchange().expectStatus().isCreated()
                .expectBody(ClienteResponseDTO.class).returnResult().getResponseBody();
        assertThat(customer).isNotNull();
        assertThat(customer.usuarioId()).isEqualTo(user.id());
        testClient.post().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Admin não é cliente", "55544433322"))
                .exchange().expectStatus().isForbidden();
        testClient.post().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Outro cadastro", "11144477735"))
                .exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.message").isEqualTo("Este usuário já possui cadastro de cliente");

        testClient.get().uri("/api/v1/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(customer.id());
        testClient.get().uri("/api/v1/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, clientToken.replace("Bearer ", "Bearer Bearer "))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(customer.id());
        testClient.get().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().is4xxClientError();
        testClient.get().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].cpf").isEqualTo("52998224725");
        testClient.get().uri("/api/v1/clientes/{id}", customer.id())
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.usuarioId").isEqualTo(user.id());
        testClient.get().uri("/api/v1/usuarios")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
        testClient.get().uri("/api/v1/usuarios/{id}", user.id())
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(user.id());
        testClient.get().uri("/api/v1/usuarios")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().is4xxClientError();

        UsuarioResponseDTO otherUser = createUser("outro@ifc.edu.br", "987654", null);
        String otherClientToken = authenticate("outro@ifc.edu.br", "987654");
        testClient.post().uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, otherClientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Outro Cliente", "529.982.247-25"))
                .exchange().expectStatus().isEqualTo(409);

        BicicletaResponseDTO bike1 = createBike(adminToken, "BIKE-01");
        BicicletaResponseDTO bike2 = createBike(adminToken, "BIKE-02");
        assertThat(bike1.codigo()).isNotEqualTo(String.valueOf(bike1.id()));
        testClient.get().uri("/api/v1/bicicletas/BIKE-01")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("LIVRE");
        testClient.get().uri("/api/v1/bicicletas")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().is4xxClientError();

        LocacaoResponseDTO rental1 = rent(clientToken, "BIKE-01");
        assertThat(rental1.status()).isEqualTo("ATIVA");
        assertThat(rental1.cartaoFinal()).isEqualTo("1111");
        testClient.post().uri("/api/v1/locacoes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rentalRequest("BIKE-01"))
                .exchange().expectStatus().isEqualTo(409);

        LocacaoResponseDTO return1 = testClient.post().uri("/api/v1/locacoes/{id}/devolucao", rental1.id())
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange().expectStatus().isOk()
                .expectBody(LocacaoResponseDTO.class).returnResult().getResponseBody();
        assertThat(return1).isNotNull();
        assertThat(return1.status()).isEqualTo("DEVOLVIDA");
        assertThat(return1.devolucaoEfetiva()).isNotNull();
        assertThat(return1.valorDebitado()).isNotNull().isGreaterThanOrEqualTo(new java.math.BigDecimal("10.00"));
        testClient.get().uri("/api/v1/bicicletas/BIKE-01")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("LIVRE");

        LocacaoResponseDTO rental2 = rent(clientToken, "BIKE-02");
        testClient.post().uri("/api/v1/locacoes/{id}/devolucao", rental2.id())
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DevolucaoRequestDTO(true))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("DEVOLVIDA");
        testClient.get().uri("/api/v1/bicicletas/BIKE-02")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("MANUTENCAO");
        testClient.get().uri("/api/v1/locacoes")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
        testClient.get().uri("/api/v1/locacoes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().is4xxClientError();

        byte[] pdf = testClient.get().uri("/api/v1/relatorios/locacoes")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody().returnResult().getResponseBody();
        assertThat(pdf).isNotNull();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");

        testClient.get().uri("/api/v1/relatorios/locacoes")
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .exchange().expectStatus().is4xxClientError();

        testClient.patch().uri("/api/v1/usuarios/{id}", user.id())
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("654321", "654321", "123456"))
                .exchange().expectStatus().isOk();
        authenticate("cliente@ifc.edu.br", "654321");
        assertThat(admin.id()).isNotNull();
        assertThat(otherUser.id()).isNotNull();
    }

    private UsuarioResponseDTO createUser(String email, String password, String role) {
        UsuarioRequestDTO request = new UsuarioRequestDTO(email, password, role);
        return testClient.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(request)
                .exchange().expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class).returnResult().getResponseBody();
    }

    private String authenticate(String email, String password) {
        JwtToken jwtToken = testClient.post().uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioLoginDto(email, password))
                .exchange().expectStatus().isOk()
                .expectBody(JwtToken.class).returnResult().getResponseBody();
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getToken()).isNotBlank();
        return JwtUtils.JWT_BEARER + jwtToken.getToken();
    }

    private BicicletaResponseDTO createBike(String token, String code) {
        return testClient.post().uri("/api/v1/bicicletas")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BicicletaRequestDTO(code, null))
                .exchange().expectStatus().isCreated()
                .expectBody(BicicletaResponseDTO.class).returnResult().getResponseBody();
    }

    private LocacaoResponseDTO rent(String token, String bikeCode) {
        return testClient.post().uri("/api/v1/locacoes")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rentalRequest(bikeCode))
                .exchange().expectStatus().isCreated()
                .expectBody(LocacaoResponseDTO.class).returnResult().getResponseBody();
    }

    private LocacaoRequestDTO rentalRequest(String bikeCode) {
        return new LocacaoRequestDTO(bikeCode, LocalDateTime.now().plusDays(1),
                new CartaoDTO("Cliente Teste", "4111 1111 1111 1111", "12/30", "123"));
    }
}
