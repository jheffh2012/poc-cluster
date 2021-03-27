// camel-k: language=java dependency=camel-quarkus-rest

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;

public class RabbitMQMessage extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("rest:get:/rabbit/hello")
      .setBody().constant("Hello Word!")
      .to("rabbitmq://definition:5672/teste?queue=hello")
      .to("log:info");

    from("rest:post:/rabbit")
      .to("rabbitmq://definition:5672/teste?queue=criar-mensagem")
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
