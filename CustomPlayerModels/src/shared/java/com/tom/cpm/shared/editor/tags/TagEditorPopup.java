package com.tom.cpm.shared.editor.tags;

import com.tom.cpl.block.Biome;
import com.tom.cpl.block.BlockState;
import com.tom.cpl.block.entity.EntityType;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.tags.TagEditorPanel.ItemTagEditorPanel;

public class TagEditorPopup extends PopupPanel {
	private TabbedPanelManager tabs;
	private HorizontalLayout topPanel;

	public TagEditorPopup(EditorGui e) {
		super(e.getGui());

		int width = Math.max(e.getBounds().w / 5 * 3, 400);
		int height = Math.max(e.getBounds().h / 4 * 3, 300);

		tabs = new TabbedPanelManager(gui);
		tabs.setBounds(new Box(0, 20, width, height - 20));
		addElement(tabs);

		Panel topPanel = new Panel(gui);
		topPanel.setBounds(new Box(0, 0, width, 20));
		topPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		addElement(topPanel);
		this.topPanel = new HorizontalLayout(topPanel);

		EditorTags tagMngrs = e.getEditor().tags;

		ItemTagEditorPanel items = new ItemTagEditorPanel(gui, tagMngrs.getItemTags(), width, height - 20);
		this.topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.tags.items"), items));

		TagEditorPanel<BlockState> blocks = new TagEditorPanel<>(gui, tagMngrs.getBlockTags(), width, height - 20);
		this.topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.tags.blocks"), blocks));

		TagEditorPanel<EntityType> entities = new TagEditorPanel<>(gui, tagMngrs.getEntityTags(), width, height - 20);
		this.topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.tags.entities"), entities));

		if (MinecraftClientAccess.get().getBiomeHandler().isAvailable()) {
			TagEditorPanel<Biome> biomes = new TagEditorPanel<>(gui, tagMngrs.getBiomeTags(), width, height - 20);
			this.topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.tags.biomes"), biomes));
		} else {
			Button btn = new Button(gui, gui.i18nFormat("tab.cpm.tags.biomes"), () -> {});
			btn.setEnabled(false);
			btn.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.tags.biomesNotAvailable")));
			this.topPanel.add(btn);
		}

		setBounds(new Box(0, 0, width, height));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.tags.editor");
	}
}
