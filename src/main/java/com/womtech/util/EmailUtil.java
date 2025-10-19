package com.womtech.util;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class EmailUtil {

	private final JavaMailSender mailSender;

	@Value("${app.mail.from:${spring.mail.username}}")
	private String fromEmail;

	@Value("${app.mail.fromName:WOMTech}")
	private String fromName;

	public EmailUtil(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendVerifyOtp(String toEmail, String username, String otp, int ttlMinutes) {
		String contentTpl = Template.load("mail/verify-otp.html");
		String content = Template.render(contentTpl,
				Map.of("username", esc(username), "otp", esc(otp), "ttl", String.valueOf(ttlMinutes)));
		String html = wrapInBase("Xác minh email – WOMTech", content, "https://www.facebook.com/jake.nguyen.3762",
				"Trang chủ");
		sendHtml(toEmail, "Xác minh email – WOMTech", html);
	}

	public void sendForgotPassword(String toEmail, String username, String otp, int ttlMinutes,
			@Nullable String resetLink) {
		String contentTpl = Template.load("mail/forgot-password.html");
		String content = Template.render(contentTpl, Map.of("username", esc(username), "otp", esc(otp), "ttl",
				String.valueOf(ttlMinutes), "resetLink", nvl(resetLink)));
		String html = wrapInBase("Khôi phục mật khẩu – WOMTech", content, nvl(resetLink),
				nvl(resetLink).isBlank() ? "" : "Đặt lại mật khẩu");
		sendHtml(toEmail, "Khôi phục mật khẩu – WOMTech", html);
	}

	public void sendWelcome(String toEmail, String username, @Nullable String ctaUrl) {
		String contentTpl = Template.load("mail/welcome.html");
		String content = Template.render(contentTpl, Map.of("username", esc(username)));
		String html = wrapInBase("Chào mừng đến với WOMTech!", content, nvl(ctaUrl),
				nvl(ctaUrl).isBlank() ? "" : "Khám phá ngay");
		sendHtml(toEmail, "Chào mừng đến với WOMTech!", html);
	}

	public void sendOrderConfirmation(String toEmail, String username, String orderCode, List<OrderLine> items,
			double totalAmount, @Nullable String deliveryEstimate, @Nullable String trackUrl) {

		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));

		String itemsTable;
		if (items == null || items.isEmpty()) {
			itemsTable = "<tr><td colspan='3' style='padding:8px;border:1px solid #e5e7eb'>Không có sản phẩm</td></tr>";
		} else {
			itemsTable = items.stream().map((OrderLine i) -> String.format("<tr>%s%s%s</tr>", td(esc(i.name())),
					td("x" + i.quantity()), td(nf.format(i.price())))).collect(Collectors.joining());
		}

		String contentTpl = Template.load("mail/order-confirmation.html");
		String content = Template.render(contentTpl, Map.of("username", esc(username), "orderCode", esc(orderCode),
				"deliveryEstimate", nvl(deliveryEstimate), "itemsTable", itemsTable, "total", nf.format(totalAmount)));

		String html = wrapInBase("Xác nhận đơn hàng – WOMTech", content, nvl(trackUrl),
				nvl(trackUrl).isBlank() ? "" : "Theo dõi đơn hàng");
		sendHtml(toEmail, "Xác nhận đơn hàng #" + orderCode, html);
	}

	public void sendPromotion(String toEmail, String title, String message, @Nullable String promoCode,
			@Nullable String ctaUrl) {

		String contentTpl = Template.load("mail/promotion.html");
		String content = Template.render(contentTpl,
				Map.of("title", esc(title), "message", esc(message), "promoCode", nvl(promoCode)));

		String html = wrapInBase(title, content, nvl(ctaUrl), nvl(ctaUrl).isBlank() ? "" : "Mua ngay");
		sendHtml(toEmail, title, html);
	}

	private void sendHtml(String toEmail, String subject, String html) {
		try {
			MimeMessage mm = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mm, true, StandardCharsets.UTF_8.name());
			helper.setFrom(new InternetAddress(fromEmail, fromName));
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(html, true);

			if (html.contains("cid:appstoreBadge") || html.contains("cid:googlePlayBadge")) {
				var appstore = new org.springframework.core.io.ClassPathResource("mail/img/appstore.png");
				var gplay = new org.springframework.core.io.ClassPathResource("mail/img/googleplay.png");
				if (appstore.exists())
					helper.addInline("appstoreBadge", appstore, "image/png");
				if (gplay.exists())
					helper.addInline("googlePlayBadge", gplay, "image/png");
			}

			mailSender.send(mm);
		} catch (Exception e) {
			throw new RuntimeException("Send mail failed: " + e.getMessage(), e);
		}
	}

	private String wrapInBase(String title, String content, String buttonUrl, String buttonText) {
		String base = Template.load("mail/base.html");
		return Template.render(base, Map.of("TITLE", esc(title), "CONTENT", content, "BUTTON_URL", esc(buttonUrl),
				"BUTTON_TEXT", esc(buttonText)));
	}

	private static String esc(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&#39;");
	}

	private static String nvl(String s) {
		return s == null ? "" : s;
	}

	private static String td(String v) {
		return "<td style='padding:8px;border:1px solid #e5e7eb'>" + v + "</td>";
	}

	public record OrderLine(String name, int quantity, double price) {
	}

	public record Attachment(String filename, org.springframework.core.io.Resource resource) {
	}

	static class Template {
		static String load(String classpathPath) {
			try (var in = EmailUtil.class.getClassLoader().getResourceAsStream(classpathPath)) {
				if (in == null)
					throw new IllegalArgumentException("Template not found: " + classpathPath);
				return new String(in.readAllBytes(), StandardCharsets.UTF_8);
			} catch (Exception e) {
				throw new RuntimeException("Failed to read template: " + classpathPath, e);
			}
		}

		static String render(String template, Map<String, String> vars) {
			String html = template;
			for (var e : vars.entrySet()) {
				html = html.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
			}

			html = html.replace("${BUTTON_URL}", "").replace("${BUTTON_TEXT}", "");
			return html;
		}
	}
}