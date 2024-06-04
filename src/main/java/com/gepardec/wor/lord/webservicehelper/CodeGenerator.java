package com.gepardec.wor.lord.webservicehelper;

import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.Parser;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeGenerator {
    private static final String DIR = "elgkk-util/src/main/java/at/sozvers/stp/lgkk/webservice/helper/";

    private static final String CODE_TEMPLATE = "package at.sozvers.stp.lgkk.webservice.helper;\n" +
            "\n" +
            "import at.sozvers.stp.lgkk.a02.%s.*;\n" +
            "\n" +
            "import at.sozvers.stp.lgkk.webservice.common.WebserviceHelper;\n" +
            "import at.sozvers.stp.lgkk.webservice.common.data.XmlResponseWrapper;\n" +
            "\n" +
            "import javax.xml.ws.BindingProvider;\n" +
            "\n" +
            "public class %sServiceHelper implements WebserviceHelper<ExecuteService, ExecuteServiceResponse> {\n" +
            "    public static final String ENDPOINT_ADDRESS_PROPERTY = BindingProvider.ENDPOINT_ADDRESS_PROPERTY;\n" +
            "    private final %s port;\n" +
            "\n" +
            "    public %sServiceHelper() {\n" +
            "        %sService service = new %sService();\n" +
            "        this.port = service.get%sPort();\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void setEndpoint(String endpoint) {\n" +
            "        String newUrl = WebserviceHelper.replaceEndpointInAddress(endpoint, this.getEndpoint());\n" +
            "        BindingProvider provider = (BindingProvider) port;\n" +
            "        provider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, newUrl);\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String getEndpoint() {\n" +
            "        BindingProvider provider = (BindingProvider) port;\n" +
            "        return (String) provider.getRequestContext().get(ENDPOINT_ADDRESS_PROPERTY);\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    @Override\n" +
            "    public Class<ExecuteService> getRequestClass() {\n" +
            "        return ExecuteService.class;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public Class<ExecuteServiceResponse> getResponseClass() {\n" +
            "        return ExecuteServiceResponse.class;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> callWebservice(Object request) {\n" +
            "        ExecuteService requestDtoElement = castToExecuteService(request);\n" +
            "        var requestDto = requestDtoElement.getArg0();\n" +
            "\n" +
            "        var responseData = this.port.executeService(requestDto);\n" +
            "\n" +
            "        return createServiceResponse(requestDtoElement, responseData);\n" +
            "    }\n" +
            "\n" +
            "    private XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> createServiceResponse(ExecuteService executeService, Lasaumvw responseData) {\n" +
            "        ObjectFactory objectFactory = new ObjectFactory();\n" +
            "        ExecuteServiceResponse executeServiceResponse = objectFactory.createExecuteServiceResponse();\n" +
            "        executeServiceResponse.setReturn(responseData);\n" +
            "\n" +
            "        return createServiceResponse(executeService, executeServiceResponse);\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    private XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> createServiceResponse(\n" +
            "            ExecuteService executeService, ExecuteServiceResponse executeServiceResponse) {\n" +
            "\n" +
            "        var response = executeServiceResponse.getReturn();\n" +
            "        XmlResponseWrapper<ExecuteService, ExecuteServiceResponse> serviceResponse = new XmlResponseWrapper<>();\n" +
            "        int serviceStatus = response.getMxcb().getNServicestatus().getValue();\n" +
            "        int applicationReturnCode = response.getMxcb().getNApplreturncode().getValue();\n" +
            "\n" +
            "        serviceResponse.setServiceStatus(serviceStatus);\n" +
            "        serviceResponse.setApplicationReturnCode(applicationReturnCode);\n" +
            "        serviceResponse.setRequest(executeService);\n" +
            "        serviceResponse.setResponse(executeServiceResponse);\n" +
            "\n" +
            "        return serviceResponse;\n" +
            "    }\n" +
            "\n" +
            "    private ExecuteService castToExecuteService(Object object) {\n" +
            "        if (!(object instanceof ExecuteService)) {\n" +
            "            throw new IllegalArgumentException(\"Das uebergebene Objekt ist nicht vom Typ \" + ExecuteService.class.getName());\n" +
            "        }\n" +
            "        return (ExecuteService) object;\n" +
            "    }\n" +
            "}\n";

    static Parser.Input createInput(String packageName) {
        String dir = "elgkk-util/src/main/java/at/sozvers/stp/lgkk/webservice/helper/";
        String lastSubPackage = LSTUtil.shortNameOfFullyQualified(packageName);
        String className = lastSubPackage.substring(0, 1).toUpperCase() + lastSubPackage.substring(1);

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
