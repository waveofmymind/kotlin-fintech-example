package com.waveofmymind.gateway.filter

import com.waveofmymind.gateway.jwt.JwtException
import com.waveofmymind.gateway.jwt.JwtVerifier
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationGatewayFilter(
    private val jwtVerifier: JwtVerifier
) : AbstractGatewayFilterFactory<
        JwtAuthenticationGatewayFilter.Config>(Config::class.java) {

    data class Config(
        val name: String
    )

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token = exchange.request.headers[HttpHeaders.AUTHORIZATION]?.get(0)?.substring(7)
            val decodedJwt = jwtVerifier.verify(token!!)
            addAuthorizationHeader(exchange, decodedJwt.claims["userId"].toString())
            chain.filter(exchange)
        }
    }

    private fun addAuthorizationHeader(exchange: ServerWebExchange, userId: String) {
        exchange.mutate().request(
            exchange.request.mutate().header("X-Authorization-id", userId).build()
        ).build()
    }

    @Bean
    fun tokenValidation(): ErrorWebExceptionHandler {
        return JwtExceptionHandler()
    }

    class JwtExceptionHandler : ErrorWebExceptionHandler {
        private fun getErrorCode(errorCode: Int) = "에러 코드: $errorCode"

        override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
            var errorCode = 500
            when (ex) {
                is JwtException -> {
                    errorCode = 401
                }

                is IllegalArgumentException -> {
                    errorCode = 400
                }
            }
            val bytes = getErrorCode(errorCode).toByteArray()
            val buffer = exchange.response.bufferFactory().wrap(bytes)
            return exchange.response.writeWith(Flux.just(buffer))
        }
    }
}
