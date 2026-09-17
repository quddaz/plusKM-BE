package personal_projects.backend.domain.guardian.sms;

public interface EmergencySmsSender {

    SmsSendResult send(String phoneNumber, String content);
}
