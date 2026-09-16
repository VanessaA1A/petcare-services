package com.petcare

/*
 * Comentario de modulo PetCare:
 * Prueba de integracion de extremo a extremo (Bloque 6.1).
 *
 * IMPORTANTE: esta suite corre contra una base de datos PostgreSQL LOCAL real ("petcare_test").
 * Deliberadamente NO usa TestContainers ni Docker (no hay Docker disponible en este entorno) ni
 * mocks de ningun tipo: cada llamada HTTP pasa por los controladores, servicios y repositorios
 * reales del contexto de Spring Boot completo (@SpringBootTest, RANDOM_PORT) y persiste filas
 * reales en "petcare_test". Ver README.md, seccion "Tests de integracion", para como preparar
 * esa base de datos y como ejecutar esta suite localmente.
 *
 * Cada ejecucion genera emails/usernames unicos (sufijo con timestamp + numero aleatorio) para
 * no chocar con las restricciones UNIQUE de email/username si se corre varias veces sobre la
 * misma base de datos de prueba.
 */

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val mapper = ObjectMapper()

    @BeforeEach
    fun useJdkHttpClientRequestFactory() {
        // El factory HTTP por defecto de RestTemplate en Spring Framework 6.1 (SimpleClientHttpRequestFactory,
        // sobre HttpURLConnection) siempre activa el modo "streaming" del JDK para el cuerpo de la peticion
        // (setFixedLengthStreamingMode/setChunkedStreamingMode), lo que hace que HttpURLConnection lance
        // HttpRetryException ("cannot retry due to server authentication, in streaming mode") apenas la
        // respuesta a un POST/PUT con cuerpo es 401/407 - un caso legitimo aqui (login con credenciales
        // invalidas). Se usa en su lugar JdkClientHttpRequestFactory (java.net.http.HttpClient), que no
        // tiene esa limitacion; esto es solo del cliente HTTP de la prueba, no un problema del backend.
        restTemplate.restTemplate.requestFactory = JdkClientHttpRequestFactory()
    }

    private fun headers(token: String? = null): HttpHeaders {
        val h = HttpHeaders()
        h.contentType = MediaType.APPLICATION_JSON
        if (token != null) h.setBearerAuth(token)
        return h
    }

    private fun post(path: String, body: Any?, token: String? = null): ResponseEntity<String> =
        restTemplate.exchange(path, HttpMethod.POST, HttpEntity(body, headers(token)), String::class.java)

    private fun put(path: String, body: Any?, token: String? = null): ResponseEntity<String> =
        restTemplate.exchange(path, HttpMethod.PUT, HttpEntity(body, headers(token)), String::class.java)

    private fun get(path: String, token: String? = null): ResponseEntity<String> =
        restTemplate.exchange(path, HttpMethod.GET, HttpEntity<Any>(headers(token)), String::class.java)

    private fun json(response: ResponseEntity<String>): JsonNode = mapper.readTree(response.body ?: "{}")

    private fun uniqueSuffix() = "${System.currentTimeMillis()}-${(1000..999999).random()}"

    @Test
    fun `flujo completo propietario-cuidador con servicio, chat y calificacion`() {
        val suffix = uniqueSuffix()
        val password = "Passw0rd!23"

        // 1) Registro del propietario (owner)
        val ownerEmail = "owner-$suffix@petcare-test.local"
        val ownerRegRes = post("/api/auth/registro", mapOf("email" to ownerEmail, "password" to password))
        assertEquals(HttpStatus.CREATED, ownerRegRes.statusCode, "registro de propietario deberia responder 201")
        val ownerReg = json(ownerRegRes)
        val ownerId = ownerReg["user"]["id"].asInt()
        assertTrue(ownerId > 0)
        assertNotNull(ownerReg["session"]["tokenSesion"].asText())

        // Confirma el rol "propietario" para esa cuenta (regla de negocio: un usuario elige su rol una sola vez)
        val ownerRoleRes = post("/api/users/$ownerId/roles", mapOf("role" to "propietario"))
        assertEquals(HttpStatus.OK, ownerRoleRes.statusCode, "confirmar rol propietario deberia responder 200")
        assertEquals("OWNER", json(ownerRoleRes)["rol"].asText())

        // 2) Registro del cuidador (caregiver)
        val caregiverEmail = "caregiver-$suffix@petcare-test.local"
        val caregiverRegRes = post("/api/auth/registro", mapOf("email" to caregiverEmail, "password" to password))
        assertEquals(HttpStatus.CREATED, caregiverRegRes.statusCode, "registro de cuidador deberia responder 201")
        val caregiverReg = json(caregiverRegRes)
        val caregiverId = caregiverReg["user"]["id"].asInt()
        assertTrue(caregiverId > 0)

        val caregiverRoleRes = post("/api/users/$caregiverId/roles", mapOf("role" to "cuidador"))
        assertEquals(HttpStatus.OK, caregiverRoleRes.statusCode, "confirmar rol cuidador deberia responder 200")
        assertEquals("CAREGIVER", json(caregiverRoleRes)["rol"].asText())

        // 3) Login de ambas cuentas (obtiene un token JWT de sesion)
        val ownerLoginRes = post("/api/auth/login", mapOf("email" to ownerEmail, "password" to password))
        assertEquals(HttpStatus.OK, ownerLoginRes.statusCode, "login de propietario deberia responder 200")
        val ownerToken = json(ownerLoginRes)["session"]["tokenSesion"].asText()
        assertNotNull(ownerToken)

        val caregiverLoginRes = post("/api/auth/login", mapOf("email" to caregiverEmail, "password" to password))
        assertEquals(HttpStatus.OK, caregiverLoginRes.statusCode, "login de cuidador deberia responder 200")
        val caregiverToken = json(caregiverLoginRes)["session"]["tokenSesion"].asText()
        assertNotNull(caregiverToken)

        // El token JWT de sesion realmente autentica contra un endpoint protegido por Bearer token
        val meRes = get("/api/auth/me", ownerToken)
        assertEquals(HttpStatus.OK, meRes.statusCode, "GET /api/auth/me con el token de sesion deberia responder 200")
        assertEquals(ownerId, json(meRes)["user"]["id"].asInt())

        // 4) El propietario registra una mascota (requerida para publicar una solicitud)
        val petRes = post(
            "/api/pets",
            mapOf(
                "owner_id" to ownerId,
                "name" to "Rocky",
                "species" to "Perro",
                "breed" to "Labrador",
                "size" to "GRANDE",
                "age" to 3
            )
        )
        assertEquals(HttpStatus.CREATED, petRes.statusCode, "crear mascota deberia responder 201")
        val petId = json(petRes)["id"].asInt()
        assertTrue(petId > 0)

        // 5) El propietario publica una solicitud de servicio
        val requestRes = post(
            "/api/service-requests",
            mapOf(
                "owner_id" to ownerId,
                "pet_id" to petId,
                "service_type_id" to 1,
                "title" to "Paseo el sabado",
                "description" to "Necesito un paseo de una hora en la tarde.",
                "requested_date" to "2026-09-20",
                "start_time" to "15:00",
                "end_time" to "16:00"
            )
        )
        assertEquals(HttpStatus.CREATED, requestRes.statusCode, "crear solicitud de servicio deberia responder 201")
        val requestBody = json(requestRes)
        val requestId = requestBody["id"].asInt()
        assertTrue(requestId > 0)
        assertEquals("PENDING", requestBody["status"].asText())

        // 6) El cuidador se postula a la solicitud
        val applicationRes = post(
            "/api/service-applications",
            mapOf(
                "service_request_id" to requestId,
                "caregiver_id" to caregiverId,
                "initiated_by" to "CAREGIVER"
            )
        )
        assertEquals(HttpStatus.CREATED, applicationRes.statusCode, "crear postulacion deberia responder 201")
        val applicationId = json(applicationRes)["id"].asInt()
        assertTrue(applicationId > 0)

        // 7) El propietario acepta la postulacion
        val acceptRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "ACCEPTED"))
        assertEquals(HttpStatus.OK, acceptRes.statusCode, "aceptar postulacion deberia responder 200")
        assertEquals("ACCEPTED", json(acceptRes)["status"].asText())

        // 8) Intercambio de al menos un mensaje de chat en ambos sentidos
        val msg1 = post(
            "/api/chat/mensajes",
            mapOf(
                "service_request_id" to requestId,
                "sender_id" to ownerId,
                "receiver_id" to caregiverId,
                "message" to "Hola, podrias pasear a Rocky el sabado a las 3pm?"
            )
        )
        assertEquals(HttpStatus.CREATED, msg1.statusCode, "enviar mensaje de chat deberia responder 201")

        val msg2 = post(
            "/api/chat/mensajes",
            mapOf(
                "service_request_id" to requestId,
                "sender_id" to caregiverId,
                "receiver_id" to ownerId,
                "message" to "Claro, ahi estare."
            )
        )
        assertEquals(HttpStatus.CREATED, msg2.statusCode, "responder mensaje de chat deberia responder 201")

        val conversationRes = get("/api/chat/mensajes/$requestId")
        assertEquals(HttpStatus.OK, conversationRes.statusCode, "historial de chat deberia responder 200")
        assertEquals(2, json(conversationRes).size())

        // 9) El cuidador marca el servicio como realizado y el propietario lo da por completado
        val doneRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "DONE_BY_CAREGIVER"))
        assertEquals(HttpStatus.OK, doneRes.statusCode, "marcar DONE_BY_CAREGIVER deberia responder 200")
        assertEquals("DONE_BY_CAREGIVER", json(doneRes)["status"].asText())

        val completeRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "COMPLETED"))
        assertEquals(HttpStatus.OK, completeRes.statusCode, "completar el servicio deberia responder 200")
        assertEquals("COMPLETED", json(completeRes)["status"].asText())

        val requestAfterComplete = get("/api/service-requests/$requestId")
        assertEquals(HttpStatus.OK, requestAfterComplete.statusCode)
        assertEquals("COMPLETED", json(requestAfterComplete)["status"].asText())

        // 10) El propietario califica al cuidador
        val ratingRes = post(
            "/api/ratings",
            mapOf(
                "service_request_id" to requestId,
                "caregiver_id" to caregiverId,
                "owner_id" to ownerId,
                "rated_by_role" to "OWNER",
                "score" to 4.5,
                "comment" to "Excelente atencion, muy puntual."
            )
        )
        assertEquals(HttpStatus.CREATED, ratingRes.statusCode, "crear calificacion deberia responder 201")
        assertEquals(4.5, json(ratingRes)["score"].asDouble())

        val summaryRes = get("/api/ratings/caregiver/$caregiverId/summary")
        assertEquals(HttpStatus.OK, summaryRes.statusCode, "resumen de calificaciones deberia responder 200")
        assertEquals(1, json(summaryRes)["count"].asInt())
        assertEquals(4.5, json(summaryRes)["average"].asDouble())
    }

    // --- Casos negativos: codigos de error esperados en endpoints clave del flujo ---

    @Test
    fun `registro sin password responde 400`() {
        val res = post(
            "/api/auth/registro",
            mapOf("email" to "sin-password-${uniqueSuffix()}@petcare-test.local")
        )
        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `registro con email duplicado responde 400`() {
        val email = "duplicado-${uniqueSuffix()}@petcare-test.local"
        val first = post("/api/auth/registro", mapOf("email" to email, "password" to "Passw0rd!23"))
        assertEquals(HttpStatus.CREATED, first.statusCode)

        val second = post("/api/auth/registro", mapOf("email" to email, "password" to "OtraPassw0rd!23"))
        assertEquals(HttpStatus.BAD_REQUEST, second.statusCode)
    }

    @Test
    fun `login con credenciales invalidas responde 401`() {
        val email = "login-invalido-${uniqueSuffix()}@petcare-test.local"
        val regRes = post("/api/auth/registro", mapOf("email" to email, "password" to "Passw0rd!23"))
        assertEquals(HttpStatus.CREATED, regRes.statusCode)

        val loginRes = post("/api/auth/login", mapOf("email" to email, "password" to "clave-incorrecta"))
        assertEquals(HttpStatus.UNAUTHORIZED, loginRes.statusCode)
    }

    @Test
    fun `login de usuario inexistente responde 404`() {
        val res = post(
            "/api/auth/login",
            mapOf("email" to "no-existe-${uniqueSuffix()}@petcare-test.local", "password" to "cualquiera")
        )
        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `obtener solicitud de servicio inexistente responde 404`() {
        val res = get("/api/service-requests/999999999")
        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `crear solicitud de servicio sin campos requeridos responde 400`() {
        val res = post("/api/service-requests", mapOf("title" to "solicitud incompleta"))
        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `expediente medico visible para el dueno y para el cuidador antes, durante y despues del servicio`() {
        val suffix = uniqueSuffix()
        val password = "Passw0rd!23"

        fun registrarUsuario(prefijo: String, rol: String): Int {
            val email = "$prefijo-$suffix@petcare-test.local"
            val regRes = post("/api/auth/registro", mapOf("email" to email, "password" to password))
            assertEquals(HttpStatus.CREATED, regRes.statusCode)
            val id = json(regRes)["user"]["id"].asInt()
            val roleRes = post("/api/users/$id/roles", mapOf("role" to rol))
            assertEquals(HttpStatus.OK, roleRes.statusCode)
            return id
        }

        val ownerId = registrarUsuario("expediente-owner", "propietario")
        val caregiverAcceptedId = registrarUsuario("expediente-cg-aceptado", "cuidador")
        val caregiverAjenoId = registrarUsuario("expediente-cg-ajeno", "cuidador")

        val petRes = post(
            "/api/pets",
            mapOf("owner_id" to ownerId, "name" to "Firulais", "species" to "Perro", "breed" to "Mestizo", "size" to "MEDIANO", "age" to 2)
        )
        assertEquals(HttpStatus.CREATED, petRes.statusCode)
        val petId = json(petRes)["id"].asInt()

        // El dueno siempre puede ver el expediente de su propia mascota.
        val ownerViewRes = get("/api/pets/$petId/expediente?usuario_id=$ownerId")
        assertEquals(HttpStatus.OK, ownerViewRes.statusCode, "el dueno deberia poder ver el expediente de su mascota")

        // Un cuidador cualquiera puede ver el expediente ANTES de ofertar, mientras la solicitud siga PENDIENTE.
        val requestRes = post(
            "/api/service-requests",
            mapOf(
                "owner_id" to ownerId,
                "pet_id" to petId,
                "service_type_id" to 1,
                "title" to "Paseo de prueba",
                "description" to "Paseo corto",
                "requested_date" to "2026-09-25",
                "start_time" to "10:00",
                "end_time" to "11:00"
            )
        )
        assertEquals(HttpStatus.CREATED, requestRes.statusCode)
        val requestId = json(requestRes)["id"].asInt()

        val beforeOfferRes = get("/api/pets/$petId/expediente?usuario_id=$caregiverAjenoId")
        assertEquals(
            HttpStatus.OK, beforeOfferRes.statusCode,
            "cualquier cuidador deberia poder ver el expediente mientras la solicitud esta PENDIENTE"
        )

        // caregiverAcceptedId se postula y es aceptado -> debe poder ver el expediente durante el servicio.
        val applicationRes = post(
            "/api/service-applications",
            mapOf("service_request_id" to requestId, "caregiver_id" to caregiverAcceptedId, "initiated_by" to "CAREGIVER")
        )
        assertEquals(HttpStatus.CREATED, applicationRes.statusCode)
        val applicationId = json(applicationRes)["id"].asInt()

        val acceptRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "ACCEPTED"))
        assertEquals(HttpStatus.OK, acceptRes.statusCode)

        val duringServiceRes = get("/api/pets/$petId/expediente?usuario_id=$caregiverAcceptedId")
        assertEquals(
            HttpStatus.OK, duringServiceRes.statusCode,
            "el cuidador con la postulacion aceptada deberia poder ver el expediente durante el servicio"
        )

        // Ahora que la solicitud ya no esta PENDIENTE, un cuidador ajeno sin postulacion no deberia poder verlo.
        val ajenoDuringRes = get("/api/pets/$petId/expediente?usuario_id=$caregiverAjenoId")
        assertEquals(
            HttpStatus.FORBIDDEN, ajenoDuringRes.statusCode,
            "un cuidador sin relacion con la solicitud no deberia poder ver el expediente una vez que dejo de estar PENDIENTE"
        )

        // Al completar el servicio, el cuidador que lo realizo sigue pudiendo verlo (referencia historica).
        val doneRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "DONE_BY_CAREGIVER"))
        assertEquals(HttpStatus.OK, doneRes.statusCode)
        val completeRes = put("/api/service-applications/$applicationId/status", mapOf("status" to "COMPLETED"))
        assertEquals(HttpStatus.OK, completeRes.statusCode)

        val afterServiceRes = get("/api/pets/$petId/expediente?usuario_id=$caregiverAcceptedId")
        assertEquals(
            HttpStatus.OK, afterServiceRes.statusCode,
            "el cuidador que completo el servicio deberia poder seguir viendo el expediente despues"
        )
    }

    @Test
    fun `disponibilidad del cuidador se puede publicar, valida solapamiento y aparece expandida en el calendario`() {
        val suffix = uniqueSuffix()
        val password = "Passw0rd!23"

        fun registrarUsuario(prefijo: String, rol: String): Int {
            val email = "$prefijo-$suffix@petcare-test.local"
            val regRes = post("/api/auth/registro", mapOf("email" to email, "password" to password))
            assertEquals(HttpStatus.CREATED, regRes.statusCode)
            val id = json(regRes)["user"]["id"].asInt()
            val roleRes = post("/api/users/$id/roles", mapOf("role" to rol))
            assertEquals(HttpStatus.OK, roleRes.statusCode)
            return id
        }

        val caregiverId = registrarUsuario("disponibilidad-cg", "cuidador")
        val ownerId = registrarUsuario("disponibilidad-owner", "propietario")

        // GET /api/calendario exige un JWT valido (a diferencia del resto de la API, que confia
        // en el usuario_id que manda el cliente) - hace falta loguearse para obtener un token.
        val caregiverEmail = "disponibilidad-cg-$suffix@petcare-test.local"
        val loginRes = post("/api/auth/login", mapOf("email" to caregiverEmail, "password" to password))
        assertEquals(HttpStatus.OK, loginRes.statusCode)
        val caregiverToken = json(loginRes)["session"]["tokenSesion"].asText()

        val hoy = java.time.LocalDate.now()
        val diaSemana = hoy.dayOfWeek.value - 1 // lunes=0 ... domingo=6

        // Un propietario no puede publicar disponibilidad (no es cuidador).
        val rechazoRes = post(
            "/api/cuidadores/disponibilidad",
            mapOf("usuario_id" to ownerId, "dia_semana" to diaSemana, "hora_inicio" to "09:00", "hora_fin" to "12:00")
        )
        assertEquals(HttpStatus.FORBIDDEN, rechazoRes.statusCode, "un propietario no deberia poder publicar disponibilidad")

        // El cuidador si puede.
        val crearRes = post(
            "/api/cuidadores/disponibilidad",
            mapOf("usuario_id" to caregiverId, "dia_semana" to diaSemana, "hora_inicio" to "09:00", "hora_fin" to "12:00")
        )
        assertEquals(HttpStatus.CREATED, crearRes.statusCode)
        val disponibilidadId = json(crearRes)["id"].asInt()

        // Un horario solapado el mismo dia se rechaza.
        val solapadoRes = post(
            "/api/cuidadores/disponibilidad",
            mapOf("usuario_id" to caregiverId, "dia_semana" to diaSemana, "hora_inicio" to "10:00", "hora_fin" to "11:00")
        )
        assertEquals(HttpStatus.BAD_REQUEST, solapadoRes.statusCode, "un horario solapado deberia rechazarse")

        // Un horario en otro dia de la semana (sin solape) se acepta.
        val otroDiaRes = post(
            "/api/cuidadores/disponibilidad",
            mapOf("usuario_id" to caregiverId, "dia_semana" to (diaSemana + 1) % 7, "hora_inicio" to "09:00", "hora_fin" to "12:00")
        )
        assertEquals(HttpStatus.CREATED, otroDiaRes.statusCode)

        val listarRes = get("/api/cuidadores/$caregiverId/disponibilidad")
        assertEquals(HttpStatus.OK, listarRes.statusCode)
        assertEquals(2, json(listarRes).size())

        // El calendario del mes actual debe incluir hoy con el rango publicado.
        val calendarioRes = get("/api/calendario?usuario_id=$caregiverId&mes=${hoy.monthValue}&anio=${hoy.year}", caregiverToken)
        assertEquals(HttpStatus.OK, calendarioRes.statusCode)
        val disponibilidadCalendario = json(calendarioRes)["disponibilidad"]
        val entradaHoy = disponibilidadCalendario.find { it["fecha"].asText() == hoy.toString() }
        assertNotNull(entradaHoy, "el calendario deberia incluir la disponibilidad de hoy")
        val horas = entradaHoy!!["horas"].map { it.asText() }
        assertTrue(horas.contains("09:00-12:00"), "el rango publicado deberia aparecer expandido en el calendario")

        // Eliminar y verificar que ya no aparece.
        val eliminarRes = restTemplate.exchange(
            "/api/cuidadores/disponibilidad/$disponibilidadId?usuario_id=$caregiverId",
            HttpMethod.DELETE,
            HttpEntity<Any>(headers()),
            String::class.java
        )
        assertEquals(HttpStatus.NO_CONTENT, eliminarRes.statusCode)

        val listarTrasEliminarRes = get("/api/cuidadores/$caregiverId/disponibilidad")
        assertEquals(1, json(listarTrasEliminarRes).size())
    }
}
