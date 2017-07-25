package backend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.io.BufferedWriter;


import backend.Parameters.ParamType;
import backend.Parameters.Parameter;
import backend.ServiceBuilder.Service;

public class ExpConfig {
	
	private final String[][] exp;
	private final Collection<Service> services;
	private final String[][] app;
	private final Parameter[] params;
	
	final String spacing = "    ";
	final String namespacing = "  - ";
	
	public ExpConfig(String[][] exp, Collection<Service> services, String[][] app, Parameter[] params) {
		this.exp = exp;
		this.services = services;
		this.app = app;
		this.params = params;
	}

	public File toFile() throws IOException {
		File file = new File("expconfig.yaml");
		if (file.exists()) file.delete();
		file.createNewFile();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		printFields(out, "runexp", exp);
		printServiceFields(out);
		printFields(out, "application", app);
		preprocessParams();
		printParameters(out);
		out.flush();
		out.close();
		return file;
	}
	
	private void printFields(PrintWriter writer, String category, String[][] values) {
		writer.println(category + ':');
		for (String[] line : values) {
			if (line[1] != null && !line[1].isEmpty()) {
				writer.print("  " + line[0] + ": ");
				writer.println(line[1]);
			}
		}
	}
	
	private void printServiceFields(PrintWriter writer) {
		writer.println("services:");
		for (Service s : services) {
			s.printService(writer);
		}

	}
	
	private void printParameters(PrintWriter writer) {
		writer.println("vars:");
		for (Parameter p : params) {
			writer.println(namespacing + "paramname: \"" + p.name + '\"');
			writer.print(spacing + "node: [\"" + p.node[0] + '\"');
			for (int i=1; i<p.node.length; i++) {
				writer.print(", \"" + p.node[i] + '\"');
			}
			writer.println(']');
			if (p.options != null && p.options.length > 0) {
				writer.print(spacing + "options: [" + p.options[0]);
				for (int i=1; i<p.options.length; i++) {
					writer.print(" " + p.options[i]);
				}
				writer.println(']');
			}
			writer.println(spacing + "lowerbound: " + ((p.lowerbound == null)? '0' : p.lowerbound));
			writer.println(spacing + "upperbound: " + ((p.upperbound == null)? '0' : p.upperbound));
			writer.println(spacing + "integer: " + ((p.type == ParamType.INTRANGE)? 1:0));
			writer.println(spacing + "categorical: " + ((p.type == ParamType.CATEGORICAL)? 1:0));
		}
	}
	
	//put options in int params for BO4CO input
	private void preprocessParams() {
		for (Parameter p: params) {
			if (p.type == ParamType.INTRANGE) {
				try {
					int ub = Integer.parseInt(p.upperbound);
					int lb = Integer.parseInt(p.lowerbound);
					int count = 1 + (ub-lb)/p.step + (((ub-lb)%p.step)>0? 1:0);
					String[] options = new String[count];
					int i =0;
					for (int option =lb; option<ub;) {
						options[i] = String.valueOf(option);
						option += p.step;
						i++;
					}
					if (i < options.length) {
						options[i] = p.upperbound;
					}
					p.options = options;
				} catch (NumberFormatException n) {
					System.out.println("cannot process param: " + p.name);
				}				
			}
			if (p.type == ParamType.BOOLEAN) {
				String[] s ={"true", "false"};
				p.options = s;
			}
		}
	}

}
