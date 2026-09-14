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
            DocumentBuilderFactory factory = secureFactory();
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(response));
            NodeList items = document.getElementsByTagName("item");
            List<EmergencyFacilityData> facilities = new ArrayList<>();
            for (int index = 0; index < items.getLength(); index++) {
                facilities.add(toFacility((Element) items.item(index)));
            }
            return List.copyOf(facilities);
        } catch (Exception exception) {
            throw new IllegalStateException("응급실 API 응답을 읽을 수 없습니다.");
        }
    }

    private EmergencyFacilityData toFacility(Element item) {
        String emergencyPhone = optionalValue(item, "dutyTel3");
        String phoneNumber = emergencyPhone == null ? optionalValue(item, "dutyTel1") : emergencyPhone;
        return new EmergencyFacilityData(value(item, "hpid"), value(item, "dutyName"),
            value(item, "dutyAddr"), phoneNumber, Double.parseDouble(value(item, "wgs84Lon")),
            Double.parseDouble(value(item, "wgs84Lat")));
    }

    private String value(Element item, String tagName) {
        String value = optionalValue(item, tagName);
        if (value == null) {
            throw new IllegalStateException("응급실 API 필수 값이 없습니다.");
        }
        return value;
    }

    private String optionalValue(Element item, String tagName) {
        if (item.getElementsByTagName(tagName).getLength() == 0) {
            return null;
        }
        String value = item.getElementsByTagName(tagName).item(0).getTextContent().trim();
        return value.isEmpty() ? null : value;
    }

    private DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }
}
