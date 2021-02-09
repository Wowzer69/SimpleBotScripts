package simple.api.panel.options;

public class WebhookMessage {
	public String content;

	public String username;

	public String avatar_url;

	public boolean tts;

	public Object file;

	public Object[] embeds;

	public WebhookMessage(String username, String content) {
		this.content = content;
		this.username = username;
	}
}
