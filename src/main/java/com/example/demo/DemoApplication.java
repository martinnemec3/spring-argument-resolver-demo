package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageReaderArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Controller
	public static class MyController {

		@PostMapping("/")
		public Mono<ResponseEntity<String>> myEndpoint(GetDataRequest getDataRequest) {
			return Mono.just("Request headers: " + getDataRequest.getHeader1() + ", " + getDataRequest.getHeader2() + ", Request body values: " + getDataRequest.getBodyValue1() + ", " + getDataRequest.getBodyValue2())
					.map(ResponseEntity::ok);
		}

	}

	@Configuration
	public static class WebFluxConfiguration implements WebFluxConfigurer {

		@Autowired
		ApplicationContext applicationContext;

		@Override
		public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
			ServerCodecConfigurer serverCodecConfigurer = applicationContext.getBean(ServerCodecConfigurer.class);
			ReactiveAdapterRegistry reactiveAdapterRegistry = applicationContext.getBean("webFluxAdapterRegistry", ReactiveAdapterRegistry.class);
			configurer.addCustomResolver(new MyArgumentResolver(serverCodecConfigurer.getReaders(), reactiveAdapterRegistry));
		}
	}

	public static class MyArgumentResolver extends AbstractMessageReaderArgumentResolver {

		public MyArgumentResolver(List<HttpMessageReader<?>> messageReaders, ReactiveAdapterRegistry adapterRegistry) {
			super(messageReaders, adapterRegistry);
		}

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return MyAbstractRequest.class.isAssignableFrom(parameter.getParameterType());
		}

		@Override
		public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
			return readBody(parameter, true, bindingContext, exchange)
					.map(o -> {
						((MyAbstractRequest) o).setHeader1(exchange.getRequest().getHeaders().getFirst("header1"));
						((MyAbstractRequest) o).setHeader2(exchange.getRequest().getHeaders().getFirst("header2"));
						return o;
					});
		}
	}

	public static class MyAbstractRequest {
		private String header1;
		private String header2;

		public String getHeader1() {
			return header1;
		}

		public void setHeader1(String header1) {
			this.header1 = header1;
		}

		public String getHeader2() {
			return header2;
		}

		public void setHeader2(String header2) {
			this.header2 = header2;
		}
	}

	public static class GetDataRequest extends MyAbstractRequest {
		private String bodyValue1;
		private String bodyValue2;

		public String getBodyValue1() {
			return bodyValue1;
		}

		public void setBodyValue1(String bodyValue1) {
			this.bodyValue1 = bodyValue1;
		}

		public String getBodyValue2() {
			return bodyValue2;
		}

		public void setBodyValue2(String bodyValue2) {
			this.bodyValue2 = bodyValue2;
		}
	}
}
