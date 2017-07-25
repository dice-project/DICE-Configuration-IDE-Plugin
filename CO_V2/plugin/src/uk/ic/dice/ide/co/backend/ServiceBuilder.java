package uk.ic.dice.ide.co.backend;

import java.io.PrintWriter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ServiceBuilder {

	private Service s;

	public ServiceBuilder(Text servicename) {
		s = new Service(servicename);
	}

	public Service withURL(Text URL) {
		s.URL = URL;
		return s;
	}

	public Service withIP(Text ip) {
		s.ip = ip;
		return s;
	}

	public Service withContainer(Text container) {
		s.container = container;
		return s;
	}

	public Service withUsername(Text username) {
		s.username = username;
		return s;
	}

	public Service withPassword(Text password) {
		s.password = password;
		return s;
	}

	public Service withTools(Text tools) {
		s.tools = tools;
		return s;
	}

	public Service withStorm(Text storm_client) {
		s.storm_client = storm_client;
		return s;
	}

	public Service add(int i, Text t) {
		switch (i) {
		case 1:
			return withURL(t);
		case 2:
			return withIP(t);
		case 3:
			return withContainer(t);
		case 4:
			return withUsername(t);
		case 5:
			return withPassword(t);
		case 6:
			return withTools(t);
		case 7:
			return withStorm(t);
		}
		return s;
	}

	public Service build(Composite c) {
		s.c = c;
		return s;
	}

	public class Service {

		private static final String namespacing = "  - ";
		private static final String spacing = "    ";

		private Text servicename;
		private Text URL;
		private Text ip;
		private Text container;
		private Text username;
		private Text password;
		private Text tools;
		private Text storm_client;

		private Composite c;

		private Service(Text servicename) {
			this.servicename = servicename;
		}

		public boolean hasComposite(Composite c) {
			return this.c == c;
		}

		public Composite getComposite() {
			return c;
		}

		public void printService(PrintWriter writer) {
			writer.print(namespacing + "servicename" + ": ");
			writer.println("\"" + servicename.getText() + "\"");
			if (URL != null) {
				writer.print(spacing + "URL" + ": ");
				writer.println("\"" + URL.getText() + "\"");
			}
			if (ip != null) {
				writer.print(spacing + "ip" + ": ");
				writer.println("\"" + ip.getText() + "\"");
			}
			if (container != null) {
				writer.print(spacing + "container" + ": ");
				writer.println("\"" + container.getText() + "\"");
			}
			if (username != null) {
				writer.print(spacing + "username" + ": ");
				writer.println("\"" + username.getText() + "\"");
			}
			if (password != null) {
				writer.print(spacing + "password" + ": ");
				writer.println("\"" + password.getText() + "\"");
			}
			if (tools != null) {
				writer.print(spacing + "tools" + ": ");
				writer.println("\"" + tools.getText() + "\"");
			}
			if (storm_client != null) {
				writer.print(spacing + "storm_client" + ": ");
				writer.println("\"" + storm_client.getText() + "\"");
			}

		}

		public String serialise() {
			StringBuilder sb = new StringBuilder("servicename>>");
			sb.append(servicename.getText());
			if (URL != null && !URL.getText().isEmpty()) {
				sb.append(",URL>>");
				sb.append(URL.getText());
			}
			if (ip != null && !ip.getText().isEmpty()) {
				sb.append(",ip>>");
				sb.append(ip.getText());
			}
			if (container != null && !container.getText().isEmpty()) {
				sb.append(",container>>");
				sb.append(container.getText());
			}
			if (username != null && !username.getText().isEmpty()) {
				sb.append(",username>>");
				sb.append(username.getText());
			}
			if (password != null && !password.getText().isEmpty()) {
				sb.append(",password>>");
				sb.append(password.getText());
			}
			if (tools != null && !tools.getText().isEmpty()) {
				sb.append(",tools>>");
				sb.append(tools.getText());
			}
			if (storm_client != null && !storm_client.getText().isEmpty()) {
				sb.append(",storm_client>>");
				sb.append(storm_client.getText());
			}
			return sb.toString();
		}
	}

}
