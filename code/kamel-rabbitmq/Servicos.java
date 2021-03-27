// camel-k: language=java dependency=camel-quarkus-rest

import java.util.Map;

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.model.dataformat.JsonLibrary;

class Iptu {
    private String documento;
    private String tipoDocumento;

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String newDocumento) {
        this.documento = newDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String newTipoDocumento) {
        this.tipoDocumento = newTipoDocumento;
    }
}

class IptuProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Iptu iptu = exchange.getIn().getBody(Iptu.class);
    
        exchange.getIn().setHeader("tipodocumento", iptu.getTipoDocumento());
        exchange.getIn().setHeader("documento", iptu.getDocumento());
        exchange.getIn().setBody(iptu);
    }
}

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
        getContext().getRegistry().bind("iptuProcessor", new IptuProcessor());
        getContext().getRegistry().bind("iptu", new Iptu());
        getContext().getRegistry().bind("iptuBoletoAggregator", new IptuBoletoAggregator());

        restConfiguration()
            .enableCORS(true)
            .corsAllowCredentials(true)
            .corsHeaderProperty("Access-Control-Allow-Origin","*")
            .corsHeaderProperty("Access-Control-Allow-Methods","POST, GET, OPTIONS, PUT, DELETE")
            .corsHeaderProperty("Access-Control-Max-Age","86400")
            .corsHeaderProperty("Access-Control-Allow-Headers", "*");
            // .corsHeaderProperty("Access-Control-Allow-Headers","Content-Type, Authorization, X-Requested-With, User-Agent, Referer, Accept");

        from("rest:get:/servicos/running")
            .setBody().constant("Hello! I'm Running...")
            .to("log:info");

        from("rest:post:/servicos/iptu")
            .convertBodyTo(String.class)
            .to("log:info")
            .unmarshal().json(JsonLibrary.Jackson, Iptu.class)
            .process("iptuProcessor")
            .marshal().json()
            .enrich("direct:gerar-boleto", new AggregationStrategy() {
                @Override
                public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                    String iptu = newExchange.getIn().getBody(String.class);
                    String boleto = oldExchange.getIn().getBody(String.class);
                    newExchange.getIn().setBody("[" + iptu + "," + boleto + "]");
                    return newExchange;
                } 
            })
            .to("rabbitmq://definition:5672/teste?queue=boleto-gerado");

        from("direct:gerar-boleto")
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .to("http://{{castlemock-service}}/emitir-boleto");
    }
}
