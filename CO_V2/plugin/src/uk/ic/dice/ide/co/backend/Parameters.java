package uk.ic.dice.ide.co.backend;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Parameters {

	public enum ParamType {
		BOOLEAN, INTRANGE, CATEGORICAL;
	}

	public class Parameter {
		ParamType type;
		String name;
		String[] node;
		String defvalue;
		String lowerbound;
		String upperbound;
		int step;
		String[] options;
		String description;

		private Parameter() {
		};

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("type: ").append(type);
			sb.append(", name: ").append(name);
			sb.append(", default: ").append(defvalue);
			if (lowerbound != null) {
				sb.append("range: ").append(lowerbound + " to " + upperbound);
			}
			if (options != null) {
				sb.append("options: ");
				for (String o : options) {
					sb.append(o + ", ");
				}
			}
			if (description != null) {
				sb.append("description: ");
				sb.append(description);
			}
			return sb.toString();
		}

		public ParamType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String[] getNode() {
			return node;
		}

		public String getDefvalue() {
			return defvalue;
		}

		public String getLowerbound() {
			return lowerbound;
		}

		public String getUpperbound() {
			return upperbound;
		}

		public String getStep() {
			return String.valueOf(step);
		}

		public String[] getOptions() {
			return options;
		}

		public String getDescription() {
			return description;
		}
	}

	public Parameter[] parseFile(File inputFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getDocumentElement().getChildNodes();
			ArrayList<Parameter> parameters = new ArrayList<Parameter>();
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Parameter p = new Parameter();
					Element eElement = (Element) nNode;
					p.name = eElement.getElementsByTagName("name").item(0).getTextContent();
					NodeList nodes = eElement.getElementsByTagName("node");
					p.node = new String[nodes.getLength()];
					for (int j = 0; j < nodes.getLength(); j++) {
						p.node[j] = nodes.item(j).getTextContent();
					}
					p.defvalue = eElement.getElementsByTagName("default").item(0).getTextContent();
					switch (eElement.getTagName()) {
					case "intparam":
						parameters.add(intparam(p, eElement));
						break;
					case "boolparam":
						parameters.add(boolparam(p, eElement));
						break;
					case "catparam":
						parameters.add(catparam(p, eElement));
						break;
					}
					if (eElement.getElementsByTagName("description").getLength() > 0) {
						p.description = eElement.getElementsByTagName("description").item(0).getTextContent();
					}
				}
			}
			return parameters.toArray(new Parameter[parameters.size()]);
		} catch (Exception e) {
		}
		return null;

	}

	private Parameter catparam(Parameter p, Element eElement) {
		p.type = ParamType.CATEGORICAL;
		NodeList ops = eElement.getElementsByTagName("option");
		p.options = new String[ops.getLength()];
		for (int i = 0; i < ops.getLength(); i++) {
			p.options[i] = ops.item(i).getTextContent();
		}
		return p;
	}

	public Parameter catparam(String name, String[] node, String[] options) {
		Parameter p = new Parameter();
		p.type = ParamType.CATEGORICAL;
		p.node = node;
		p.name = name;
		p.options = options;
		return p;
	}

	private Parameter boolparam(Parameter p, Element eElement) {
		p.type = ParamType.BOOLEAN;
		return p;
	}

	public Parameter boolparam(String name, String[] node) {
		Parameter p = new Parameter();
		p.type = ParamType.BOOLEAN;
		p.name = name;
		p.node = node;
		return p;
	}

	private Parameter intparam(Parameter p, Element eElement) {
		p.type = ParamType.INTRANGE;
		p.lowerbound = eElement.getElementsByTagName("lowerbound").item(0).getTextContent();
		p.upperbound = eElement.getElementsByTagName("upperbound").item(0).getTextContent();
		return p;
	}

	public Parameter intparam(String name, String[] node, int lowerbound, int upperbound, int step) {
		Parameter p = new Parameter();
		p.type = ParamType.INTRANGE;
		p.name = name;
		p.node = node;
		p.step = step;
		p.lowerbound = String.valueOf(lowerbound);
		p.upperbound = String.valueOf(upperbound);
		return p;
	}

}
