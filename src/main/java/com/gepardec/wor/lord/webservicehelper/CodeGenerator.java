package com.gepardec.wor.lord.webservicehelper;

import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.Parser;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeGenerator {
    private static final String DIR = "elgkk-util/src/main/java/at/sozvers/stp/lgkk/webservice/helper/";

    private static final String CODE_TEMPLATE = """
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
                        throw new IllegalArgumentException("Das uebergebene Objekt ist nicht vom Typ " + ExecuteService.class.getName());
                    }
                    return (ExecuteService) object;
                }
            }
            """;

    static Parser.Input createInput(String packageName) {
        String dir = "elgkk-util/src/main/java/at/sozvers/stp/lgkk/webservice/helper/";
        String lastSubPackage = LSTUtil.shortNameOfFullyQualified(packageName);
        String className = Accumulator.getServiceClass(packageName);

        Path path = Path.of(dir + lastSubPackage);
        String wsHelperCode = generateWebserviceHelper(lastSubPackage, className);
        return Parser.Input.fromString(path, wsHelperCode);
    }

    private static String generateWebserviceHelper(String packageName, String serviceClassName) {
        List<String> imports = new LinkedList<>(List.of(packageName));
        List<String> serviceClassNames = Stream.generate(() -> packageName)
                .limit(6)
                .collect(Collectors.toList());
        imports.addAll(serviceClassNames);
        return String.format(CODE_TEMPLATE,
                imports.toArray()
        );
    }

}
