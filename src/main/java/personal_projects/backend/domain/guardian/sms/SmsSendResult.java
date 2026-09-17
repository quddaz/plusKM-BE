package personal_projects.backend.domain.guardian.sms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SmsSendResult(String requestId, String statusCode, String statusName) {
}
