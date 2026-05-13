package com.devlog.devlog.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Async
    public void send(String email, String code) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[DevLog] 비밀번호 재설정 인증 코드 안내");

            String htmlContent = """
                    <div style="font-family: 'Apple SD Gothic Neo', 'sans-serif'; width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
                        <div style="background-color: #ffffff; padding: 40px; border-radius: 8px; border: 1px solid #eeeeee; text-align: center;">
                            <h1 style="color: #1f7af0; font-size: 40px; font-weight: 800; margin-bottom: 12px;">DevLog</h1>
                            <h2 style="color: #333333; margin-bottom: 20px;">비밀번호 재설정 인증</h2>
                            <p style="color: #666666; font-size: 16px; line-height: 1.5; margin-bottom: 30px;">
                                안녕하세요!<br/>
                                비밀번호 재설정을 위한 인증 코드입니다.<br/>
                                아래의 6자리 숫자를 진행 중인 화면에 입력해 주세요.
                            </p>

                            <!-- 인증 코드 강조 박스 -->
                            <div style="background-color: #f4f6f8; padding: 20px; border-radius: 8px; margin-bottom: 30px;">
                                <span style="font-size: 32px; font-weight: bold; color: #0056b3; letter-spacing: 8px;">%s</span>
                            </div>

                            <p style="color: #999999; font-size: 12px;">
                                본 인증 코드는 앞서 안내드린 대로 10분간 유효합니다.<br/>
                                본인이 요청하지 않은 경우 이 메일을 무시해 주세요.
                            </p>
                        </div>
                    </div>
                    """
                    .formatted(code);

            //메일 본문 설정 (두 번째 인자 true는 본문이 HTML임을 의미함)
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("{} 로 인증코드 이메일 전송 완료", email);

        } catch (MessagingException e) {
            log.error("이메일 발송 중 오류가 발생했습니다: {}", e.getMessage());
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }
}
