# Chuk's Car Services – Sistema de Atendimento para Oficina

Aplicação web para organizar o dia a dia de uma oficina mecânica: do primeiro
contato do cliente até o registro de entrada do veículo e o orçamento.
Funciona no navegador do computador, tablet e celular (Android e iOS).

## Funcionalidades

**Atendimento do cliente**
- Formulário de atendimento (nome, e-mail, carro, marca, ano, reclamação,
  data de agendamento e forma de pagamento).

**Checklist de entrada do veículo (uso do mecânico)**
- Dados do cliente e do veículo carregados a partir do atendimento
  (e-mail e nome continuam editáveis para correção).
- Nível de gasolina, KM do odômetro e condição do veículo
  (em funcionamento **ou** veio de guincho).
- Inspeção visual com 28 itens, divididos em Internos, Externos,
  Compartimentos do motor e Inferiores, baseada na norma ABNT NBR 14624.
  Cada item é marcado como conforme (👍) ou com problema (👎) e aceita
  observação. Há também uma linha "Outros" por seção.
- Fotos do veículo (mínimo de 10 e máximo de 11), com câmera ou galeria,
  pré-visualização e remoção individual.
- Envio automático de uma **cópia somente leitura por e-mail** ao cliente, com
  os dados, o resultado da inspeção e as fotos reduzidas.

**Orçamento**
- Cadastro separado do cliente, com chassi, marca, modelo, ano, cor, placa,
  prisma, KM, datas, reclamações, diagnóstico, garantia, serviço a ser feito,
  responsável técnico e problemas enfrentados.
- Validação do chassi (VIN): exatamente 17 caracteres, letras e números,
  sem as letras I, O e Q.

## Tecnologias

- Java 21
- Spring Boot 3.3.4 (Web, Mail)
- Firebase Admin SDK (Cloud Firestore)
- Thumbnailator (redução das fotos enviadas por e-mail)
- HTML, CSS e JavaScript puros (sem framework), com layout responsivo

## Estrutura

```
src/main/java/com/oficina/oficinaapp
├── Atendimento / AtendimentoController
├── Checklist / ItemInspecao / ChecklistController
├── ChecklistEmailService
├── Orcamento / OrcamentoController
├── FirebaseConfig
└── WebConfig
src/main/resources
├── static/            (telas: index, checklist, orcamento, css e imagens)
└── application.properties
```

## API

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/atendimentos` | Cria um atendimento |
| GET | `/api/checklists/cliente/{atendimentoId}` | Dados do cliente para o checklist |
| POST | `/api/checklists` | Salva o checklist com fotos e envia o e-mail |
| GET | `/api/checklists/{id}` | Busca um checklist |
| POST | `/api/checklists/{id}/reenviar-email` | Reenvia o e-mail do checklist |
| POST | `/api/orcamentos` | Cria um orçamento |
| GET | `/api/orcamentos` | Lista os orçamentos |
| GET | `/api/orcamentos/{id}` | Busca um orçamento |
| PUT | `/api/orcamentos/{id}` | Atualiza um orçamento |

## Como executar

**Pré-requisitos:** JDK 21, Maven e um projeto no Firebase com o Firestore ativado.

1. Gere a chave de serviço no console do Firebase e salve como
   `src/main/resources/firebase-key.json`.
   **Nunca envie esse arquivo ao Git.**
2. Crie uma senha de app na conta Gmail que enviará os e-mails
   (exige verificação em duas etapas).
3. Defina as variáveis de ambiente:
```
   MAIL_USER=email.da.oficina@gmail.com
   MAIL_PASSWORD=senha-de-app-sem-espacos
```
4. Execute:
```
   mvn spring-boot:run
```
5. Acesse:
   - Atendimento: `http://localhost:8080/index.html`
   - Checklist: `http://localhost:8080/checklist.html?atendimentoId=ID_DO_ATENDIMENTO`
   - Orçamento: `http://localhost:8080/orcamento.html`

## Observações

- As fotos do checklist são salvas na pasta `uploads/`. Em hospedagens com
  disco temporário, é preciso migrar para um armazenamento externo
  (por exemplo, Firebase Storage).
- Antes de ir para produção, é recomendável adicionar autenticação de usuários.

## Status

Projeto em desenvolvimento, criado para estudo e prática de Java e Spring Boot.
