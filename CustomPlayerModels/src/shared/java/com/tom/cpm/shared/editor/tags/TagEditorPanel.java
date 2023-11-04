package com.tom.cpm.shared.editor.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.InputPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.StackSlot;
import com.tom.cpl.gui.util.StackTagSlot;
import com.tom.cpl.item.Stack;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.tag.NativeTagManager;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.editor.tags.EditorTagManager.EditableTag;
import com.tom.cpm.shared.gui.panel.ListPanel;

public class TagEditorPanel<T> extends Panel {
	protected ListPicker<NamedElement<EditorTagManager<T>.EditableTag>> tagList;
	protected NameMapper<EditorTagManager<T>.EditableTag> tagMapper;
	protected FlowLayout elemsLayout;
	protected Panel elemsPanel;
	protected ScrollPanel scpElements;
	protected EditorTagManager<T> manager;
	protected ButtonIcon delTag, newEBtn;

	public TagEditorPanel(IGui gui, EditorTagManager<T> manager, int w, int h) {
		super(gui);

		this.manager = manager;
		tagMapper = new NameMapper<>(manager.getTags(), EditableTag::getId);
		tagList = new ListPicker<>(gui.getFrame(), tagMapper.asList());
		tagMapper.setSetter(tagList::setSelected);
		tagList.setBounds(new Box(5, 5, w - 60, 20));
		addElement(tagList);

		scpElements = new ScrollPanel(gui);
		addElement(scpElements);
		elemsPanel = new Panel(gui);
		scpElements.setDisplay(elemsPanel);
		elemsPanel.setBackgroundColor(gui.getColors().button_border);
		elemsPanel.setBounds(new Box(0, 0, w - 35, 0));
		scpElements.setBounds(new Box(5, 30, w - 35, h - 40));
		elemsLayout = new FlowLayout(elemsPanel, 5, 1);

		ButtonIcon newBtn = new ButtonIcon(gui, "editor", 0, 16, () -> {
			gui.getFrame().openPopup(new InputPopup(gui.getFrame(), gui.i18nFormat("label.cpm.tags.newTag"), gui.i18nFormat("label.cpm.tags.newTag.desc"), name -> {
				String f = EditorTagManager.formatTag(name);
				if (f == null) {
					gui.displayMessagePopup(gui.i18nFormat("label.cpm.tags.newTag.invalidTag"), gui.i18nFormat("label.cpm.tags.newTag.invalidChars"));
				} else {
					EditorTagManager<T>.EditableTag tag = manager.create(f);
					tagMapper.refreshValues();
					tagMapper.setValue(tag);
					updateElems();
				}
			}, null));
		});
		newBtn.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.tags.newTag")));
		newBtn.setBounds(new Box(w - 50, 5, 20, 20));
		addElement(newBtn);

		delTag = new ButtonIcon(gui, "editor", 16, 16, ConfirmPopup.confirmHandler(gui.getFrame(), gui.i18nFormat("label.cpm.tags.deleteTag"), () -> {}));
		delTag.setBounds(new Box(w - 25, 5, 20, 20));
		addElement(delTag);

		newEBtn = new ButtonIcon(gui, "editor", 0, 16, this::addNewElement);
		newEBtn.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.tags.newElem")));
		newEBtn.setBounds(new Box(w - 25, 30, 20, 20));
		addElement(newEBtn);

		updateElems();

		setBounds(new Box(0, 0, w, h));
	}

	private void updateElems() {
		int w = elemsPanel.getBounds().w;
		elemsPanel.getElements().clear();
		scpElements.setScrollY(0);
		boolean en = tagList.getSelected() != null && tagList.getSelected().getElem() != null;
		delTag.setEnabled(en);
		newEBtn.setEnabled(en);

		if (en) {
			EditorTagManager<T>.EditableTag tag = tagList.getSelected().getElem();
			tag.getEntries().forEach(e -> addElementToPanel(e, w));
		}
		elemsLayout.reflow();
	}

	protected void addElementToPanel(String elem, int w) {
		Panel pn = new Panel(gui);

		pn.addElement(new Label(gui, elem).setBounds(new Box(5, 5, 0, 0)));

		ButtonIcon del = new ButtonIcon(gui, "editor", 16, 16, () -> removeElem(elem));
		del.setBounds(new Box(w - 25, 0, 20, 20));
		pn.addElement(del);

		pn.setBounds(new Box(0, 0, w, 20));
		elemsPanel.addElement(pn);
	}

	protected void addNewElement() {
		List<String> elems = new ArrayList<>();
		manager.listAllTags().forEach(e -> elems.add(e.getId()));
		NativeTagManager<T> m = manager.getNativeManager();
		m.getAllElements().forEach(e -> elems.add(m.getId(e)));
		gui.getFrame().openPopup(new PopupList(elems));
	}

	protected class PopupList extends PopupPanel {
		private Button btnR;

		protected PopupList(List<String> values) {
			super(TagEditorPanel.this.gui);
			Frame frame = gui.getFrame();

			int w = Math.min(frame.getBounds().w / 4 * 3, 210);
			int h = Math.min(frame.getBounds().h / 4 * 3, 335);

			ListPanel<String> list = new ListPanel<>(gui, values, w - 10, h - 35);
			list.setBounds(new Box(5, 5, w - 10, h - 35));
			addElement(list);

			Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
				close();
				if (list.getSelected() != null)
					addTagElement(list.getSelected());
			});
			btn.setBounds(new Box(5, h - 25, 60, 20));
			addElement(btn);

			Button btnC = new Button(gui, gui.i18nFormat("button.cpm.cancel"), this::close);
			btnC.setBounds(new Box(70, h - 25, 60, 20));
			addElement(btnC);

			btnR = new Button(gui, gui.i18nFormat("button.cpm.tags.raw"), null);
			initRawPopup(gui.i18nFormat("button.cpm.tags.list"), () -> frame.openPopup(this));
			btnR.setBounds(new Box(135, h - 25, 60, 20));
			addElement(btnR);

			setBounds(new Box(0, 0, w, h));
		}

		protected PopupList(List<String> values, String exBtn, Runnable exAction) {
			this(values);
			initRawPopup(exBtn, exAction);
		}

		private void initRawPopup(String exBtn, Runnable exAction) {
			Frame frame = gui.getFrame();

			btnR.setAction(() -> {
				close();
				InputPopup in = new InputPopup(frame, gui.i18nFormat("label.cpm.tags.raw.title"), gui.i18nFormat("label.cpm.tags.raw.desc"), e -> addTagElement(e), null);

				Button btnT = new Button(gui, exBtn, () -> {
					in.close();
					exAction.run();
				});
				btnT.setBounds(new Box(95, in.getBounds().h - 25, 60, 20));
				in.addElement(btnT);

				frame.openPopup(in);
			});
		}

		@Override
		public String getTitle() {
			return gui.i18nFormat("label.cpm.tags.addNewElement");
		}
	}

	protected void removeElem(String elem) {
		if (tagList.getSelected() == null)return;
		EditorTagManager<T>.EditableTag tag = tagList.getSelected().getElem();
		manager.removeElemFromTag(tag, elem);
		updateElems();
	}

	protected void addTagElement(String elem) {
		if (tagList.getSelected() == null)return;
		EditorTagManager<T>.EditableTag tag = tagList.getSelected().getElem();
		manager.addElemToTag(tag, elem);
		updateElems();
	}

	public static class ItemTagEditorPanel extends TagEditorPanel<Stack> {

		public ItemTagEditorPanel(IGui gui, EditorTagManager<Stack> manager, int w, int h) {
			super(gui, manager, w, h);
		}

		@Override
		protected void addElementToPanel(String elem, int w) {
			Panel pn = new Panel(gui);

			pn.addElement(new StackTagSlot(gui, manager, elem).setBounds(new Box(5, 1, 18, 18)));
			pn.addElement(new Label(gui, elem).setBounds(new Box(30, 5, 0, 0)));

			ButtonIcon del = new ButtonIcon(gui, "editor", 16, 16, () -> removeElem(elem));
			del.setBounds(new Box(w - 25, 0, 20, 20));
			pn.addElement(del);

			pn.setBounds(new Box(0, 0, w, 20));
			elemsPanel.addElement(pn);
		}

		@Override
		protected void addNewElement() {
			gui.getFrame().openPopup(new PopupItems());
		}

		private class PopupItems extends PopupPanel {
			private List<StackSlot> slots;
			private String searchLast = "";
			private TextField searchField;
			private List<Stack> itemsList;
			private float currentScroll, oldScroll;
			private int cols, lines, dragY;
			private boolean refreshItemList = true;
			private Stack selected;
			private boolean enableDrag;

			protected PopupItems() {
				super(ItemTagEditorPanel.this.gui);
				Frame frame = gui.getFrame();

				int w = Math.min(frame.getBounds().w / 4 * 3, 210);
				int h = Math.min(frame.getBounds().h / 4 * 3, 335);

				searchField = new TextField(gui);
				searchField.setBounds(new Box(10, 10, w - 20, 20));
				addElement(searchField);
				searchField.setEventListener(this::updateSearch);

				slots = new ArrayList<>();
				itemsList = new ArrayList<>();
				lines = (int) ((h - 55) / 18f);
				cols = (int) ((w - 15) / 18f);
				for (int y = 0; y < lines;y++) {
					for (int x = 0; x < cols;x++) {
						StackSlot slot = new StackSlot(gui) {

							@Override
							public void draw(MouseEvent event, float partialTicks) {
								if (stack != null && Objects.equals(selected, stack)) {
									gui.drawRectangle(bounds.x, bounds.y, bounds.w, bounds.h, 0xFFFFFF00);
								}
								super.draw(event, partialTicks);
							}

							@Override
							public void mouseClick(MouseEvent event) {
								if (event.isHovered(bounds)) {
									selected = stack;
								}
							}
						};
						slot.setBounds(new Box(10 + x * 18, 35 + y * 18, 18, 18));
						addElement(slot);
						slots.add(slot);
					}
				}

				Button btn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
					close();
					if(selected != null)
						addTagElement(selected.getItemId());
				});
				btn.setBounds(new Box(5, h - 25, 60, 20));
				addElement(btn);

				Button btnC = new Button(gui, gui.i18nFormat("button.cpm.cancel"), this::close);
				btnC.setBounds(new Box(70, h - 25, 60, 20));
				addElement(btnC);

				Button btnT = new Button(gui, gui.i18nFormat("button.cpm.tags"), () -> {
					close();
					List<String> elems = new ArrayList<>();
					manager.listAllTags().forEach(e -> elems.add(e.getId()));
					gui.getFrame().openPopup(new PopupList(elems, gui.i18nFormat("tab.cpm.tags.items"), () -> gui.getFrame().openPopup(this)));
				});
				btnT.setBounds(new Box(135, h - 25, 60, 20));
				addElement(btnT);

				setBounds(new Box(0, 0, w, h));

				updateSearch();
			}

			@Override
			public String getTitle() {
				return gui.i18nFormat("label.cpm.tags.addNewElement");
			}


			private void updateSearch() {
				String searchString = searchField.getText();
				if (refreshItemList || !searchLast.equals(searchString)) {
					itemsList.clear();
					boolean searchMod = false;
					String search = searchString;
					if (searchString.startsWith("@")) {
						searchMod = true;
						search = searchString.substring(1);
					}
					Pattern m = null;
					try {
						m = Pattern.compile(search.toLowerCase(), Pattern.CASE_INSENSITIVE);
					} catch (Throwable ignore) {
						try {
							m = Pattern.compile(Pattern.quote(search.toLowerCase()), Pattern.CASE_INSENSITIVE);
						} catch (Throwable __) {
							return;
						}
					}
					try {
						for (Stack is : manager.getNativeManager().getAllElements()) {
							String dspName = searchMod ? is.getModId() : is.getDisplayName();
							if (m.matcher(dspName.toLowerCase()).find()) {
								itemsList.add(is);
							}
						}
					} catch (Exception e) {
					}
					Collections.sort(itemsList, Comparator.comparing(Stack::getDisplayName));
					this.currentScroll = 0;
					selected = null;
					this.searchLast = searchString;
					refreshItemList = false;
					scrollTo(this.currentScroll);
				}
			}

			public final void scrollTo(float p_148329_1_) {
				int i = (this.itemsList.size() + cols - 1) / cols - lines;
				int j = (int) (p_148329_1_ * i + 0.5D);

				if (j < 0) {
					j = 0;
				}

				for (int k = 0;k < lines;++k) {
					for (int l = 0;l < cols;++l) {
						int i1 = l + (k + j) * cols;

						if (i1 >= 0 && i1 < this.itemsList.size()) {
							setSlotContents(l + k * cols, this.itemsList.get(i1));
						} else {
							setSlotContents(l + k * cols, null);
						}
					}
				}
			}

			private void setSlotContents(int i, Stack stack) {
				slots.get(i).setStack(stack);
			}

			protected boolean needsScrollBars() {
				return itemsList.size() > lines * cols;
			}

			@Override
			public void mouseWheel(MouseEvent event) {
				if (needsScrollBars() && event.isHovered(bounds)) {
					int i = (itemsList.size() + cols - 1) / cols - lines;
					this.currentScroll = this.currentScroll - event.btn / (float) i;
					this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
					scrollTo(this.currentScroll);
					event.consume();
				}
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				super.draw(event, partialTicks);
				if (needsScrollBars()) {
					gui.pushMatrix();
					gui.setPosOffset(getBounds());
					gui.setupCut();

					int scx = bounds.w - 5;
					int i = (itemsList.size() + cols - 1) / cols;
					float sch = bounds.h - 65;
					float ph = i * 18;
					float overflowY = sch / ph;
					float h = Math.max(overflowY * sch, 8);
					float scroll = (currentScroll * (i - lines) * 18) / (ph - sch);
					int y = (int) (scroll * (sch - h));
					Box bar = new Box(bounds.x + scx, 35 + bounds.y + y, 3, (int) h);
					gui.drawBox(scx, 35, 3, sch, gui.getColors().panel_background);
					gui.drawBox(scx, 35 + y, 3, h, event.isHovered(bar) || enableDrag ? gui.getColors().button_hover : gui.getColors().button_disabled);

					gui.popMatrix();
					gui.setupCut();
				}
			}

			@Override
			public void mouseClick(MouseEvent event) {
				super.mouseClick(event);
				if(event.offset(bounds).isHovered(new Box(bounds.w - 5, 35, 3, bounds.h - 65))) {
					enableDrag = true;
					oldScroll = currentScroll;
					dragY = event.y;
					event.consume();
				}
			}

			@Override
			public void mouseDrag(MouseEvent event) {
				if (enableDrag) {
					float sch = bounds.h - 65;
					float ph = (itemsList.size() + cols - 1) / cols * 18;
					float overflowY = sch / ph;
					float h = Math.max(overflowY * sch, 8);

					float newScroll = (event.y - dragY) / (bounds.h - 65 - h);
					this.currentScroll = MathHelper.clamp(oldScroll + newScroll, 0.0F, 1.0F);
					scrollTo(currentScroll);
					event.consume();
				}
				super.mouseDrag(event);
			}

			@Override
			public void mouseRelease(MouseEvent event) {
				if(enableDrag) {
					enableDrag = false;
					event.consume();
				}
				super.mouseRelease(event);
			}
		}
	}
}
