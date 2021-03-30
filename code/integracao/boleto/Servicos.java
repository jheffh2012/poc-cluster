// camel-k: language=java dependency=camel-quarkus-rest

import java.util.Map;

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.model.dataformat.JsonLibrary;

class IptuBoletoAggregator implements AggregationStrategy {
    
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        String iptu = newExchange.getIn().getBody(String.class);
        String boleto = oldExchange.getIn().getBody(String.class);
        newExchange.getIn().setBody("[" + iptu + "," + boleto + "]");
        return newExchange;
    }
}

public class Servicos extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        getContext().getRegistry().bind("iptuBoletoAggregator", new IptuBoletoAggregator());

        restConfiguration()
            .enableCORS(true)
            .corsAllowCredentials(true)
            .corsHeaderProperty("Access-Control-Allow-Origin","*")
            .corsHeaderProperty("Access-Control-Allow-Methods","POST, GET, OPTIONS, PUT, DELETE")
            .corsHeaderProperty("Access-Control-Max-Age","86400")
            .corsHeaderProperty("Access-Control-Allow-Headers", "*");

        from("rest:get:/servicos/running")
            .setBody().constant("Hello! I'm Running...")
            .to("log:info");

        from("rest:post:/servicos/iptu/")
            .choice()
                .when(body().isNull())
                    .to("log:info")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) {
                    
                            exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
                            exchange.getIn().setHeader("Access-Control-Allow-Methods", "PATCH, TRACE, CONNECT, HEAD, POST, GET, OPTIONS, PUT, DELETE");
                            exchange.getIn().setHeader("Access-Control-Max-Age", "86400");
                            exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
                        };
                    })
                .otherwise()
                    .enrich("direct:gerar-boleto", new AggregationStrategy() {
                        @Override
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            String iptu = newExchange.getIn().getBody(String.class);
                            String boleto = oldExchange.getIn().getBody(String.class);
                            newExchange.getIn().setBody("[" + iptu + "," + boleto + "]");
                            return newExchange;
                        } 
                    })
                    .to("rabbitmq://definition:5672/teste?queue=boleto-gerado")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) {
                    
                            exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
                            exchange.getIn().setHeader("Access-Control-Allow-Methods", "PATCH, TRACE, CONNECT, HEAD, POST, GET, OPTIONS, PUT, DELETE");
                            exchange.getIn().setHeader("Access-Control-Max-Age", "86400");
                            exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
                        };
                    })
            .endChoice();
                

        from("direct:gerar-boleto")
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .to("http://{{castlemock-service}}/emitir-boleto")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) {
            
                    exchange.getIn().setHeader("Access-Control-Allow-Origin", "*");
                    exchange.getIn().setHeader("Access-Control-Allow-Methods", "PATCH, TRACE, CONNECT, HEAD, POST, GET, OPTIONS, PUT, DELETE");
                    exchange.getIn().setHeader("Access-Control-Max-Age", "86400");
                    exchange.getIn().setHeader("Access-Control-Allow-Headers", "*");
                };
            });
    }
}
