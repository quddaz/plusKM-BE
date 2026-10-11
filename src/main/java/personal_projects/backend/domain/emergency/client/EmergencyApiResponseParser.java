package personal_projects.backend.domain.emergency.client;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class EmergencyApiResponseParser {

    public List<EmergencyFacilityData> parse(byte[] response) {
        try {
            Document document = parseDocument(response);
            return parseFacilities(document.getElementsByTagName("item"));
        } catch (Exception exception) {
            throw new IllegalStateException("응급실 API 응답을 읽을 수 없습니다.");
        }
    }

    private Document parseDocument(byte[] response) throws Exception {
        DocumentBuilderFactory factory = createSecureFactory();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response);
        return factory.newDocumentBuilder().parse(inputStream);
    }

    private List<EmergencyFacilityData> parseFacilities(NodeList items) {
        List<EmergencyFacilityData> facilities = new ArrayList<>();
        for (int index = 0; index < items.getLength(); index++) {
            Element item = (Element) items.item(index);
            facilities.add(toFacility(item));
        }
        return List.copyOf(facilities);
    }

    private EmergencyFacilityData toFacility(Element item) {
        return new EmergencyFacilityData(
            requiredValue(item, "hpid"),
            requiredValue(item, "dutyName"),
            requiredValue(item, "dutyAddr"),
            phoneNumber(item),
            coordinate(item, "wgs84Lon"),
            coordinate(item, "wgs84Lat")
        );
    }

    private String phoneNumber(Element item) {
        String emergencyPhoneNumber = optionalValue(item, "dutyTel3");
        if (emergencyPhoneNumber != null) {
            return emergencyPhoneNumber;
        }
        return optionalValue(item, "dutyTel1");
    }

    private double coordinate(Element item, String tagName) {
        return Double.parseDouble(requiredValue(item, tagName));
    }

    private String requiredValue(Element item, String tagName) {
        String value = optionalValue(item, tagName);
        if (value == null) {
            throw new IllegalStateException("응급실 API 필수 값이 없습니다.");
        }
        return value;
    }

    private String optionalValue(Element item, String tagName) {
        NodeList values = item.getElementsByTagName(tagName);
        if (values.getLength() == 0) {
            return null;
        }
        return emptyToNull(values.item(0).getTextContent().trim());
    }

    private String emptyToNull(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    private DocumentBuilderFactory createSecureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }
}
