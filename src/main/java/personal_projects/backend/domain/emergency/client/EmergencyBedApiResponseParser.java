package personal_projects.backend.domain.emergency.client;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class EmergencyBedApiResponseParser {

    public List<EmergencyBedData> parse(byte[] response) {
        try {
            DocumentBuilderFactory factory = createSecureFactory();
            NodeList items = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(response))
                .getElementsByTagName("item");
            return parseItems(items);
        } catch (Exception exception) {
            throw new IllegalStateException("응급실 병상 API 응답을 읽을 수 없습니다.", exception);
        }
    }

    private List<EmergencyBedData> parseItems(NodeList items) {
        List<EmergencyBedData> beds = new ArrayList<>();
        for (int index = 0; index < items.getLength(); index++) {
            Element item = (Element) items.item(index);
            beds.add(new EmergencyBedData(
                requiredValue(item, "hpid"),
                bedCount(item, "hvec"),
                bedCount(item, "hvoc"),
                bedCount(item, "hvicc"),
                bedCount(item, "hvgc"),
                optionalValue(item, "hvidate")
            ));
        }
        return List.copyOf(beds);
    }

    private Integer bedCount(Element item, String tagName) {
        String value = optionalValue(item, tagName);
        if (value == null) {
            return null;
        }
        int count = Integer.parseInt(value);
        return count >= 0 ? count : null;
    }

    private String requiredValue(Element item, String tagName) {
        String value = optionalValue(item, tagName);
        if (value == null) {
            throw new IllegalStateException("응급실 병상 API 필수 값이 없습니다.");
        }
        return value;
    }

    private String optionalValue(Element item, String tagName) {
        NodeList values = item.getElementsByTagName(tagName);
        if (values.getLength() == 0) {
            return null;
        }
        String value = values.item(0).getTextContent().trim();
        return value.isEmpty() ? null : value;
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
