// camel-k: language=java dependency=camel-quarkus-rest

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class RabbitMQ extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("rest:post:/rabbit:/{queue}")
      .to("rabbitmq://definition:5672/teste?queue=${queue}")
      .process(new Processor() {
          @Override
          public void process(Exchange exchange) {
          
              exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
              exchange.getIn().setHeader("Access-Control-Allow-Methods", "PATCH, TRACE, CONNECT, HEAD, POST, GET, OPTIONS, PUT, DELETE");
              exchange.getIn().setHeader("Access-Control-Max-Age", "86400");
              exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
          }
      })
      .to("log:info");

    from("rabbitmq://definition:5672/teste?queue=hello")
      .log("Message received from Rabbit : ${body}")
      .to("log:info");

    from("rabbitmq://definition:5672/teste?queue=criar-mensagem")
      .log("Message received from Rabbit : ${body}")
      .to("log:info");
    
    from("rabbitmq://definition:5672/teste?queue=boleto-gerado")
      .log("Message received from Rabbit : ${body}")
      .to("log:info")
      .setHeader("CamelHttpMethod", constant("POST"))
      .to("http://backend-service/mensagem/boleto-gerado");
  }
}
