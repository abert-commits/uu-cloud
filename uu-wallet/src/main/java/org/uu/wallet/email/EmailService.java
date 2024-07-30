package org.uu.wallet.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.property.ArProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    //服务名称
    private static final String serviceName = "uu-Wallet";

    //从nacos获取配置
    private final ArProperty arProperty;

    //邮件主题
    private static final String emailSubject = "Your " + serviceName + " Verification Code";


    public Boolean sendMimeMessage(String to, String code) {

        log.info("发送邮箱验证码: 邮箱账号: {}, 验证码: {}", to, code.substring(0, 3) + "***");


        String htmlBody = String.format(
                "Dear User,<br><br>" +
                        "Greetings! Thank you for using %s.Your verification code is as follows:<br><br>" +
                        "<strong style='font-size: 16px;'>Verification Code: %s</strong><br><br>" +
                        "Please enter this code within the next %d minutes to complete the verification process.<br><br>" +
                        "If you did not initiate this verification, it might be that another user has mistakenly entered your email address. In that case, please disregard this email.<br><br>" +
                        "Kind Reminders:<br>" +
                        "<ul> <li>Do not share this verification code with anyone.</li> <li>If you encounter any issues while entering the code, please ensure that you enter it exactly as it appears in this email.</li> </ul>",
                serviceName, code, arProperty.getValidityDuration(), serviceName, serviceName);


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置邮件内容为HTML
            message.setContent(htmlBody, "text/html; charset=utf-8");

            helper.setFrom(arProperty.getEmailAccount());
            helper.setTo(to);
            helper.setSubject(emailSubject);
            helper.setText(htmlBody, true); // true 表示发送HTML邮件

            mailSender.send(message);

            log.info("邮箱验证码发送成功: 邮箱账号: {}, 验证码: {}", to, code.substring(0, 3) + "***");

            return Boolean.TRUE;
        } catch (Exception e) {
            // 处理异常
            log.error("邮件发送失败 邮箱账号: {}, 验证码: {}, e :{}", to, code.substring(0, 3) + "***", e);
            return Boolean.FALSE;
        }
    }
}
