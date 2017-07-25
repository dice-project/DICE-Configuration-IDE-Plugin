package uk.ic.dice.ide.co.ui;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//http://stackoverflow.com/questions/31673121/how-to-add-a-checkbox-in-a-combodrop-down-in-java-swt

public class MultiSelectionCombo extends Composite {

	private Shell shell;
	private Text display;
	private String[] items;
	private Button[] buttons;
	private int[] currentSelection;
	private GridLayout layout;

	private TableItem listener;

	public MultiSelectionCombo(Composite parent, String[] items, int[] defaultselection, int style) {
		super(parent, style);
		currentSelection = defaultselection;
		this.items = items;
		init();
	}

	private void init() {
		layout = new GridLayout();
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		display = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		display.setLayoutData(new GridData(GridData.FILL_BOTH));

		displayText();

		display.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent event) {
				super.mouseDown(event);
				initFloatShell();
			}

		});
	}

	private void initFloatShell() {
		Point p = display.getParent().toDisplay(display.getLocation());
		Point size = display.getSize();
		Rectangle shellRect = new Rectangle(p.x, p.y + size.y, size.x, 0);
		shell = new Shell(MultiSelectionCombo.this.getShell(), SWT.NO_TRIM);

		shell.setLayout(layout);

		buttons = new Button[items.length];
		for (int i = 0; i < items.length; i++) {
			buttons[i] = new Button(shell, SWT.CHECK);
			buttons[i].setText(items[i]);
		}
		for (int i = 0; i < currentSelection.length; i++) {
			buttons[currentSelection[i]].setSelection(true);
		}

		shell.setSize(shellRect.width, 30 * buttons.length);
		shell.setLocation(shellRect.x, shellRect.y);

		shell.addShellListener(new ShellAdapter() {

			public void shellDeactivated(ShellEvent arg0) {
				saveNCloseShell();
			}
		});

		shell.open();
	}

	protected void saveNCloseShell() {
		if (shell != null && !shell.isDisposed()) {
			currentSelection = selected();
			displayText();
			shell.dispose();
			disposebuttons();
		}
	}

	private void displayText() {
		if (currentSelection != null && currentSelection.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < currentSelection.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(items[currentSelection[i]]);
			}
			display.setText(sb.toString());
			listener.setText(5, sb.toString());
		} else {
			display.setText("");
		}
	}

	public int[] getSelections() {
		return this.currentSelection;
	}

	private int[] selected() {
		LinkedList<Integer> selected = new LinkedList<Integer>();
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].getSelection())
				selected.add(i);
		}
		int[] array = new int[selected.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = selected.get(i);
		}
		return array;
	}

	private void disposebuttons() {
		for (Button b : buttons) {
			b.dispose();
		}
	}

	public void addSelectionListener(TableItem item) {
		listener = item;
	}

}
