// camel-k: language=java dependency=camel-quarkus-rest

import java.util.Map;

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.model.dataformat.JsonLibrary;

public class Stur extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration()
            .enableCORS(true)
            .corsAllowCredentials(true)
            .corsHeaderProperty("Access-Control-Allow-Origin","*")
            .corsHeaderProperty("Access-Control-Allow-Methods","PATCH, TRACE, CONNECT, HEAD, POST, GET, OPTIONS, PUT, DELETE")
            .corsHeaderProperty("Access-Control-Max-Age","86400")
            .corsHeaderProperty("Access-Control-Allow-Headers", "*");

        from("rest:get:/servicos/stur/propriedades:/{tipodocumento}/{documento}")
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            .to("log:info")
            .to("http://{{legado-stur}}/propriedades/${header.tipodocumento}/${header.documento}")
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
    }
}
