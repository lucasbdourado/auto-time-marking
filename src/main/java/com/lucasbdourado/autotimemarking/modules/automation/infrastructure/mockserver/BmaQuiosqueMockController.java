package com.lucasbdourado.autotimemarking.modules.automation.infrastructure.mockserver;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping(value = {"/wpe/quiosque", "/wpe/Quiosque"})
public class BmaQuiosqueMockController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BmaQuiosqueMockController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String REAL_HOST_BASE = "http://joinville.neomind.com.br:8070";

    private final RestTemplate restTemplate = new RestTemplate();
    private final List<String> todayMarkings = new CopyOnWriteArrayList<>();
    private final MockCredentialsLoader credentialsLoader;

    public BmaQuiosqueMockController(MockCredentialsLoader credentialsLoader) {
        this.credentialsLoader = credentialsLoader;
        todayMarkings.add("09:00");
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String getLoginPage() {
        LOGGER.info("Mock BMA Quiosque: Serving 1:1 exact mirror login page");
        return renderLoginPage(null);
    }

    private String renderLoginPage(String errorMessage) {
        String alertHtml = (errorMessage != null)
                ? "<div class=\"alert alert-danger\" id=\"retorno\">" + errorMessage + "</div>"
                : "";
        return """
                <!DOCTYPE html>
                <html lang="pt-br">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
                    <meta name="author" content="BMA Sistemas LTDA">
                    <link rel="shortcut icon" type="image/ico" href="https://www.bmasistemas.com.br/icons/wpe-57.png">
                    <link href="/wpe/Quiosque/Content/vendor/css?v=bcrFUkw51Aa7qm-JfGxaw4m7fofixcOjRFIPjhdvSe81" rel="stylesheet">
                    <link href="/wpe/Quiosque/Content/css?v=oJEvxLXJ9gYeQw32AMx-z0W6gpRxvthjd-1xVG785Bo1" rel="stylesheet">
                    <title>WPE - Acessar</title>
                    <script src="/wpe/Quiosque/bundles/modernizr?v="></script>
                </head>
                <body>
                    <div class="navbar navbar-default navbar-fixed-top">
                        <div class="container">
                            <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </button>
                                <a href="/wpe/quiosque/" class="navbar-brand" style="padding-top:10px;">
                                    <img class="logo" height="45" alt="BMA Quiosque" src="/wpe/Quiosque/Content/images/logo-2.svg">
                                </a>
                            </div>
                            <div class="navbar-collapse collapse"></div>
                        </div>
                    </div>
                    <div class="container body-content">
                        <h2 class="page-header">Acessar Quiosque</h2>
                        """ + alertHtml + """
                        <form action="/wpe/quiosque/login" method="post" novalidate="novalidate">
                            <div class="form-horizontal">
                                <hr>
                                <div class="form-group">
                                    <label class="control-label col-md-2" for="Usuario">Matrícula</label>
                                    <div class="col-md-10">
                                        <input autocomplete="off" class="form-control text-box single-line" id="Usuario" name="Usuario" type="number" required>
                                        <span class="field-validation-valid text-danger" data-valmsg-for="Usuario" data-valmsg-replace="true"></span>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-2" for="Senha">Senha</label>
                                    <div class="col-md-10">
                                        <input class="form-control text-box single-line password" id="Senha" name="Senha" type="password" required>
                                        <span class="field-validation-valid text-danger" data-valmsg-for="Senha" data-valmsg-replace="true"></span>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-offset-2 col-md-10">
                                        <input type="submit" value="Acessar" class="btn btn-info btn-block">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-offset-2 col-md-2">
                                        <a href="/wpe/quiosque/conta/recuperar">Esqueceu a sua senha?</a>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-offset-2 col-md-2">
                                        <a href="/wpe/quiosque/conta/novousuario">Novo usuário?</a>
                                    </div>
                                </div>
                            </div>
                        </form>
                        <hr>
                        <footer>
                            <p>Todos os direitos reservados © <a href="https://bmasistemas.com.br/home"> BMA Sistemas</a> - 2026</p>
                        </footer>
                    </div>
                    <script src="/wpe/Quiosque/bundles/vendor?v=QUGOWdlm6RtCQJsMEWn2NkVkcS3wgK77GrpjxCXucdE1"></script>
                    <script src="/wpe/Quiosque/bundles/bma-utils?v=TC7KirL4l-iFQ19XCXOlCckbu9mAkuLSLnpv0F9zE281"></script>
                </body>
                </html>
                """;
    }

    @PostMapping(value = {"", "/", "/login", "/login/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String handleLogin(
            @RequestParam(value = "Usuario", required = false) String username,
            @RequestParam(value = "Senha", required = false) String password
    ) {
        if (!credentialsLoader.isValidUser(username, password)) {
            LOGGER.warn("Mock BMA Quiosque: Invalid authentication attempt for user '{}'", username);
            return renderLoginPage("Matrícula ou Senha inválidas.");
        }
        LOGGER.info("Mock BMA Quiosque: User '{}' authenticated. Rendering Cartão Ponto page", username);
        return renderMarkingsPage();
    }

    @GetMapping(value = {"/cartaoponto/marcacao", "/cartaoponto/marcacao/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String getMarkingsPage() {
        return renderMarkingsPage();
    }

    @GetMapping(value = {"/marcacao/registrar", "/marcacao/registrar/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String getRegistrarPage() {
        return renderRegistrarPage();
    }

    @PostMapping(value = {"/marcacao/registrar", "/marcacao/registrar/"})
    public ResponseEntity<?> handlePunch(
            HttpServletRequest request,
            @RequestParam(value = "Senha", required = false) String password
    ) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        String xRequestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(xRequestedWith) || (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));

        if (password != null && !credentialsLoader.isValidPassword(password)) {
            LOGGER.warn("Mock BMA Quiosque: Invalid punch password provided");
            if (isAjax) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Senha incorreta!");
                return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(errorResponse);
            }
            return ResponseEntity.status(400).contentType(MediaType.TEXT_HTML).body(renderRegistrarPage());
        }

        String newPunch = LocalTime.now().format(TIME_FORMATTER);
        todayMarkings.add(newPunch);
        LOGGER.info("Mock BMA Quiosque: Registered punch at {}", newPunch);

        // If AJAX request or JSON requested, return production JSON response
        if (isAjax) {
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("status", "success");
            jsonResponse.put("message", "Marcação efetuada com sucesso!");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
        }

        // Fallback for direct form POST (HTML response)
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(renderRegistrarPage());
    }

    @GetMapping(value = {"/marcacao/listarmarcacoesdodia", "/marcacao/listarmarcacoesdodia/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listDailyMarkings() {
        String todayDateStr = LocalDate.now().format(DATE_FORMATTER);
        List<Map<String, Object>> marcacoes = new ArrayList<>();
        int seq = 1;
        for (String t : todayMarkings) {
            Map<String, Object> item = new HashMap<>();
            item.put("Id", String.valueOf(seq));
            item.put("Sequencia", seq);
            item.put("Data", todayDateStr);
            item.put("Hora", t);
            item.put("Status", "S");
            marcacoes.add(item);
            seq++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("marcacoes", marcacoes);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = {"/notificacao/**", "/notificacao"}, method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> handleNotificationsFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("isValid", true);
        response.put("count", 0);
        response.put("lista", List.of());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = {"/reset", "/reset/"})
    public void resetMarkings() {
        todayMarkings.clear();
        LOGGER.info("Mock BMA Quiosque: Reset daily markings");
    }

    // Proxy endpoints for static CSS, JS, fonts, and images from real BMA Quiosque server
    @GetMapping(value = {"/Content/**", "/bundles/**", "/images/**", "/fonts/**", "/wpe/Quiosque/Content/**", "/wpe/Quiosque/bundles/**"})
    public ResponseEntity<byte[]> proxyStaticAsset(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/wpe/quiosque/Content")) {
            uri = uri.replace("/wpe/quiosque/Content", "/wpe/Quiosque/Content");
        } else if (uri.startsWith("/wpe/quiosque/bundles")) {
            uri = uri.replace("/wpe/quiosque/bundles", "/wpe/Quiosque/bundles");
        }
        String targetUrl = REAL_HOST_BASE + uri;
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, byte[].class);
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(response.getHeaders().getContentType() != null ? response.getHeaders().getContentType() : MediaType.APPLICATION_OCTET_STREAM)
                    .body(response.getBody());
        } catch (Exception e) {
            LOGGER.warn("Could not proxy static asset from real server ({}), returning 404 fallback", targetUrl);
            return ResponseEntity.notFound().build();
        }
    }

    private String renderMarkingsPage() {
        StringBuilder markingsHtml = new StringBuilder();
        markingsHtml.append("<div id=\"Registros\">\n");
        for (String t : todayMarkings) {
            markingsHtml.append("  <span class=\"marking-time-item\">").append(t).append("</span> \n");
        }
        markingsHtml.append("</div>\n");

        String todayDate = LocalDate.now().format(DATE_FORMATTER);

        return """
                <!DOCTYPE html>
                <html lang="pt-br">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
                    <meta name="author" content="BMA Sistemas LTDA">
                    <link rel="shortcut icon" type="image/ico" href="https://www.bmasistemas.com.br/icons/wpe-57.png">
                    <link href="/wpe/Quiosque/Content/vendor/css?v=bcrFUkw51Aa7qm-JfGxaw4m7fofixcOjRFIPjhdvSe81" rel="stylesheet">
                    <link href="/wpe/Quiosque/Content/css?v=oJEvxLXJ9gYeQw32AMx-z0W6gpRxvthjd-1xVG785Bo1" rel="stylesheet">
                    <title>WPE - Marcações</title>
                    <script src="/wpe/Quiosque/bundles/modernizr?v="></script>
                </head>
                <body>
                    <div class="navbar navbar-default navbar-fixed-top">
                        <div class="container">
                            <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </button>
                                <a href="/wpe/quiosque/" class="navbar-brand" style="padding-top:10px;">
                                    <img class="logo" height="45" alt="BMA Quiosque" src="/wpe/Quiosque/Content/images/logo-2.svg">
                                </a>
                            </div>
                            <div class="navbar-collapse collapse">
                                <form action="/wpe/quiosque/conta/sair" class="navbar-right" id="logoutForm" method="post">
                                    <ul class="nav navbar-nav navbar-right">
                                        <li><a href="/wpe/quiosque/conta/perfil" title="Visualizar perfil">Olá, Lucas!</a></li>
                                        <li class="dropdown">
                                            <a href="javascript:void(0)" id="notifications" data-toggle="dropdown">
                                                <i id="bell" class="fa fa-bell"></i>
                                                <b class="caret"></b>
                                            </a>
                                        </li>
                                        <li><a href="/wpe/quiosque/marcacao/registrar" title="Registrar marcação do ponto através do quiosque">Registrar Marcação</a></li>
                                        <li><a href="/wpe/quiosque/home/sobre">Sobre</a></li>
                                        <li><a href="/wpe/quiosque/">Sair</a></li>
                                    </ul>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div class="container body-content" style="margin-top:70px;">
                        <h2 class="page-header"><i class="fa fa-calendar"></i> <strong>Marcações</strong></h2>
                        <hr>
                        <div class="well">
                            <div class="btn-group">
                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown">
                                    Cartão Ponto <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu">
                                    <li><a href="/wpe/quiosque/cartaoponto/marcacao">Marcações</a></li>
                                    <li><a href="/wpe/quiosque/cartaoponto/bancodehoras">Banco de Horas</a></li>
                                    <li><a href="/wpe/quiosque/cartaoponto/totais">Totais</a></li>
                                </ul>
                            </div>
                            <a class="btn btn-warning" href="/wpe/quiosque/justificativa/lista">Justificativas</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover table-striped">
                                <thead>
                                    <tr>
                                        <th class="col-sm-2">Data</th>
                                        <th class="col-sm-3">Horários</th>
                                        <th class="col-sm-3">Marcações</th>
                                        <th class="col-sm-2">Status</th>
                                        <th class="col-sm-2"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>""" + todayDate + """
                                         - hoje</td>
                                        <td>7:45 12:00 13:30 18:00</td>
                                        <td>""" + markingsHtml + """
                                        </td>
                                        <td><span class="label label-default">Normal</span></td>
                                        <td></td>
                                    </tr>
                                    <tr>
                                        <td>25/07/2026 - sáb</td>
                                        <td>Sábado</td>
                                        <td>Não processado</td>
                                        <td><span class="label label-default">Normal</span></td>
                                        <td></td>
                                    </tr>
                                    <tr>
                                        <td>24/07/2026 - sex</td>
                                        <td>7:45 12:00 13:30 18:00</td>
                                        <td><span class="marking-time-item">09:00</span> <span class="marking-time-item">12:00</span> <span class="marking-time-item">13:00</span> <span class="marking-time-item">18:45</span></td>
                                        <td><span class="label label-default">Normal</span></td>
                                        <td></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <hr>
                        <footer>
                            <p class="text-center hidden-print">Todos os direitos reservados © <a href="https://bmasistemas.com.br/home"> BMA Sistemas</a> - 2026</p>
                        </footer>
                    </div>
                    <script src="/wpe/Quiosque/bundles/vendor?v=QUGOWdlm6RtCQJsMEWn2NkVkcS3wgK77GrpjxCXucdE1"></script>
                    <script src="/wpe/Quiosque/bundles/bma-utils?v=TC7KirL4l-iFQ19XCXOlCckbu9mAkuLSLnpv0F9zE281"></script>
                </body>
                </html>
                """;
    }

    private String renderRegistrarPage() {
        String todayDateStr = LocalDate.now().format(DATE_FORMATTER);

        StringBuilder pendingRowsHtml = new StringBuilder();
        int index = 1;
        for (String t : todayMarkings) {
            pendingRowsHtml.append("<tr>")
                    .append("<td>").append(index++).append("</td>")
                    .append("<td>").append(todayDateStr).append("</td>")
                    .append("<td>").append(t).append("</td>")
                    .append("<td><span class=\"label label-success\">Aprovada</span></td>")
                    .append("</tr>\n");
        }

        return """
                <!DOCTYPE html>
                <html lang="pt-br">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
                    <meta name="author" content="BMA Sistemas LTDA">
                    <link rel="shortcut icon" type="image/ico" href="https://www.bmasistemas.com.br/icons/wpe-57.png">
                    <link href="/wpe/Quiosque/Content/vendor/css?v=bcrFUkw51Aa7qm-JfGxaw4m7fofixcOjRFIPjhdvSe81" rel="stylesheet">
                    <link href="/wpe/Quiosque/Content/css?v=oJEvxLXJ9gYeQw32AMx-z0W6gpRxvthjd-1xVG785Bo1" rel="stylesheet">
                    <title>WPE - Registrar Marcação</title>
                    <script src="/wpe/Quiosque/bundles/modernizr?v="></script>
                </head>
                <body>
                    <div class="navbar navbar-default navbar-fixed-top">
                        <div class="container">
                            <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </button>
                                <a href="/wpe/quiosque/" class="navbar-brand" style="padding-top:10px;">
                                    <img class="logo" height="45" alt="BMA Quiosque" src="/wpe/Quiosque/Content/images/logo-2.svg">
                                </a>
                            </div>
                            <div class="navbar-collapse collapse">
                                <form action="/wpe/quiosque/conta/sair" class="navbar-right" id="logoutForm" method="post">
                                    <ul class="nav navbar-nav navbar-right">
                                        <li><a href="/wpe/quiosque/conta/perfil" title="Visualizar perfil">Olá, Lucas!</a></li>
                                        <li class="dropdown">
                                            <a href="javascript:void(0)" id="notifications" onclick="loadNotifications(0);" data-toggle="dropdown" data-request-url="/wpe/quiosque/notificacao/retornanotificacoes">
                                                <i id="bell" class="fa fa-bell"></i>
                                                <span id="sinalizador" class="circle bg-danger fw-bold hidden"><span id="nt_total" class="nt-font-size" data-request-url="/wpe/quiosque/notificacao/totalnotificacoes"></span></span>
                                                <b class="caret"></b>
                                            </a>
                                        </li>
                                        <li><a href="/wpe/quiosque/cartaoponto/marcacao">Marcações</a></li>
                                        <li><a href="/wpe/quiosque/home/sobre">Sobre</a></li>
                                        <li><a href="/wpe/quiosque/">Sair</a></li>
                                    </ul>
                                </form>
                                <div id="nt_notificacao" data-request-url="/wpe/quiosque/notificacao/visualizarnotificacao"></div>
                                <div id="nt_notificacaoStatus" data-request-url="/wpe/quiosque/notificacao/alterarstatusnotificacao"></div>
                            </div>
                        </div>
                    </div>
                    <div class="container body-content" style="margin-top:70px;">
                        <h2 class="page-header"><strong>Registrar Marcação</strong></h2>
                        <hr>
                        <div class="well">
                            <a class="btn btn-success" href="/wpe/quiosque/cartaoponto/marcacao" id="btnBancoDeHoras" name="btnBancoDeHoras">Voltar para Marcações</a>
                        </div>
                        <form action="/wpe/quiosque/marcacao/registrar" enctype="multipart/form-data" id="formMarcacao" method="post" name="formMarcacao" novalidate="novalidate">
                            <input type="hidden" id="returnUrl" name="returnUrl">
                            <div class="form-horizontal">
                                <hr>
                                <div class="form-group">
                                    <label class="control-label col-md-2" for="Senha">Senha</label>
                                    <div class="col-md-10">
                                        <input class="form-control text-box single-line password valid" id="Senha" name="Senha" type="password" required>
                                        <input id="Latitude" name="Latitude" readonly="readonly" type="hidden" value="">
                                        <input id="Longitude" name="Longitude" readonly="readonly" type="hidden" value="">
                                        <input id="Gmt" name="Gmt" readonly="readonly" type="hidden" value="-0300">
                                        <span class="text-danger field-validation-valid" data-valmsg-for="Senha" data-valmsg-replace="true"></span>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-lg-2"></div>
                                    <div class="col-lg-4">
                                        <div id="retorno" class="alert alert-info alert-dismissable" style="display: none;"></div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-offset-2 col-md-10">
                                        <input id="btnEfetuarMarcacao" type="submit" value="Efetuar Marcação" class="btn btn-info btn-block" data-loading-text="Aguarde...">
                                    </div>
                                </div>
                            </div>
                        </form>
                        <div class="panel panel-info">
                            <div class="panel-heading">
                                <h3 id="mco_titulo" class="panel-title">Marcações do dia</h3>
                            </div>
                            <div id="mco_pendentes" class="panel-body">
                                <table class="table table-hover table-striped">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Data</th>
                                            <th>Horário</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        """ + pendingRowsHtml + """
                                    </tbody>
                                </table>
                            </div>
                            <div class="panel-footer"><span class="text-danger">* As marcações pendentes serão coletadas até o final do dia. Qualquer dúvida, contate o seu supervisor.</span></div>
                        </div>
                        <div id="mco_request" data-request-url="/wpe/quiosque/marcacao/listarmarcacoesdodia"></div>
                        <hr>
                        <footer>
                            <p class="text-center hidden-print">Todos os direitos reservados © <a href="https://bmasistemas.com.br/home"> BMA Sistemas</a> - 2026</p>
                        </footer>
                    </div>
                    <script src="/wpe/Quiosque/bundles/vendor?v=QUGOWdlm6RtCQJsMEWn2NkVkcS3wgK77GrpjxCXucdE1"></script>
                    <script src="/wpe/Quiosque/bundles/bma-utils?v=TC7KirL4l-iFQ19XCXOlCckbu9mAkuLSLnpv0F9zE281"></script>
                    <script src="/wpe/Quiosque/bundles/bma-message?v=Lcues8svcquP_sDHjFBa3ZfWHsehNGpfb2O4-EEnCQo1"></script>
                    <script>
                        window.errorAlert = function(msg) { console.log("Suppressed errorAlert:", msg); };
                    </script>
                    <script src="/wpe/Quiosque/bundles/modCollector?v=3Bbnbp5SOe9z4yJbb11-gQL3nZwGddheWA-F6gRLM4c1"></script>
                </body>
                </html>
                """;
    }
}
