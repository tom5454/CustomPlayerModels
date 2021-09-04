package com.tom.cpm.shared.gui.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.ListElement.ListModel;
import com.tom.cpl.gui.elements.ListElement2d;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;

public class ListPanel<E> extends Panel implements ListModel<E> {
	private List<E> list;
	private List<E> listIn;
	private TextField search;
	private ListElement2d<E> listElement;
	private Consumer<E> onSelect;
	private Panel listPanel;
	private ScrollPanel scpList;
	private int w, h;
	private E selected;

	public ListPanel(IGui gui, List<E> listIn, int w, int h) {
		super(gui);

		this.list = new ArrayList<>();
		this.listIn = listIn;

		listPanel = new Panel(gui);
		listPanel.setBackgroundColor(gui.getColors().menu_bar_background);

		listElement = new ListElement2d<>(gui, this, this.list);
		listPanel.addElement(listElement);

		search = new TextField(gui);
		search.setEventListener(this::refreshList);
		addElement(search);

		scpList = new ScrollPanel(gui);
		scpList.setDisplay(listPanel);
		addElement(scpList);

		search.setBounds(new Box(5, 5, w - 10, 20));
		scpList.setBounds(new Box(5, 30, w - 10, h - 35));

		this.w = w;
		this.h = h;
		listElement.setPreferredWidth(w);
		refreshList();
	}

	private void refreshList() {
		String searchString = search.getText();
		Pattern m = null;
		try {
			m = Pattern.compile(searchString.toLowerCase(), Pattern.CASE_INSENSITIVE);
		} catch (Throwable ignore) {
			try {
				m = Pattern.compile(Pattern.quote(searchString.toLowerCase()), Pattern.CASE_INSENSITIVE);
			} catch (Throwable __) {
				return;
			}
		}
		final Pattern fp = m;
		list.clear();
		listIn.stream().filter(e -> {
			String dspName = e.toString();
			return fp.matcher(dspName.toLowerCase()).find();
		}).sorted(Comparator.comparing(Object::toString)).forEach(list::add);
		Vec2i size = listElement.getSize();
		size.x = Math.max(size.x, w - 10);
		listElement.setBounds(new Box(0, 0, size.x, size.y));
		listPanel.setBounds(new Box(0, 0, size.x, size.y));
	}

	@Override
	public Tooltip getTooltip(E el) {
		return null;
	}

	@Override
	public String getDisplay(E el) {
		return el.toString();
	}

	@Override
	public void selected(E selected) {
		this.selected = selected;
		onSelect.accept(selected);
	}

	public void setSelect(Consumer<E> select) {
		this.onSelect = select;
	}

	public String getSearchText() {
		return search.getText();
	}

	public void setSearchText(String txt) {
		search.setText(txt);
	}

	public void setSelected(E selected) {
		listElement.setSelected(selected);
	}

	public void setWidth(int w) {
		this.w = w;
		listElement.setPreferredWidth(w - 10);
		Vec2i size = listElement.getSize();
		size.x = Math.max(size.x, w - 10);
		listElement.setBounds(new Box(0, 0, size.x, size.y));
		listPanel.setBounds(new Box(0, 0, size.x, size.y));
		search.setBounds(new Box(5, 5, w - 10, 20));
		scpList.setBounds(new Box(5, 30, w - 10, h - 35));
		setBounds(new Box(bounds.x, bounds.y, w, h));
	}

	public E getSelected() {
		return selected;
	}
}
