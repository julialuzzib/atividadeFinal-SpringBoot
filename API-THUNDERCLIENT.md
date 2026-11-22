# testes

valor padrão R$ 10,00. simula débito em cartão.
http://localhost:8080

.\mvnw.cmd spring-boot:run


```http
POST /api/v1/usuarios
Content-Type: application/json

{
  "username": "admin.apresentacao@ifc.edu.br",
  "password": "135790",
  "role": "ADMIN"
}
```

```http
POST /api/v1/usuarios
Content-Type: application/json

{
  "username": "cliente.apresentacao@ifc.edu.br",
  "password": "123456"
}
```

O perfil omitido assume `CLIENTE`. 

**EXEMPLOS:**
| Perfil | E-mail | Senha | ID |

| Cliente | `cliente.teste.bikes@ifc.edu.br` | `123456` | 15 |
| Administrador | `admin.teste.bikes@ifc.edu.br` | `654321` | 16 |

```http
POST /api/v1/auth
Content-Type: application/json

{
  "username": "cliente.teste.bikes@ifc.edu.br",
  "password": "123456"
}
```
O login retorna `{"token":"<jwt>"}`. 
O JWT expira em 12 horas para facilitar a demonstração.


| Método | Endpoint | Perfil | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/usuarios` | Público | Cria usuário (e-mail único; senha de 6 caracteres; perfil opcional). |
| `POST` | `/api/v1/auth` | Público | Autentica e devolve JWT. |
| `GET` | `/api/v1/usuarios/{id}` | Administrador ou o próprio cliente | Localiza usuário pelo identificador. |
| `PATCH` | `/api/v1/usuarios/{id}` | Próprio usuário | Altera senha com senha atual, nova senha e confirmação. |
| `GET` | `/api/v1/usuarios` | Administrador | Lista usuários. |
| `POST` | `/api/v1/clientes` | Cliente | Registra nome completo e CPF para o usuário autenticado. |
| `GET` | `/api/v1/clientes/me` | Cliente | Recupera os próprios dados de cliente. |
| `GET` | `/api/v1/clientes/{id}` | Administrador | Localiza cliente pelo identificador. |
| `GET` | `/api/v1/clientes` | Administrador | Lista clientes. |
| `POST` | `/api/v1/bicicletas` | Administrador | Cadastra bicicleta com código único (não é o ID) e status inicial. |
| `GET` | `/api/v1/bicicletas` | Administrador | Lista bicicletas. |
| `GET` | `/api/v1/bicicletas/{codigo}` | Administrador | Localiza bicicleta pelo código. |
| `PATCH` | `/api/v1/bicicletas/{codigo}/status` | Administrador | Altera para `LIVRE` ou `MANUTENCAO`; `OCUPADA` é controlado pelas locações. |
| `POST` | `/api/v1/locacoes` | Cliente cadastrado | Solicita bicicleta disponível e informa devolução prevista e cartão para cobrança simulada. |
| `POST` | `/api/v1/locacoes/{id}/devolucao` | Cliente dono da locação ou administrador | Registra devolução, duração, débito simulado e status da bicicleta. |
| `GET` | `/api/v1/locacoes` | Administrador | Lista locações e devoluções. |
| `GET` | `/api/v1/relatorios/locacoes` | Administrador | Gera e baixa o relatório PDF usando JasperReports. |


Cadastro de cliente
Com um token atual do usuário `CLIENTE`, obtido em `POST /api/v1/auth`:

```http
POST {{baseUrl}}/api/v1/clientes
Authorization: {{tokenCliente}}
Content-Type: application/json

{
  "nomeCompleto": "Maria da Silva",
  "cpf": "52998224725"
}
```


Cadastro e busca de bicicletas
Com o token do administrador:

```http
POST {{baseUrl}}/api/v1/bicicletas
Authorization: {{tokenAdmin}}
Content-Type: application/json

{
  "codigo": "BIKE-005"
}
```

O status inicial padrão é `LIVRE`.
`{"codigo":"BIKE-002","status":"MANUTENCAO"}`. 
Não é permitido definir `OCUPADA` manualmente.



Solicitação de locação
Com o token do cliente cadastrado e uma bicicleta `LIVRE`:

```http
POST {{baseUrl}}/api/v1/locacoes
Authorization: {{tokenCliente}}
Content-Type: application/json

{
  "codigoBicicleta": "BIKE-001",
  "devolucaoPrevista": "2026-11-22T14:00:00",
  "cartao": {
    "nomeTitular": "Maria da Silva",
    "numero": "4111111111111111",
    "validade": "12/30",
    "cvv": "123"
  }
}
```


Devolução
usa o ID da resposta da locação. Para devolver e liberar automaticamente a bicicleta:

```http
POST {{baseUrl}}/api/v1/locacoes/1/devolucao
Authorization: {{tokenCliente}}
Content-Type: application/json

{
  "problemaManutencao": false
}
```


Relatório Jasper
Com o token de administrador:

```http
GET {{baseUrl}}/api/v1/relatorios/locacoes
Authorization: {{tokenAdmin}}
```


Alteração de senha
Com o token do usuário, use o ID do usuário:

```http
PATCH {{baseUrl}}/api/v1/usuarios/2
Authorization: {{tokenCliente}}
Content-Type: application/json

{
  "senhaAtual": "123456",
  "novaSenha": "654321",
  "confirmaSenha": "654321"
}
```

{
  "username": "admin.teste@ifc.edu.br",
  "password": "123456",
  "role": "ADMIN"
}
