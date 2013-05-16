package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;

import javax.swing.JComboBox;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class DropdownCell<T> extends EmptyCell {

	private static final long serialVersionUID = 8492943126183666582L;
	
	//private static final Logger log = LoggerFactory.getLogger(DropdownCell.class);

	private JComboBox<T> dropdown;


	public DropdownCell() {
		this(new JComboBox<T>());
		initDropdown();
	}
	
	/**
	 * Initializes the cell with a {@link JComboBox} filled with array 
	 * argument. Label for the content is the object's toString method.
	 * 
	 * @param content array of elements to fill the dropdown
	 */
	public DropdownCell(T[] content) {
		this(new JComboBox<T>());
		initDropdown();
		initContent(content);
	}
	
	private void initDropdown() {
		dropdown.setFont(getFont(dropdown.getFont().getName()));
	}
	
	/**
	 * @param dropdown
	 * @throws IllegalArgumentException if dropdown is null
	 */
	public DropdownCell(JComboBox<T> dropdown) {
		super();
		setLayout(new BorderLayout());
		if(dropdown != null) {
			this.dropdown = dropdown;
			add(dropdown, BorderLayout.CENTER);
		}
		else
			throw new IllegalArgumentException("null dropdown");
	}
	
	private void initContent(T[] content) {
		if(content != null) {
			for(T c : content) dropdown.addItem(c);
		}
	}
	
	public JComboBox<T> getDropdown() {
		return dropdown;
	}
}
