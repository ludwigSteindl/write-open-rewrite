package com.gepardec.wor.lord.dto;

import com.gepardec.wor.helpers.SourceFileContents;
import com.gepardec.wor.lord.call.ternaries.BinaryProxyToWebTernaryAndClassTest;
import com.gepardec.wor.lord.util.ParserUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryDtoToWebTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryAndClassTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new BinaryDtoToWeb())
          .parser(ParserUtil.createParserWithRuntimeClasspath())
          .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    public void whenBinaryStdhSet_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.LaqamhsuDto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        LaqamhsuDto reqDto = new LaqamhsuDto();\n" +
                  "        reqDto.setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaamhsu.Laqamhsu;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaamhsu.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqamhsu reqDto = new Laqamhsu();\n" +
                  "        reqDto.setStdh(objectFactory.createOmStandardRequestHeader());\n" +
                  "        reqDto.getStdh().setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n")
        );
    }

    @DocumentExample
    @Test
    public void whenBinaryStdhSetWithOtherNamesAndTyoe_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.Laqaumv4Dto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4Dto request = new Laqaumv4Dto();\n" +
                  "        request.setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4 request = new Laqaumv4();\n" +
                  "        request.setStdh(objectFactory.createOmStandardRequestHeader());\n" +
                  "        request.getStdh().setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenBinaryStdhSetWith2TimesSameType_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.Laqaumv4Dto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4Dto request = new Laqaumv4Dto();\n" +
                  "        request.setZvst(\"11\");\n" +
                  "        request.setTransactionid(1L);\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4 request = new Laqaumv4();\n" +
                  "        request.setStdh(objectFactory.createOmStandardRequestHeader());\n" +
                  "        request.getStdh().setZvst(\"11\");\n" +
                  "        request.getStdh().setTransactionid(1L);\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenBinarySetterWithNestedWebObjectUsed_thenCreateNestedWebDto() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.Laqaumv4Dto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4Dto request = new Laqaumv4Dto();\n" +
                  "        request.setPostleitzahl(\"1220\");\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4 request = new Laqaumv4();\n" +
                  "        request.setDatenv3(objectFactory.createLaqaumv4Datenv3());\n" +
                  "        request.getDatenv3().setPostleitzahl(objectFactory.createLaqaumv4Datenv3Postleitzahl(\"1220\"));\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenNestedSetters_thenTransformNestedSetters() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.Laqaumv4Dto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4Dto request = new Laqaumv4Dto();\n" +
                  "        request.setPostleitzahl(\"1220\");\n" +
                  "        request.setCReserved(\"test\");\n" +
                  "        request.setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqaumv4 request = new Laqaumv4();\n" +
                  "        request.setStdh(objectFactory.createOmStandardRequestHeader());\n" +
                  "        request.setMxcb(objectFactory.createMxcb());\n" +
                  "        request.setDatenv3(objectFactory.createLaqaumv4Datenv3());\n" +
                  "        request.getDatenv3().setPostleitzahl(objectFactory.createLaqaumv4Datenv3Postleitzahl(\"1220\"));\n" +
                  "        request.getMxcb().setCReserved(objectFactory.createMxcbCReserved(\"test\"));\n" +
                  "        request.getStdh().setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenBinaryStdhSetWithObjectFactoryAlreadyThere_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.LaqamhsuDto;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaamhsu.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        LaqamhsuDto reqDto = new LaqamhsuDto();\n" +
                  "        reqDto.setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import at.sozvers.stp.lgkk.a02.laaamhsu.Laqamhsu;\n" +
                  "import at.sozvers.stp.lgkk.a02.laaamhsu.ObjectFactory;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    private static final ObjectFactory objectFactory = new ObjectFactory();\n" +
                  "    public void test() {\n" +
                  "        Laqamhsu reqDto = new Laqamhsu();\n" +
                  "        reqDto.setStdh(objectFactory.createOmStandardRequestHeader());\n" +
                  "        reqDto.getStdh().setZvst(\"11\");\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenNoBinaryStdhSetButDto_thenCreateWebDtoWithoutStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.LaqamhsuDto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        LaqamhsuDto reqDto = new LaqamhsuDto();\n" +
                  "        reqDto.toString();\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenNoWsdlService_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.LaqaumwtDto;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        LaqaumwtDto reqDto = new LaqaumwtDto();\n" +
                  "        reqDto.toString();\n" +
                  "    }\n" +
                  "}\n")
        );
    }

    @DocumentExample
    @Test
    public void whenTypeUsedByWsdlServiceButNoDto_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "        String reqDto = new String();\n" +
                  "        reqDto.getClass();\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenNoDtoUsed_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public void test() {\n" +
                  "          int i = 0;\n" +
                  "    }\n" +
                  "}\n")
        );
    }

    @Test
    public void whenRealElgkkCodeUsed_thenTransformCode() {
        rewriteRunWithWsdlClasses(
          //language=java
          java(
                  "import java.util.Calendar;\n" +
                  "import com.gepardec.wor.lord.stubs.Laqaumv4Dto;\n" +
                  "import com.gepardec.wor.lord.stubs.AuAfMeldung4Dto;\n" +
                  "\n" +
                  "import org.apache.commons.logging.Log;\n" +
                  "import org.apache.commons.logging.LogFactory;\n" +
                  "\n" +
                  "private static final Log log = LogFactory.getLog(AuAfMeldung2EJBeanImpl.class);\n" +
                  "\n" +
                  "public class AuAfMeldung2EJBeanImpl {\n" +
                  "\n" +
                  "    private WSResponseDto callSvcProxy(AuAfMeldung4Dto req) {\n" +
                  "        WSResponseDto ret = null;\n" +
                  "\n" +
                  "        // ASSERT: Validierung der Eingabedaten ok\n" +
                  "\n" +
                  "        // Technisches Dto erzeugen\n" +
                  "        Laqaumv4Dto reqDto = new Laqaumv4Dto();\n" +
                  "\n" +
                  "\n" +
                  "        // StdReqHeader setzen\n" +
                  "        reqDto.setZvst(String.valueOf(req.getZustaendigertraeger()));\n" +
                  "        reqDto.setVers(\"001\");\n" +
                  "        reqDto.setDebuglevel(log.isDebugEnabled() ? 9.0 : 1.0);\n" +
                  "        reqDto.setTransactionid(Sequencer.next());\n" +
                  "        reqDto.setIdapp(\"LGKK\");\n" +
                  "        reqDto.setBearbgrund(\"LGKK\");\n" +
                  "        reqDto.setTraegerid(String.valueOf(req.getZustaendigertraeger()));\n" +
                  "        reqDto.setOrgeinheitid(\"0000000000\");\n" +
                  "        reqDto.setSystemmodus(ElgkkPropertiesUtil.getSystemmodus());\n" +
                  "        reqDto.setVerarbeitungsmodus(\"O\");\n" +
                  "        reqDto.setBerechtpruefung(\"0\");\n" +
                  "        reqDto.setQbid(\"\");\n" +
                  "        reqDto.setAktion(\"U\");\n" +
                  "\n" +
                  "        // Au-/Af-Meldung - Daten\n" +
                  "        reqDto.setLid(0, 0.0);\n" +
                  "        reqDto.setStatus(0, Const.LG_STAT_EINGELANGT);\n" +
                  "\n" +
                  "        Calendar c = null;\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAnstaltbisDat().getTime());\n" +
                  "        reqDto.setAnstaltbisdat(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAnstaltvonDat().getTime());\n" +
                  "        reqDto.setAnstaltvondat(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAubisDat().getTime());\n" +
                  "        reqDto.setAubisdat(0, c);\n" +
                  "\n" +
                  "        // rumpi, 17.12.2009\n" +
                  "        // Vermeidung von Newline im DiagnosetextText\n" +
                  "        // -> Newlines werden durch '-' ersetzt\n" +
                  "        reqDto.setAudiagnosebez(0, req.getAudiagnoseBez());\n" +
                  "        if (req.getAudiagnoseBez() != null) {\n" +
                  "            reqDto.setAudiagnosebez(0, req.getAudiagnoseBez().replace(\"\n\", \"-\"));\n" +
                  "        }\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAusgangbiszeit1().getTime());\n" +
                  "        reqDto.setAusgangbiszeit1(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAusgangvonzeit1().getTime());\n" +
                  "        reqDto.setAusgangvonzeit1(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAusgangbiszeit2().getTime());\n" +
                  "        reqDto.setAusgangbiszeit2(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAusgangvonzeit2().getTime());\n" +
                  "        reqDto.setAusgangvonzeit2(0, c);\n" +
                  "\n" +
                  "        reqDto.setAuslaendischertraeger(0, req.getAuslaendischertraeger());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAusstellungsdatum().getTime());\n" +
                  "        reqDto.setAusstellungsdatum(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getAuvonDat().getTime());\n" +
                  "        reqDto.setAuvondat(0, c);\n" +
                  "\n" +
                  "        reqDto.setBerufskrankheitjn(0, Const.convertLogic(req.isBerufskrankheitJn()));\n" +
                  "        reqDto.setBesondereerkrankungtyp(0, req.getBesondereerkrankungTyp());\n" +
                  "        reqDto.setBestaetigungan(0, req.getBestaetigungan());\n" +
                  "        reqDto.setBettruhejn(0, Const.convertLogic(req.isBettruheJn()));\n" +
                  "        reqDto.setEaumid(0, 0.0);\n" +
                  "        reqDto.setEmail(0, req.getEmail());\n" +
                  "        reqDto.setFallanmerkung(0, req.getFallanmerkung());\n" +
                  "        reqDto.setFamilienname(0, req.getFamilienname());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getGehunfaehigbisDat().getTime());\n" +
                  "        reqDto.setGehunfaehigbisdat(0, c);\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getGipsbisDat().getTime());\n" +
                  "        reqDto.setGipsbisdat(0, c);\n" +
                  "\n" +
                  "        reqDto.setIcddiagnose(0, req.getIcddiagnose().replaceAll(\"\\.\", \"\"));\n" +
                  "        reqDto.setMeldertyp(0, req.getMelderTyp());\n" +
                  "        reqDto.setMeldungsart(0, req.getMeldungsart());\n" +
                  "        reqDto.setMitteilungsanmerkung(0, req.getMitteilungsanmerkung());\n" +
                  "        reqDto.setPostleitzahl(0, req.getPostleitzahl());\n" +
                  "        reqDto.setPostort(0, req.getPostort());\n" +
                  "        reqDto.setRevision(0, req.getRevision());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getSendezeitpunkt().getTime());\n" +
                  "        reqDto.setSendezeitpunkt(0, c);\n" +
                  "\n" +
                  "        reqDto.setStrassehausnummer(0, req.getStrassehausnummer());\n" +
                  "        reqDto.setStromunfalljn(0, Const.convertLogic(req.isStromunfallJn()));\n" +
                  "        reqDto.setTelefonnummer(0, req.getTelefonnummer());\n" +
                  "        reqDto.setVersicherungsnummervertreter(0, req.getVersicherungsnummervertreter());\n" +
                  "        reqDto.setVersicherungsnummer(0, req.getVersicherungsnummer());\n" +
                  "        reqDto.setVertragspartnernummer(0, req.getVertragspartnernummer());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getVoraussichtlauendeDat().getTime());\n" +
                  "        reqDto.setVoraussichtlauendedat(0, c);\n" +
                  "\n" +
                  "        reqDto.setVorname(0, req.getVorname());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getWiederbestellDat().getTime());\n" +
                  "        reqDto.setWiederbestelldat(0, c);\n" +
                  "\n" +
                  "        reqDto.setZusatzicd(0, req.getZusatzicd());\n" +
                  "        reqDto.setZustaendigertraeger(0, req.getZustaendigertraeger());\n" +
                  "\n" +
                  "        // Attribute aus SS V2 ..\n" +
                  "        reqDto.setTicketId(0, req.getTicketId());\n" +
                  "\n" +
                  "        reqDto.setAumid(0, req.getAumId());\n" +
                  "\n" +
                  "        reqDto.setQuelltraeger(0, req.getQuellTraeger());\n" +
                  "\n" +
                  "        c = Calendar.getInstance();\n" +
                  "        c.setTime(req.getGeburtDat().getTime());\n" +
                  "        reqDto.setGeburtdat(0, c);\n" +
                  "\n" +
                  "        reqDto.setRueckdatierunggrund(0, req.getRueckdatierungGrund());\n" +
                  "\n" +
                  "        // Neue Members aus SS 3\n" +
                  "        reqDto.setEkvk(0, req.getEkvk());\n" +
                  "        reqDto.setStaatisoa3(0, req.getStaatIsoA3());\n" +
                  "\n" +
                  "        // Service rufen, asynchron\n" +
                  "        Laaaumv4SvcProxy svcProxy = getSvcProxyV4();\n" +
                  "        svcProxy.setRequest(reqDto);\n" +
                  "\n" +
                  "        // LGKK-13329: IGSM, Pruefung auf leistungszustaendige Landesstelle\n" +
                  "        if (System.getProperty(\"LGKK-13329.ignore\") == null) {\n" +
                  "            QueueHelper hlp = queuesMap.get(reqDto.getZvst());\n" +
                  "            svcProxy.setQueueHelper(hlp);\n" +
                  "        }\n" +
                  "\n" +
                  "        svcProxy.send();\n" +
                  "\n" +
                  "        // XML\n" +
                  "        var dto = map(req);\n" +
                  "        svcProxy.sendXml(dto);\n" +
                  "\n" +
                  "        // Ergebnis des Calls im Rückgabeobjekt setzen\n" +
                  "        ret = new WSResponseDto(Const.SVC_RC_OK, RUECKMELDUNG_TEXT + \", gemeldete Daten waren: \" + req);\n" +
                  "\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }

    private void rewriteRunWithWsdlClasses(SourceSpecs... sourceSpecs) {
        rewriteRun(
            new SourceFileContents().forWsdl2JavaService(sourceSpecs)
        );
    }
}

