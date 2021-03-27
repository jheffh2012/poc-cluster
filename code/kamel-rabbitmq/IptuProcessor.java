import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
// import org.apache.camel.AggregationStrategy;
import org.apache.camel.model.dataformat.JsonLibrary;

public class IptuProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Iptu iptu = exchange.getIn().getBody(Iptu.class);
    
        exchange.getIn().setHeader("tipodocumento", iptu.getTipoDocumento());
        exchange.getIn().setHeader("documento", iptu.getDocumento());
        exchange.getIn().setBody(iptu);
    }
}