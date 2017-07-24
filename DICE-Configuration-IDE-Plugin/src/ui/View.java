package ui;

import org.eclipse.ui.part.*;

import backend.ExpConfig;
import backend.Parameters;
import backend.Parameters.Parameter;
import backend.ServiceBuilder;
import backend.ServiceBuilder.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */


public class View extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "DICE-Configuration-IDE-Plugin.ui.DICE-Configuration-IDE-Plugin";
	
	// CHANGE THIS!!!
	private File paramfile = new File("/DICE-Configuration-IDE-Plugin/src/backend/params.xml");
	private TabFolder tabFolder;
	
	// maps for parameters and the tables, controls in tab
	private HashMap<TableItem, itemControls> map = new HashMap<TableItem, itemControls>();
	private HashMap<TableItem, Parameter> paramtableitem = new HashMap<TableItem, Parameter>();
	private String showingParams;
	private Parameter[] params;
	
	// selected parameters
	private Table selected;
	
	// save and load settings
	private Button save;
	private Button load;
	
	// exp configs
	private Text[] exp;
	private final String[] expFields = {"noise", "numIter", "initialDesign",
			"saveFolder", "confFolder", "summaryFolder", "blueprint", "conf",
			"topic", "sleep_time", "metricPoll", "expTime", "replication"};
	private final String[] displayExpFields = {"Noise", "Number of Iterations", "Initial Design",
			"Save Folder", "Config Folder", "Summary Folder", "Blueprint", "Config",
			"Topic", "Sleep Time", "Metric Poll", "Exp Time", "Replication"};
	
	//app configs
	private Text[] app;
	private final String[] appFields = {"cli_file", "jar_file", "jar_path", "class", "name", "args", "type"};
	private final String[] displayAppFields = {"CLI file", "Jar File", "Jar Path", "Class", "Name", "Args", "Type"};
	private Combo type;
	
	// service configs
	private ArrayList<Service> services = new ArrayList<Service>(10);	
	private final String[] serviceFields = {"servicename", "URL", "ip", "container", "username", "password", "tools", "storm_client"};
	
	// class itemControl represents a row of controls in the selected param table
	class itemControls {
		Control c;
		TableEditor e;
		itemControls(TableEditor e, Control c) {
			this.e = e;
			this.c = c;
		}
		public void dispose() {
			e.dispose();
			c.dispose();
		}
	}
	 

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

/**
	 * The constructor.
	 */
	public View() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		//get saved configs 
		backend.Save.loadFile();
		
		parent.setLayout(new GridLayout());
		
		Group[] groups = new Group[6];
		String[] tabnames = {"Plugin Config", "Parameter Selection", "Service Config", "Experiment Config", "App Config", "Experiments"};
		
		tabFolder = new TabFolder (parent, SWT.BORDER);		
		
		for (int i=0; i<tabnames.length; i++) {
			TabItem item = new TabItem (tabFolder, SWT.NONE);
			item.setText (tabnames[i]);
			Group group = new Group(tabFolder, SWT.SHADOW_NONE);
			group.setLayout(new GridLayout());
			groups[i] = group;
			Button button = new Button (tabFolder, SWT.PUSH);	
			button.setText ("Page " + i);
			item.setControl (group);
		}
		
		Group tab0 = groups[0];
		createPluginTab(tab0);
		
		Group tab1 = groups[1];
		createParamsTab(tab1);

		Group tab2 = groups[2];
		createServicesTab(tab2);
		
		Group tab3 = groups[3];
		createExpTab(tab3);
		
		Group tab4 = groups[4];
		createAppTab(tab4);
		
		Group tab5 = groups[5];
		createResultTab(tab5);
		
		tabFolder.pack ();
	}

	private void createPluginTab(Group tab0) {
		save = new Button(tab0, SWT.BUTTON1);
		save.setText("Save current configurations");
		save.addListener(SWT.MouseUp, e-> {
			backend.Save.write();
		});
		load = new Button(tab0, SWT.BUTTON1);
		load.setText("Load saved configurations");
		
		Group jenkins = new Group(tab0, SWT.NONE);
		Composite jcompo = new Composite(jenkins, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 2;
		jcompo.setLayout(g);
		
		Text username = addField("Username", jcompo);
		username.addListener(SWT.Modify, e -> {
			backend.Jenkins.setUsername(username.getText());
		});
		
		Text password = addField("Password", jcompo);
		password.addListener(SWT.Modify, e -> {
			backend.Jenkins.setPw(password.getText());
		});
		Text jenkinsUrl = addField("Jenkins Url", jcompo);
		jenkinsUrl.addListener(SWT.Modify, e -> {
			backend.Jenkins.setJen(jenkinsUrl.getText());
		});
		Text jobName = addField("Job Name", jcompo);
		jobName.addListener(SWT.Modify, e -> {
			backend.Jenkins.setJob(jobName.getText());
		});
		Text token = addField("Build Token", jcompo);
		token.addListener(SWT.Modify, e -> {
			backend.Jenkins.setToken(token.getText());
		});
		
		jcompo.pack();
	}

	private void createParamsTab(Group tab1) {
		Combo combo = new Combo(tab1, SWT.NONE);
		
		Table selectiontable = new Table (tab1, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		
		Button select = new Button(tab1, SWT.BUTTON1);
		select.setText("Add Parameters");
		select.addListener(SWT.MouseDown, event -> {
			makeSelections(selectiontable);
		});
		
		selected = new Table (tab1, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		
		Button deselect = new Button(tab1, SWT.BUTTON1);
		
		String[] items = {"hadoop", "storm", "spark"};
		combo.setItems(items);
		combo.select(0);
		showingParams = combo.getItem(0);
		combo.addListener(SWT.Selection, event -> {
			String selected = combo.getItem(combo.getSelectionIndex());
			if (!selected.equals(showingParams)) {
				showingParams = selected;
				removeParams(selectiontable);
				showParams(selectiontable);
			}
		});
		
		setupSelectionTable(selectiontable);

		setupSelectedTable(selected);
		
		makeTableEditable(selected);
		
		
		deselect.setText("Remove Parameters");
		deselect.addListener(SWT.MouseDown, event -> {
			removeSelections(selected, selectiontable);
		});
		
	}
	
	private void removeParams(Table selectiontable) {
		for (itemControls i :map.values()) {
			i.dispose();
		}
		map.clear();
		selected.removeAll();
		selectiontable.removeAll();
		paramtableitem.clear();
		
	}

	private void showParams(Table selectiontable) {
		for (int i=0; i<params.length; i++) {
			boolean show = false;
			for (int j=0; j<params[i].getNode().length; j++) {
				show = show || (params[i].getNode())[j].equals(showingParams);
			}
			if (show) {
				TableItem item = new TableItem (selectiontable, SWT.NONE);
				item.setText (0, params[i].getName());
				if (params[i].getDescription() != null) {
					item.setText (1, params[i].getDescription());
				}
				paramtableitem.put(item, params[i]);
			}
		}
	}
	
	

	private void setupSelectionTable(Table selectiontable) {
		selectiontable.setLinesVisible (true);
		selectiontable.setHeaderVisible (true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.heightHint = 200;
		selectiontable.setLayoutData(data);
		TableColumn column = new TableColumn (selectiontable, SWT.NONE);
		column.setText("Parameter                                         ");
		TableColumn column2 = new TableColumn (selectiontable, SWT.NONE);
		column2.setText("Description");
		Parameters p = new Parameters();
		params = p.parseFile(paramfile);
		showParams(selectiontable);
		selectiontable.addListener (SWT.MouseDoubleClick, e -> {
			makeSelections(selectiontable);
		});

		for (int i=0; i<selectiontable.getColumnCount(); i++) {
			selectiontable.getColumn (i).pack ();
		}
	}
	
	private void makeSelections(Table selectiontable) {
		TableItem [] selection = selectiontable.getSelection ();
		for (int i=0; i<selection.length; i++) {
			createSelection(selection[i], selected);
		}
		selectiontable.remove(selectiontable.getSelectionIndices());
	}
	
	private void setupSelectedTable(Table selected) {
		selected.setLinesVisible (true);
		selected.setHeaderVisible (true);
		GridData selecteddata = new GridData(SWT.FILL, SWT.FILL, false, false);
		selecteddata.heightHint = 250;
		selected.setLayoutData(selecteddata);
		String[] selectedtitles = {"Parameter                                                                           ", "Type         ", "Min   ", "Max   ", "Step", "Options                                           "};
		for (int i=0; i<selectedtitles.length; i++) {
			TableColumn column = new TableColumn (selected, SWT.NONE);
			column.setText (selectedtitles [i]);
		}

		for (int i=0; i<selected.getColumnCount(); i++) {
			selected.getColumn (i).pack ();
		}
	}
	
	private void removeSelections(Table selected, Table selectiontable) {
		TableItem [] selection = selected.getSelection ();
		for (int i=0; i<selection.length; i++) {
			TableItem item = new TableItem (selectiontable, SWT.NONE);
			Parameter param = paramtableitem.get(selection[i]);
			item.setText(param.getName());
			if (map.containsKey(selection[i])) {
				map.remove(selection[i]).dispose();
			}
			paramtableitem.remove(selection[i]);
			paramtableitem.put(item, param);
		}
		selected.remove(selected.getSelectionIndices());
	}

	private void createSelection(TableItem selection, Table selected) {
		TableItem item = new TableItem (selected, SWT.NONE);
		Parameter p = paramtableitem.remove(selection);
		item.setText(0, p.getName());
		switch (p.getType()) {
		case BOOLEAN:
			item.setText(1, "Boolean");
			break;
		case CATEGORICAL:
			item.setText(1, "Categorical");
			TableEditor editor = new TableEditor (selected);
			MultiSelectionCombo combo = new MultiSelectionCombo(selected, p.getOptions(), new int[0], SWT.NONE);
			combo.addSelectionListener(item);
			editor.grabHorizontal = true;
			editor.setEditor(combo, item, 5);
			map.put(item, new itemControls(editor, combo));
			break;
		case INTRANGE:
			item.setText(1, "Integer");
			item.setText(2, p.getLowerbound());
			item.setText(3, p.getUpperbound());
			item.setText(4, "1");
			break;
		default:
			assert false;
		}
	
		paramtableitem.put(item, p);
	}
	
	private void createServicesTab(Group tab2) {
		
		ScrolledComposite sc = new ScrolledComposite(tab2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		Composite mask = new Composite(sc, SWT.NONE);
		mask.setLayout(new GridLayout());
		boolean[] ops = {true, true, false, true, true, true, true, false};
		sc.setContent(mask);
		addService(mask, ops);
		
		Group options = new Group(tab2, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 4;
		options.setLayout(g);
		Button[] checks = new Button[serviceFields.length];
		for (int i=0;i < serviceFields.length; i++) {
			checks[i] = new Button(options, SWT.CHECK);
			checks[i].setText(serviceFields[i]);
			checks[i].setSelection(true);
		}
		
		Button add = new Button(tab2, SWT.BUTTON1);
		add.setText("Add Service");
		add.addListener(SWT.MouseDown, event -> {
			for (int i =0; i < serviceFields.length; i++) {
				ops[i] = checks[i].getSelection();
			}
			addService(mask, ops);
		});
		save.addListener(SWT.MouseDown, e->{
			for (int i =0; i < services.size(); i++) {
				Service ser = services.get(i);
				String s = ser.serialise();
				backend.Save.saveProperty("Service"+i, s);
			}
		});
		load.addListener(SWT.MouseDown, e->{
			// clear services
			for (Service s : services) {
				s.getComposite().dispose();
			}
			services.clear();
			mask.pack();
			
			boolean more = true;
			int i=0;
			while (more) {
				String value = backend.Save.loadProperty("Service"+i);
				if (!value.isEmpty()) {
					addService(mask, value.split(","));
				} else {
					more = false;
				}
				i++;
			}
		});
			
	}
	
	private void addService(Composite parent, String[] fields) {
		Composite c = new Composite(parent, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 2;
		c.setLayout(g);
		
		ServiceBuilder sb = null;
		int i = 0;
		for (String serv : fields) {
			String k = serv.split(">>")[0];
			String v = serv.split(">>")[1];
			while (!k.equals(serviceFields[i])){
				i++;
			}
			Label label = new Label(c, SWT.NULL);
			label.setText(serviceFields[i] + ": ");
		    Text field = new Text(c, SWT.SINGLE | SWT.BORDER);
		    GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		    gridData.widthHint = 500;
		    field.setLayoutData(gridData);
			field.setSize(500, 50);
			field.setText(v);
			if (i ==0) {
			sb = new ServiceBuilder(field);
			} else {
			sb.add(i, field);
			}

		}
		services.add(sb.build(c));
		
		Button remove = new Button(c, SWT.BUTTON1);
		remove.setText("Remove");
		remove.addListener(SWT.MouseDown, event -> {
			int index = 0;
			while (index < services.size()) {
				if (services.get(index).hasComposite(c))
					break;
				else index ++;
			}
			services.remove(index);
			c.dispose();
			parent.pack();
		});
		c.pack();
		parent.pack();		
	}

	private void addService(Composite parent, boolean[] options) {
		assert options.length == serviceFields.length;
		Composite c = new Composite(parent, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 2;
		c.setLayout(g);
		
		ServiceBuilder sb = null;
		for (int i=0; i < options.length; i++) {
			if (options[i]) {
				Label label = new Label(c, SWT.NULL);
			    label.setText(serviceFields[i] + ": ");

			    Text field = new Text(c, SWT.SINGLE | SWT.BORDER);
			    GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
			    gridData.widthHint = 500;
			    field.setLayoutData(gridData);
			    field.setSize(500, 50);
			    if (i ==0) {
			    	sb = new ServiceBuilder(field);
			    } else {
			    	sb.add(i, field);
			    }

			}}
		services.add(sb.build(c));
		
		Button remove = new Button(c, SWT.BUTTON1);
		remove.setText("Remove");
		remove.addListener(SWT.MouseDown, event -> {
			int index = 0;
			while (index < services.size()) {
				if (services.get(index).hasComposite(c))
					break;
				else index ++;
			}
			services.remove(index);
			c.dispose();
			parent.pack();
		});
		c.pack();
		parent.pack();
	}
	
	
	private Text addField(String s, Composite c) {
		Label label = new Label(c, SWT.NULL);
		label.setText(s + ": ");
		Text field = new Text(c, SWT.SINGLE | SWT.BORDER);
	    GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
	    gridData.widthHint = 500;
	    field.setLayoutData(gridData);
		save.addListener(SWT.MouseDown, e->{
			if (!field.getText().isEmpty()) {
				backend.Save.saveProperty(s, field.getText());
			}
		});
		load.addListener(SWT.MouseDown, e->{
			field.setText(backend.Save.loadProperty(s));
		});
	    return field;
	}
	
	private void createExpTab(Group tab3) {
		Composite expcompo = new Composite(tab3, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 2;
		expcompo.setLayout(g);
		
		assert displayExpFields.length == expFields.length;
		exp = new Text[displayExpFields.length];
		for (int i=0; i < displayExpFields.length; i++) {
			exp[i] = addField(displayExpFields[i], expcompo);
		}
		expcompo.pack();
	
	}
	
	private void createAppTab(Group tab4) {
		Composite appcompo = new Composite(tab4, SWT.BORDER);
		GridLayout g = new GridLayout();
		g.numColumns = 2;
		appcompo.setLayout(g);
		
		app = new Text[displayAppFields.length-1];
		for (int i=0; i < displayAppFields.length-1; i++) {
			app[i] = addField(displayAppFields[i], appcompo);
		}
		
		Label label = new Label(appcompo, SWT.NULL);
		label.setText(displayAppFields[displayAppFields.length-1] + ": ");		
		type = new Combo(appcompo, SWT.NONE);
		type.setItems("hadoop", "storm", "spark");
		appcompo.pack();
	
	}
	
	private void createResultTab(Group tab4) {
		tab4.setLayout(new GridLayout(2, false));
		
		Button run = new Button(tab4, SWT.BUTTON1);
		run.setText("Run BO4CO");
		GridData d = new GridData();
		d.horizontalSpan = 2;
		run.setLayoutData(d);
		
		run.addListener(SWT.MouseDown, event -> {
			run();
		});
		
		Button check = new Button(tab4, SWT.BUTTON1);
		check.setText("Check Status");
		Text status = new Text(tab4, SWT.BORDER);
		status.setText("Unknown");
		check.addListener(SWT.MouseDown, e->{
			status.setText((backend.Jenkins.checkFinished())? "Running" : "Finished");
		});
		
		Button results = new Button(tab4, SWT.BUTTON1);
		results.setText("Retrieve Results");
		results.setLayoutData(d);
		
		Table table = new Table(tab4, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.heightHint = 200;
		table.setLayoutData(data);
		TableColumn column = new TableColumn (table, SWT.NONE);
		column.setText("Parameter                                         ");
		TableColumn column2 = new TableColumn (table, SWT.NONE);
		column2.setText("Optimum value");
		table.setVisible(false);
		results.addListener(SWT.MouseDown, event -> {
			displayOutput(table);
		});
		
	}

	private void displayOutput(Table table) {
		for (TableItem item : table.getItems()) {
			item.dispose();
		}

		String[] outputs = backend.Jenkins.checkOutput();
		for (String output : outputs) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, output.split(", ")[0]);
			item.setText(1, output.split(", ")[1]);
		}
		for (int i=0; i<table.getColumnCount(); i++) {
			table.getColumn (i).pack ();
		}
		table.pack();
		table.setVisible(true);
	}
	
	private void makeTableEditable(Table table) {
		final TableEditor editor = new TableEditor (table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		table.addListener (SWT.MouseDoubleClick, event -> {
			Rectangle clientArea2 = table.getClientArea ();
			Point pt = new Point (event.x, event.y);
			int index = table.getTopIndex ();
			while (index < table.getItemCount ()) {
				boolean visible = false;
				final TableItem item = table.getItem (index);

				for (int i=0; i<table.getColumnCount (); i++) {
					Rectangle rect = item.getBounds (i);
					if (rect.contains (pt)) {
						final int column = i;
						final Text text = new Text (table, SWT.NONE);
						Listener textListener = e -> {
							switch (e.type) {
								case SWT.FocusOut:
									item.setText (column, text.getText ());
									text.dispose ();
									break;
								case SWT.Traverse:
									switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText (column, text.getText ());
											//FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose ();
											e.doit = false;
									}
									break;
							}
						};
						text.addListener (SWT.FocusOut, textListener);
						text.addListener (SWT.Traverse, textListener);
						editor.setEditor (text, item, i);
						text.setText (item.getText (i));
						text.selectAll ();
						text.setFocus ();
						return;
					}
					if (!visible && rect.intersects (clientArea2)) {
						visible = true;
					}
				}
				if (!visible) return;
				index++;
			}
		});		
	}
	
	private Parameter[] collectParams() {
		Parameter[] params = new Parameter[selected.getItemCount()];
		Parameters p = new Parameters();
		for (int i=0; i<selected.getItemCount(); i++) {
			TableItem t = selected.getItem(i);
			Parameter original = paramtableitem.get(t);
			switch (t.getText(1)) {
			case "Boolean":
				params[i] = p.boolparam(t.getText(0), original.getNode());
				break;
			case "Categorical":
				params[i] = p.catparam(t.getText(0), original.getNode(), t.getText(5).split(","));
				break;
			case "Integer":
				try {
					params[i] = p.intparam(t.getText(0), original.getNode(),
							Integer.parseInt(t.getText(2)), 
							Integer.parseInt(t.getText(3)), 
							Integer.parseInt(t.getText(4)));
				} catch (NumberFormatException e) {
					System.out.println("Unexpected user input for parameter: " + t.getText(0));
				}
				break;
			default:
				assert false;
			}
		}
		return params;
	}
	
	private String[][] collectTable(String[] fields, Text[] input) {
		String[][] config = new String[fields.length][2];

		for (int i=0; i<input.length; i++) {
			String[] s = {fields[i], input[i].getText()};
			config[i] = s;
		}
		return config;
	}
	
	private void run() {
		String[][] appConfig = collectTable(appFields, app);
		String[] s = {appFields[appFields.length-1], type.getText()};
		appConfig[appFields.length-1] = s;
		
		ExpConfig c = new ExpConfig(collectTable(expFields, exp), services, appConfig, collectParams());
		try {
			File expconfig = c.toFile();
			backend.Jenkins.request(expconfig);
		} catch (IOException e) {
			System.out.println("Cannot create config file expconfig.yaml");
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		tabFolder.setFocus();
	}	
}
