package com.gepardec.wor.lord.servicehelper;


import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.text.PlainTextParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateWebserviceHelper extends ScanningRecipe<Accumulator> {

    @Override
    public String getDisplayName() {
        return "Append to release notes";
    }

    @Override
    public String getDescription() {
        return "Adds the specified line to RELEASE.md.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new ServiceSearch(acc);
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        List<Parser.Input> inputs = acc.getServicePackages().stream()
                .map(GenerateWebserviceHelper::createInput)
                .collect(Collectors.toList());
        return PlainTextParser
                .builder()
                .build()
                .parseInputs(inputs, Path.of(""), ctx)
                .collect(Collectors.toList());
    }

    private static Parser.Input createInput(String packageName) {
        String dir = "elgkk-util/src/main/java/at/sozvers/stp/lgkk/webservice/helper/";
        String lastSubPackage = LSTUtil.shortNameOfFullyQualified(packageName);
        String className = lastSubPackage.substring(0, 1).toUpperCase() + lastSubPackage.substring(1);

        Path path = Path.of(dir + lastSubPackage);
        String wsHelperCode = generateWebserviceHelper(lastSubPackage, className);
        return Parser.Input.fromString(path, wsHelperCode);
    }

    private static String generateWebserviceHelper(String packageName, String serviceClassName) {
        return String.format(WEBSERVICE_HELPER_TEMPLATE,
                packageName,
                serviceClassName,
                serviceClassName,
                serviceClassName,
                serviceClassName,
                serviceClassName,
                serviceClassName
                );
    }

    private static final String WEBSERVICE_HELPER_TEMPLATE = """
            package at.sozvers.stp.lgkk.webservice.helper;
            
            import at.sozvers.stp.lgkk.a02.%s.*;
            
            import at.sozvers.stp.lgkk.webservice.common.WebserviceHelper;
            import at.sozvers.stp.lgkk.webservice.common.data.XmlResponseWrapper;
            
            import javax.xml.ws.BindingProvider;
            
            public class %sServiceHelper implements WebserviceHelper<ExecuteService, ExecuteServiceResponse> {
                public static final String ENDPOINT_ADDRESS_PROPERTY = BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
                private final %s port;
            
                public %sServiceHelper() {
                    %sService service = new %sService();
                    this.port = service.get%sPort();
                }
            
                @Override
                public void setEndpoint(String endpoint) {
                    String newUrl = WebserviceHelper.replaceEndpointInAddress(endpoint, this.getEndpoint());
                    BindingProvider provider = (BindingProvider) port;
                    provider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, newUrl);
                }
            
                @Override
                public String getEndpoint() {
                    BindingProvider provider = (BindingProvider) port;
                    return (String) provider.getRequestContext().get(ENDPOINT_ADDRESS_PROPERTY);
                }
            
            
                @Override
                public Class<ExecuteService> getRequestClass() {
                    return ExecuteService.class;
                }
            
                @Override
                public Class<ExecuteServiceResponse> getResponseClass() {
                    return ExecuteServiceResponse.class;
                }
            
                @Override
                public XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> callWebservice(Object request) {
                    ExecuteService requestDtoElement = castToExecuteService(request);
                    var requestDto = requestDtoElement.getArg0();
            
                    var responseData = this.port.executeService(requestDto);
            
                    return createServiceResponse(requestDtoElement, responseData);
                }
            
                private XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> createServiceResponse(ExecuteService executeService, Lasaumvw responseData) {
                    ObjectFactory objectFactory = new ObjectFactory();
                    ExecuteServiceResponse executeServiceResponse = objectFactory.createExecuteServiceResponse();
                    executeServiceResponse.setReturn(responseData);
            
                    return createServiceResponse(executeService, executeServiceResponse);
                }
            
            
                private XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> createServiceResponse(
                        ExecuteService executeService, ExecuteServiceResponse executeServiceResponse) {
            
                    var response = executeServiceResponse.getReturn();
                    XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> serviceResponse = new XmlResponseWrapper<>();
                    int serviceStatus = response.getMxcb().getNServicestatus().getValue();
                    int applicationReturnCode = response.getMxcb().getNApplreturncode().getValue();
            
                    serviceResponse.setServiceStatus(serviceStatus);
                    serviceResponse.setApplicationReturnCode(applicationReturnCode);
                    serviceResponse.setRequest(executeService);
                    serviceResponse.setResponse(executeServiceResponse);
            
                    return serviceResponse;
                }
            
                private ExecuteService castToExecuteService(Object object) {
                    if (!(object instanceof ExecuteService)) {
                        throw new IllegalArgumentException("Das übergebene Objekt ist nicht vom Typ " + ExecuteService.class.getName());
                    }
                    return (ExecuteService) object;
                }
            }
            """;

}
