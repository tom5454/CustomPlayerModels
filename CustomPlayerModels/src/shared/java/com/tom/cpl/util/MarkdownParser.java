package com.tom.cpl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.LabelText;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.LiteralText;
import com.tom.cpl.text.StyledText;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.MarkdownRenderer.CustomMdElementFactory;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

public class MarkdownParser {
	private static final Line NULL_LINE = new Line() {

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			return Collections.emptyList();
		}
	};
	private static final Pattern LIST = Pattern.compile("^(\\d+)\\..*$");
	private static final String ESC_CHARS = "!#()*+-.[\\]_{}~`";
	private static final Map<String, Character> htmlEscapeEntities = new HashMap<>();
	static {
		htmlEscapeEntities.put("amp", '&');
		htmlEscapeEntities.put("lt", '<');
		htmlEscapeEntities.put("gt", '>');
		htmlEscapeEntities.put("quot", '"');
		htmlEscapeEntities.put("apos", '\'');
	}

	private final List<Line> lines = new ArrayList<>();
	private final Map<Line, String> headerLines = new HashMap<>();
	private final Map<String, LinkReference> linkReferences = new HashMap<>();

	public static MarkdownParser makeErrorPage(IGui gui, Throwable e) {
		StringBuilder sb = new StringBuilder("# ");
		sb.append(gui.i18nFormat("label.cpm.md.loadFail"));
		sb.append("\n");
		sb.append(gui.i18nFormat("label.cpm.md.loadFail." + (e instanceof IOException ? "network" : "unknown")));
		sb.append("\n```");
		StringBuilderStream.stacktraceToString(e, sb, "\t");
		sb.append("```");
		return new MarkdownParser(sb.toString());
	}

	public MarkdownParser(String input) {
		try(BufferedReader rd = new BufferedReader(new StringReader(input))) {
			String ln, lnt;
			StringBuilder sb = new StringBuilder();
			String codeBlock = null;
			String nextHeaderName = null;
			List<Component> prefix = new ArrayList<>();
			boolean lastBreak = false;
			IndentComponent lastIndent = null;
			while((ln = rd.readLine()) != null) {
				lnt = ln.trim();
				if(lnt.startsWith("```")) {
					lastBreak = false;
					if(codeBlock == null) {
						parseLine(sb, 1, prefix);
						codeBlock = lnt.substring(3);
					} else {
						lines.add(new CodeLine(codeBlock, sb.toString()));
						sb.setLength(0);
						codeBlock = null;
					}
					continue;
				} else if(codeBlock != null) {
					sb.append(ln);
					sb.append('\n');
					continue;
				}
				if(lnt.isEmpty()) {//Paragraph break
					if (lastBreak)continue;
					parseLine(sb, 1, prefix);
					lines.add(new EmptyLine(12));
					lastBreak = true;
					continue;
				}
				boolean wasLastBreak = lastBreak;
				lastBreak = false;
				if(!ln.startsWith("  "))lastIndent = null;
				if(ln.startsWith("|")) {//Table
					parseLine(sb, 1, prefix);
					List<String> t = new ArrayList<>();
					t.add(lnt);
					while((ln = rd.readLine()) != null && ln.startsWith("|")) {
						t.add(ln.trim());
					}
					lines.add(new TableLine(t, this));
				} else if(ln.startsWith(">")) {//Quote
					parseLine(sb, 1, prefix);
					List<String> t = new ArrayList<>();
					t.add(lnt);
					while((ln = rd.readLine()) != null && ln.startsWith(">")) {
						t.add(ln.trim());
					}
					lines.add(new QuoteLine(t, this));
				} else if(lnt.startsWith("<a name=")) {//Header link
					String e = lnt.substring(9);
					int i = e.indexOf('"');
					if (i != -1) {
						nextHeaderName = e.substring(0, i);
						if (wasLastBreak)lastBreak = true;
						continue;
					} else {
						if(sb.length() > 0)sb.append(' ');
						sb.append(lnt);
					}
				} else if(lnt.startsWith("#")) {//Header
					parseLine(sb, 1, prefix);

					lines.add(new EmptyLine(8));

					float scl = 1;
					if(lnt.startsWith("####")) {
						scl = 1.1f;
					} else if(lnt.startsWith("###")) {
						scl = 1.2f;
					} else if(lnt.startsWith("##")) {
						scl = 1.5f;
					} else if(lnt.startsWith("#")) {
						scl = 2f;
					}
					String h = lnt.replaceAll("^#+", "").trim();
					sb.append(h);
					Line line = parseLine(sb, scl, prefix);
					if (nextHeaderName != null)
						headerLines.put(line, nextHeaderName);
					else
						headerLines.put(line, h.replaceAll("[^a-zA-Z0-9_\\-\\s]", "").replaceAll("[\\s\\-]", "-").toLowerCase(Locale.ROOT));
					nextHeaderName = null;

					if(scl > 1.3f)lines.add(new HorizontalLine());
					else lines.add(new EmptyLine(4));
				} else if(ln.startsWith("---") || ln.startsWith("***") || ln.startsWith("__")) {//HLine
					parseLine(sb, 1, prefix);
					lines.add(new HorizontalLine());
				} else if(lnt.startsWith("[")) {//Links
					int j = 0;
					for (;j<lnt.length() && lnt.charAt(j) != ']';j++);
					if (at(lnt, j + 1) == ':') {
						parseLine(sb, 1, prefix);

						String lnk = lnt.substring(1, j).toLowerCase(Locale.ROOT);
						LinkReference ref = linkReferences.get(lnk);
						if (ref != null) {
							if (lnk.startsWith("^")) {
								sb.append(lnt.substring(j + 2).trim());
								prefix.add(new TextComponent(new StringBuilder(lnk + ": "), new TextStyle()));
								Line line = parseLine(sb, 0.9f, prefix);
								headerLines.put(line, "footnote_" + lnk);
								ref.setLink("#footnote_" + lnk);
								lastIndent = new IndentComponent();
								lastIndent.setX(10);
								continue;
							} else {
								String val = lnt.substring(j + 2).trim();
								ref.setLink(val);
							}
						}
					} else if(ln.endsWith("  ")) {//Line break
						sb.append(lnt);
						parseLine(sb, 1, prefix);
					} else {
						if(sb.length() > 0)sb.append(' ');
						sb.append(lnt);
					}
				} else if(lnt.startsWith("- ") || lnt.startsWith("* ") || lnt.startsWith("+ ")) {//List
					parseLine(sb, 1, prefix);
					int indC = 0;
					for (;indC<ln.length() && ln.charAt(indC) == ' ';indC++);
					indC = indC / 2;
					lastIndent = new IndentComponent();
					prefix.add(new ListComponent(indC, null, lastIndent));
					sb.append(lnt.substring(1).trim());
					if(ln.endsWith("  ")) {
						parseLine(sb, 1, prefix);
					}
					continue;
				} else if(LIST.matcher(lnt).matches()) {//Numbered list
					parseLine(sb, 1, prefix);
					int indC = 0;
					for (;indC<ln.length() && ln.charAt(indC) == ' ';indC++);
					indC = indC / 2;
					Matcher m = LIST.matcher(lnt);
					m.find();
					String num = m.group(1);
					lastIndent = new IndentComponent();
					prefix.add(new ListComponent(indC, m.group(1) + ". ", lastIndent));
					sb.append(lnt.substring(num.length() + 1).trim());
					if(ln.endsWith("  ")) {
						parseLine(sb, 1, prefix);
					}
					continue;
				} else if(ln.endsWith("  ")) {//Line break
					if(ln.startsWith("  ") && lastIndent != null && !prefix.contains(lastIndent))
						prefix.add(lastIndent);
					sb.append(lnt);
					parseLine(sb, 1, prefix);
				} else {
					if(sb.length() > 0)sb.append(' ');
					sb.append(lnt);
				}
				if(ln.startsWith("  ") && lastIndent != null && !prefix.contains(lastIndent))
					prefix.add(lastIndent);
			}
			parseLine(sb, 1, prefix);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int toElements(MarkdownRenderer mdr, int width, Consumer<List<GuiElement>> elementAdder, Map<String, Integer> header) {
		Cursor c = new Cursor();
		c.maxWidth = width;
		lines.forEach(l -> {
			String h = headerLines.get(l);
			if(h != null)header.put(h, c.y);

			elementAdder.accept(l.toElements(mdr, c));
		});
		return c.y;
	}

	private Line parseLine(StringBuilder sb, float scl, List<Component> prefix) {
		Line ln = parseLine(sb, scl, prefix, this);
		lines.add(ln);
		return ln;
	}

	private static Line parseLine(StringBuilder sb, float scl, List<Component> prefix, MarkdownParser parser) {
		if(sb.length() == 0 && (prefix == null || prefix.isEmpty()))return NULL_LINE;
		String text = sb.toString();
		sb.setLength(0);
		List<Component> comp = new ArrayList<>();
		if(prefix != null) {
			comp.addAll(prefix);
			prefix.clear();
		}
		TextStyle current = new TextStyle();
		Map<Integer, TextStyle> styleEnd = new HashMap<>();
		for(int i = 0;i<text.length();i++) {
			if (styleEnd.containsKey(i)) {
				if(sb.length() > 0)
					comp.add(new TextComponent(sb, current));
				current = styleEnd.get(i);
				continue;
			}
			char c = text.charAt(i);
			if (c == '\\') {
				char n = at(text, i + 1);
				if(ESC_CHARS.indexOf(n) > -1) {
					sb.append(n);
					i++;
				} else sb.append(c);
			} else if(c == '*' || c == '_') {
				if(c == at(text, i + 1)) {
					boolean foundEnd = false;
					int m = -1;
					for (int j = i+1; j<text.length(); j++) {
						if (styleEnd.containsKey(j))continue;
						if (text.charAt(j) == c) {
							m = j;
							if (at(text, j + 1) == c) {
								foundEnd = true;
								break;
							}
						}
					}

					if (foundEnd) {
						styleEnd.put(m, current);//skip processing
						styleEnd.put(m + 1, new TextStyle(current));
						comp.add(new TextComponent(sb, current));
						current.bold = !current.bold;
						i++;
						sb.setLength(0);
					} else if(m != -1) {
						sb.append(c);
						styleEnd.put(m, new TextStyle(current));
						comp.add(new TextComponent(sb, current));
						current.italic = !current.italic;
						sb.setLength(0);
					} else {
						sb.append(c);
					}
				} else {
					boolean foundEnd = false;
					int m = -1;
					for (int j = i+1; j<text.length(); j++) {
						if (styleEnd.containsKey(j))continue;
						if (text.charAt(j) == c) {
							foundEnd = true;
							m = j;
							break;
						}
					}
					if (foundEnd) {
						styleEnd.put(m, new TextStyle(current));
						comp.add(new TextComponent(sb, current));
						current.italic = !current.italic;
						sb.setLength(0);
					} else {
						sb.append(c);
					}
				}
			} else if(c == '~' && at(text, i + 1) == '~') {
				boolean foundEnd = false;
				int m = -1;
				for (int j = i+1; j<text.length(); j++) {
					if (styleEnd.containsKey(j))continue;
					if (text.charAt(j) == c && at(text, j + 1) == c) {
						m = j;
						foundEnd = true;
						break;
					}
				}
				if (foundEnd) {
					styleEnd.put(m, current);//skip processing
					styleEnd.put(m + 1, new TextStyle(current));
					comp.add(new TextComponent(sb, current));
					current.strikethrough = !current.strikethrough;
					sb.setLength(0);
				} else {
					sb.append(c);
					sb.append(c);
				}
				i++;
			} else if(c == '!' && at(text, i + 1) == '[') {
				comp.add(new TextComponent(sb, current));
				sb.setLength(0);
				int j = i;
				i++;
				for(i++;i<text.length() && text.charAt(i) != ']';i++) {
					sb.append(text.charAt(i));
				}
				String alt = sb.toString();
				sb.setLength(0);
				if(at(text, i + 1) == '(') {
					i++;
					for(i++;i<text.length() && text.charAt(i) != ')';i++) {
						sb.append(text.charAt(i));
					}
					String lnk = sb.toString();
					sb.setLength(0);
					comp.add(new ImageComponent(alt, new LinkReference(lnk)));
				} else if(at(text, i + 1) == '[') {
					i++;
					for(i++;i<text.length() && text.charAt(i) != ']';i++) {
						sb.append(text.charAt(i));
					}
					String lnk = sb.toString();
					sb.setLength(0);
					LinkReference ref = LinkReference.getRef(parser, lnk);
					comp.add(new ImageComponent(alt, ref));
				} else {
					i = j + 1;
					sb.append("![");
				}
			} else if(c == '$' && at(text, i + 1) == '[') {
				comp.add(new TextComponent(sb, current));
				sb.setLength(0);
				int j = i;
				i++;
				for(i++;i<text.length() && text.charAt(i) != ']';i++) {
					sb.append(text.charAt(i));
				}
				String id = sb.toString();
				sb.setLength(0);
				if(at(text, i + 1) == '(') {
					i++;
					for(i++;i<text.length() && text.charAt(i) != ')';i++) {
						sb.append(text.charAt(i));
					}
					String args = sb.toString();
					sb.setLength(0);
					comp.add(new CustomComponent(id, args));
				} else {
					i = j + 1;
					sb.append("$[");
				}
			} else if(c == '[') {
				comp.add(new TextComponent(sb, current));
				sb.setLength(0);
				int j = i;
				for(i++;i<text.length() && text.charAt(i) != ']';i++) {
					sb.append(text.charAt(i));
				}
				String lnkText = sb.toString();
				sb.setLength(0);
				if(at(text, i + 1) == '(') {
					i++;
					for(i++;i<text.length() && text.charAt(i) != ')';i++) {
						sb.append(text.charAt(i));
					}
					String lnk = sb.toString();
					sb.setLength(0);
					comp.add(new LinkComponent(lnkText, new LinkReference(lnk)));
				} else if(at(text, i + 1) == '[') {
					i++;
					for(i++;i<text.length() && text.charAt(i) != ']';i++) {
						sb.append(text.charAt(i));
					}
					String lnk = sb.toString();
					sb.setLength(0);
					LinkReference ref = LinkReference.getRef(parser, lnk);
					comp.add(new LinkComponent(lnkText, ref));
				} else if(lnkText.startsWith("^")) {
					LinkReference ref = LinkReference.getRef(parser, lnkText);
					comp.add(new ScaledComponent(0.5f, 0, new LinkComponent("[" + lnkText.substring(1) + "]", ref)));
				} else {
					i = j;
					sb.append("[");
				}
			} else if(c == '`') {
				comp.add(new TextComponent(sb, current));
				sb.setLength(0);
				for(i++;i<text.length() && text.charAt(i) != '`';i++) {
					char ch = text.charAt(i);
					if (ch == '&') {
						i = parseEscape(i, text, sb);
					} else {
						sb.append(ch);
					}
				}
				comp.add(new CodeComponent(sb));
			} else if(c == '&') {
				i = parseEscape(i, text, sb);
			} else {
				sb.append(c);
			}
		}
		comp.add(new TextComponent(sb, current));
		sb.setLength(0);
		Line line = new ComponentLine(comp, scl);
		return line;
	}

	private static int parseEscape(int i, String text, StringBuilder sb) {
		int j = i;
		for(i++;i<text.length() && text.charAt(i) != ';';i++);
		if (at(text, i) == ';') {
			if (at(text, j + 1) == '#') {
				try {
					char ch;
					if (at(text, j + 2) == 'X' || at(text, j + 2) == 'x') {
						ch = (char) Integer.parseInt(text.substring(j + 3, i), 16);
					} else {
						ch = (char) Integer.parseInt(text.substring(j + 2, i), 10);
					}
					sb.append(ch);
				} catch (NumberFormatException e) {
					i = j;
					sb.append('&');
				}
			} else {
				Character ec = htmlEscapeEntities.get(text.substring(j + 1, i));
				if (ec != null) {
					sb.append(ec);
				} else {
					i = j;
					sb.append('&');
				}
			}
		} else {
			i = j;
			sb.append('&');
		}
		return i;
	}

	private static char at(String s, int i) {
		if(s.length() > i)return s.charAt(i);
		else return 0;
	}

	private static class LinkReference {
		private String url;
		private String tooltip;

		private LinkReference() {
		}

		public LinkReference(String url) {
			setLink(url);
		}

		public static LinkReference getRef(MarkdownParser parser, String id) {
			return parser.linkReferences.computeIfAbsent(id.toLowerCase(Locale.ROOT), __ -> new LinkReference());
		}

		public void setLink(String url) {
			this.url = url;
			if (url.endsWith("\"")) {
				String[] sp = url.split(" \"", 2);
				this.url = sp[0];
				this.tooltip = sp[1];
				this.tooltip = this.tooltip.substring(0, this.tooltip.length() - 1);
			}
		}
	}

	private static interface Line {
		List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor);
	}

	private static class EmptyLine implements Line {
		private int height;

		public EmptyLine(int height) {
			this.height = height;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			cursor.y += height;
			return Collections.emptyList();
		}
	}

	private static class HorizontalLine implements Line {

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			cursor.y += 4;
			return Arrays.asList(new HLine(mdr.getGui()).setBounds(cursor.bounds(0, -4, cursor.maxWidth, 1)));
		}

		private static class HLine extends GuiElement {

			public HLine(IGui gui) {
				super(gui);
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				gui.drawBox(bounds.x, bounds.y, bounds.w, 1, gui.getColors().button_disabled);
			}
		}
	}

	private static class CodeLine implements Line {
		private String[] code;
		private String lang, c;

		public CodeLine(String lang, String code) {
			this.code = code.split("\n");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.code.length; i++) {
				String ln = this.code[i];
				for (int j = 0;j<ln.length();j++) {
					char c = ln.charAt(j);
					if(c == '\t') {
						int s = 4 - j % 4;
						for(int k = 0;k<s;k++)sb.append(' ');
					} else sb.append(c);
				}
				this.code[i] = sb.toString();
				sb.setLength(0);
			}
			this.c = code;
			this.lang = lang;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			Code c = new Code(mdr.getGui(), lang, this.c, code, cursor.bounds(cursor.maxWidth, code.length * 10 + 8));
			cursor.y += code.length * 10 + 15;
			return Arrays.asList(c);
		}

		private static class Code extends Panel {

			public Code(IGui gui, String lang, String c, String[] code, Box b) {
				super(gui);
				setBounds(b);

				Panel p = new Panel(gui);
				p.setBackgroundColor(gui.getColors().button_fill);

				ScrollPanel scp = new ScrollPanel(gui);
				addElement(scp);
				scp.setDisplay(p);
				scp.setBounds(new Box(0, 0, b.w, b.h));

				int w = Arrays.stream(code).mapToInt(gui::textWidth).max().orElse(0) + 5;
				if(w > bounds.w - 52)w += 55;

				p.setBounds(new Box(0, 0, w, b.h - 4));
				p.addElement(new GuiElement(gui) {

					@Override
					public void draw(MouseEvent event, float partialTicks) {
						int x = bounds.x + 3;
						int y = bounds.y + 5;
						int c = gui.getColors().label_text_color;

						for (int i = 0; i < code.length; i++) {
							gui.drawText(x, y + i * 10, code[i], c);
						}
					}

				}.setBounds(new Box(0, 0, w, b.h - 4)));

				Button cpy = new Button(gui, gui.i18nFormat("button.cpm.copy"), () -> {
					gui.setClipboardText(c);
				});
				cpy.setBounds(new Box(bounds.w - 52, 3, 50, 12));
				addElement(cpy);
			}
		}
	}

	private static class QuoteLine implements Line {
		private Line[] lines;
		private QuoteNote header;

		public QuoteLine(List<String> t, MarkdownParser parser) {
			try {
				String header = t.get(0);
				if (header.startsWith("> [!")) {
					int i = header.indexOf('!');
					header = header.substring(i + 1);
					i = header.indexOf(']');
					header = header.substring(0, i);
					t.remove(0);
					for (QuoteNote q : QuoteNote.values()) {
						if (q.name().equalsIgnoreCase(header)) {
							this.header = q;
							break;
						}
					}
				}
				lines = new ComponentLine[t.size()];

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < t.size(); i++) {
					String txt = t.get(i);
					sb.append(txt, 2, txt.length());
					this.lines[i] = parseLine(sb, 1, null, parser);
				}
			} catch (Exception e) {
				Log.warn("Error parsing markdown quote", e);
				this.lines = null;
			}
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			if (lines == null) {
				Label l = new Label(mdr.getGui(), "Error parsing quote in Markdown");
				l.setBounds(cursor.bounds(0, 0));
				l.setColor(0xffff0000);
				cursor.y += 10;
				return Arrays.asList(l);
			} else {
				return Arrays.asList(new QuoteBlock(mdr, cursor));
			}
		}

		private class QuoteBlock extends Panel {

			public QuoteBlock(MarkdownRenderer mdr, Cursor cursor) {
				super(mdr.getGui());

				int mw = cursor.maxWidth - 1;
				Panel p = new Panel(gui);
				Cursor c = new Cursor();
				c.maxWidth = mw - 10;
				for (int j = 0; j < lines.length; j++) {
					Line line = lines[j];
					p.getElements().addAll(line.toElements(mdr, c));
				}
				int h = header != null ? 21 : 0;
				p.setBounds(new Box(6, h, mw - 10, c.y));
				addElement(p);
				setBounds(cursor.bounds(mw + 1, c.y + h + 2));
				cursor.y += c.y + h + 10;
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				gui.pushMatrix();
				gui.setPosOffset(getBounds());
				gui.setupCut();
				Box bounds = getBounds();
				int rgb = gui.getColors().popup_background;
				if (header != null) {
					rgb = header.color.apply(gui);
					gui.drawBox(1, 1, 2, bounds.h - 1, gui.getColors().popup_background);
					String note = gui.i18nFormat("label.cpm.md.quote." + header.name().toLowerCase(Locale.ROOT));
					gui.drawTexture(4, 2, 16, 16, header.ordinal() * 16, 96, "editor", rgb);
					gui.drawText(25, 7, note, gui.getColors().popup_background);
					gui.drawText(24, 6, note, rgb);
				}
				gui.drawBox(0, 0, 2, bounds.h - 2, rgb);

				for (GuiElement guiElement : elements) {
					if(guiElement.isVisible())
						guiElement.draw(event.offset(bounds), partialTicks);
				}
				gui.popMatrix();
				gui.setupCut();
			}
		}

		public static enum QuoteNote {
			NOTE(g -> 0xFF2f81f7),
			TIP(g -> 0xFF3fb950),
			IMPORTANT(g -> 0xFFa371f7),
			WARNING(g -> 0xFFd29922),
			CAUTION(g -> 0xFFf85149),
			;
			private Function<IGui, Integer> color;

			private QuoteNote(Function<IGui, Integer> color) {
				this.color = color;
			}
		}
	}

	private static class TableLine implements Line {
		private Line[][] table;
		private Align[] colAlign;
		private int cols;

		public TableLine(List<String> lines, MarkdownParser parser) {
			try {
				String[][] table = lines.stream().map(l -> Arrays.stream(l.substring(1, l.length() - 1).split("\\|")).map(String::trim).toArray(String[]::new)).toArray(String[][]::new);
				cols = table[0].length;
				this.table = new ComponentLine[table.length - 1][cols];
				this.colAlign = new Align[cols];
				Arrays.fill(colAlign, Align.LEFT);

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < table.length; i++) {
					String[] c = table[i];
					if(i == 1) {
						for (int j = 0; j < cols; j++) {
							String align = c[j];
							boolean left = align.startsWith(":");
							boolean right = align.endsWith(":");
							if (left && right)colAlign[j] = Align.CENTER;
							else if(right)colAlign[j] = Align.RIGHT;
						}
						//TODO alignment
						continue;
					}
					for (int j = 0; j < cols; j++) {
						String txt = c[j];
						sb.append(txt);
						this.table[i == 0 ? 0 : i - 1][j] = parseLine(sb, 1, null, parser);
					}
				}

			} catch (Exception e) {
				Log.warn("Error parsing markdown table", e);
				this.table = null;
			}
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			if(table == null) {
				Label l = new Label(mdr.getGui(), "Error parsing table in Markdown");
				l.setBounds(cursor.bounds(0, 0));
				l.setColor(0xffff0000);
				cursor.y += 10;
				return Arrays.asList(l);
			} else {
				return Arrays.asList(new Table(mdr, cursor));
			}
		}

		private static enum Align {
			LEFT,
			CENTER,
			RIGHT;
		}

		private class Table extends Panel {

			public Table(MarkdownRenderer mdr, Cursor cursor) {
				super(mdr.getGui());

				Panel[] ps = new Panel[cols];
				int y = 1;
				int mw = (cursor.maxWidth - 1) / cols;
				for (int i = 0; i < table.length; i++) {
					Line[] lines = table[i];

					int mh = 0;
					for (int j = 0; j < lines.length; j++) {
						Line line = lines[j];
						Panel p = new Panel(gui);
						ps[j] = p;
						p.setBackgroundColor(i == 0 ? gui.getColors().menu_bar_background : (i % 2 == 0 ? gui.getColors().button_fill : gui.getColors().button_border));

						Cursor c = new Cursor();
						c.maxWidth = mw - 1;
						c.y = 1;
						p.getElements().addAll(line.toElements(mdr, c));
						mh = Math.max(mh, c.y);

						addElement(p);
					}
					for (int j = 0; j < ps.length; j++) {
						ps[j].setBounds(new Box(1 + j * mw, y, mw - 1, mh));
					}
					y += mh;
					y++;
				}

				setBounds(cursor.bounds(mw * cols + 1, y));
				setBackgroundColor(gui.getColors().popup_background);
				cursor.y += y + 10;
			}
		}
	}

	private static class ComponentLine implements Line {
		private List<Component> components;
		private float scale;

		public ComponentLine(List<Component> components, float scale) {
			this.components = components;
			this.scale = scale;
		}

		@Override
		public String toString() {
			return scale + " " + components.toString();
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			Cursor sc = new Cursor();
			sc.y = cursor.y;
			sc.maxWidth = cursor.maxWidth;
			sc.scale = scale;
			List<GuiElement> e = components.stream().flatMap(c -> c.toElements(mdr, sc).stream()).collect(Collectors.toList());
			cursor.y = (int) (sc.y + scale * 10);
			return e;
		}
	}

	public static class Cursor {
		public int maxWidth, x, y, xStart;
		public float scale = 1;

		public Box bounds(float w, float h) {
			return new Box(x, y, (int) w, (int) h);
		}

		public Box bounds(int x, int y, int w, int h) {
			return new Box(x, this.y + y, w, h);
		}
	}

	private static interface Component {
		List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor);
	}

	private static class TextComponent implements Component {
		private String text;
		private TextStyle style;

		public TextComponent(StringBuilder text, TextStyle style) {
			this.text = text.toString();
			text.setLength(0);
			this.style = new TextStyle(style);
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			IGui gui = mdr.getGui();
			return linewrapStyled(text, cursor, style, s -> new LabelText(gui, s).setScale(cursor.scale), gui::textWidthFormatted);
		}
	}

	private static class CodeComponent implements Component {
		private String text;

		public CodeComponent(StringBuilder text) {
			this.text = text.toString();
			text.setLength(0);
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			float s = cursor.scale;
			return linewrapSimple(text, cursor, t -> new Lbl(mdr.getGui(), t, s), mdr.getGui()::textWidth);
		}

		private static class Lbl extends GuiElement {
			private String text;
			private LiteralText txt;
			private float scale;

			public Lbl(IGui gui, String text, float scale) {
				super(gui);
				this.text = text;
				this.txt = new LiteralText(text);
				this.scale = scale;
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				gui.drawBox(bounds.x, bounds.y - 1, gui.textWidth(text) * scale, scale * 10, gui.getColors().button_fill);
				gui.drawFormattedText(bounds.x, bounds.y, txt, gui.getColors().label_text_color, scale);
			}
		}
	}

	private static class LinkComponent implements Component {
		protected LinkReference ref;
		protected String text;

		public LinkComponent(String text, LinkReference ref) {
			this.text = text;
			this.ref = ref;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			float s = cursor.scale;
			String url = ref.url;
			Runnable click = () -> mdr.browse(url);
			List<Predicate<MouseEvent>> hovers = new ArrayList<>();
			Tooltip tt = ref.tooltip != null ? new Tooltip(mdr.getGui().getFrame(), ref.tooltip) : null;
			return linewrapSimple(text, cursor, t -> new Lbl(mdr.getGui(), t, click, s, hovers, tt), mdr.getGui()::textWidth);
		}

		private static class Lbl extends GuiElement {
			private LiteralText txt;
			private float scale;
			private Runnable click;
			private List<Predicate<MouseEvent>> hovers;
			private Tooltip tt;

			public Lbl(IGui gui, String text, Runnable click, float scale, List<Predicate<MouseEvent>> hovers, Tooltip tt) {
				super(gui);
				this.txt = new LiteralText(text);
				this.scale = scale;
				this.click = click;
				this.hovers = hovers;
				this.tt = tt;
				hovers.add(e -> e.isHovered(bounds));
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				if (tt != null && event.isHovered(bounds))tt.set();
				gui.drawFormattedText(bounds.x, bounds.y, txt, hovers.stream().anyMatch(p -> p.test(event)) ? gui.getColors().link_hover : gui.getColors().link_normal, scale);
			}

			@Override
			public void mouseClick(MouseEvent event) {
				if(event.isHovered(bounds) && event.btn == 0) {
					click.run();
				}
			}
		}
	}

	private static class IndentComponent implements Component {
		private int x;

		public void setX(int x) {
			this.x = x;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			cursor.x += x;
			cursor.xStart = x;
			return Collections.emptyList();
		}
	}

	private static class ListComponent implements Component {
		private int sub;
		private String h;
		private IndentComponent indent;

		public ListComponent(int sub, String h, IndentComponent indent) {
			this.sub = sub;
			this.h = h;
			this.indent = indent;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			int w = h == null ? 10 : mdr.getGui().textWidth(h);
			indent.setX(5 + sub * 15 + w);
			cursor.x = 5 + sub * 15;
			cursor.xStart = cursor.x + w;
			GuiElement he = h != null ? new Label(mdr.getGui(), h) : new BulletPoint(mdr.getGui(), Math.min(sub, 3));
			List<GuiElement> l = Arrays.asList(he.setBounds(cursor.bounds(0, 0)));
			cursor.x += w;
			return l;
		}

		private static class BulletPoint extends GuiElement {
			private int i;

			public BulletPoint(IGui gui, int i) {
				super(gui);
				this.i = i;
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				gui.drawTexture(bounds.x, bounds.y, 8, 8, 48 + (i % 2) * 8, (i / 2) * 8, "editor");
			}
		}
	}

	private static class ImageComponent implements Component {
		private LinkReference ref;
		private String altText;
		private Vec2i size = new Vec2i();
		private Tooltip error;

		public ImageComponent(String altText, LinkReference ref) {
			this.altText = altText;
			this.ref = ref;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			IGui gui = mdr.getGui();
			Img i = new Img(gui);
			if (ref.tooltip != null)
				i.tooltip = new Tooltip(gui.getFrame(), ref.tooltip);
			if (ref.url != null) {
				boolean[] refresh = new boolean[] {false};
				mdr.getLoader().loadImage(ref.url).thenAcceptAsync(img -> {
					if(size.x == 0) {
						size.x = img.getWidth();
						size.y = img.getHeight();
						if(refresh[0]) {
							mdr.refresh();
							return;
						}
					}
					TextureProvider p = new TextureProvider(img, new Vec2i());
					mdr.registerCleanup(p::free);
					i.pr = p;
					error = null;
				}, gui::executeLater).exceptionally(e -> {
					size.x = 16;
					size.y = 16;
					error = new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.failedToLoadImage", e.getMessage()));
					return null;
				});
				refresh[0] = true;
			} else {
				error = new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.failedToLoadImage", "Invalid image URL"));
			}
			if (error != null) {
				float s = cursor.scale;
				Tooltip tt = error;
				return linewrapSimple(altText, cursor, t -> new Alt(mdr.getGui(), t, s, tt), mdr.getGui()::textWidth);
			}
			if(size.x > cursor.maxWidth) {
				float sc = size.x / (float) cursor.maxWidth;
				size.x = cursor.maxWidth;
				size.y /= sc;
			}
			i.setBounds(cursor.bounds(size.x, size.y));
			cursor.x += size.x + 1;
			cursor.y += size.y + 1;
			return Arrays.asList(i);
		}

		private static class Img extends GuiElement {
			private Tooltip tooltip;
			private TextureProvider pr;
			private boolean error;

			public Img(IGui gui) {
				super(gui);
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				if(event.isHovered(bounds) && tooltip != null)tooltip.set();
				if(error) {
					gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, 0xffff0000);
				} else if(pr != null) {
					pr.bind();
					gui.drawTexture(bounds.x, bounds.y, bounds.w, bounds.h, 0, 0, 1, 1);
				} else {
					gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_fill);
				}
			}
		}

		private static class Alt extends GuiElement {
			private LiteralText txt;
			private float scale;
			private Tooltip tt;

			public Alt(IGui gui, String text, float scale, Tooltip tt) {
				super(gui);
				this.txt = new LiteralText(text);
				this.scale = scale;
				this.tt = tt;
			}

			@Override
			public void draw(MouseEvent event, float partialTicks) {
				if (tt != null && event.isHovered(bounds))tt.set();
				gui.drawFormattedText(bounds.x, bounds.y, txt, gui.getColors().label_text_color, scale);
			}
		}
	}

	private static class CustomComponent implements Component {
		private String id, args;

		public CustomComponent(String id, String args) {
			this.id = id;
			this.args = args;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			CustomMdElementFactory f = mdr.customElementFactories.get(id);
			if(f == null) {
				Label lbl = new Label(mdr.getGui(), "??");
				lbl.setBounds(cursor.bounds(10, 10));
				lbl.setColor(0xff0000);
				lbl.setTooltip(new Tooltip(mdr.getGui().getFrame(), "Unknown custom component: " + id));
				cursor.x += 10;
				return Arrays.asList(lbl);
			}
			return f.create(mdr, cursor, args);
		}
	}

	private static class ScaledComponent implements Component {
		private float scale;
		private int yOff;
		private Component component;

		public ScaledComponent(float scale, int yOff, Component component) {
			this.scale = scale;
			this.yOff = yOff;
			this.component = component;
		}

		@Override
		public List<GuiElement> toElements(MarkdownRenderer mdr, Cursor cursor) {
			Cursor c = new Cursor();
			c.x = cursor.x;
			c.scale = cursor.scale * scale;
			c.y = cursor.y + yOff;
			c.xStart = cursor.xStart;
			c.maxWidth = (int) (cursor.maxWidth / scale);
			List<GuiElement> el = component.toElements(mdr, c);
			cursor.x = c.x;
			cursor.y = c.y;
			return el;
		}
	}

	public static List<GuiElement> linewrapSimple(String textIn, Cursor cursor, Function<String, GuiElement> lbl, ToIntFunction<String> width) {
		return linewrap(textIn, cursor, Function.identity(), lbl, width);
	}

	public static <T> List<GuiElement> linewrap(String textIn, Cursor cursor, Function<String, T> toText, Function<T, GuiElement> lbl, ToIntFunction<T> width) {
		List<GuiElement> text = new ArrayList<>();
		int splitStart = 0;
		int space = -1;
		float h = 10 * cursor.scale;
		for (int i = 0;i<textIn.length();i++) {
			char c = textIn.charAt(i);
			if(c == ' ') {
				T s = toText.apply(textIn.substring(splitStart, i));
				float lw = width.applyAsInt(s) * cursor.scale;
				if(cursor.x + lw > cursor.maxWidth) {
					if (space == -1 && cursor.x > cursor.maxWidth / 2) {
					} else if(splitStart == space + 1) {
						text.add(lbl.apply(s).setBounds(cursor.bounds(lw, h)));
						splitStart = i + 1;
					} else {
						s = toText.apply(textIn.substring(splitStart, space));
						text.add(lbl.apply(s).setBounds(cursor.bounds(lw, h)));
						splitStart = space + 1;
					}
					cursor.x = cursor.xStart;
					cursor.y += cursor.scale * 10;
				}
				space = i;
			}
		}
		T s = toText.apply(textIn.substring(splitStart, textIn.length()));
		float lw = width.applyAsInt(s) * cursor.scale;
		if(cursor.x + lw > cursor.maxWidth && space != -1) {
			if(splitStart == space + 1) {
				text.add(lbl.apply(s).setBounds(cursor.bounds(lw, h)));
				splitStart = textIn.length();
			} else {
				s = toText.apply(textIn.substring(splitStart, space));
				text.add(lbl.apply(s).setBounds(cursor.bounds(lw, h)));
				splitStart = space + 1;
			}
			cursor.x = cursor.xStart;
			cursor.y += cursor.scale * 10;
		}

		if(splitStart < textIn.length()) {
			s = toText.apply(textIn.substring(splitStart, textIn.length()));
			lw = width.applyAsInt(s) * cursor.scale;
			if(cursor.x + lw > cursor.maxWidth && ((space == -1 && cursor.x > cursor.maxWidth / 2) || space != -1)) {
				cursor.x = cursor.xStart;
				cursor.y += cursor.scale * 10;
			}
			text.add(lbl.apply(s).setBounds(cursor.bounds(lw, h)));
			cursor.x += lw;
		}
		return text;
	}

	public static <T> List<GuiElement> linewrapStyled(String textIn, Cursor cursor, TextStyle style, Function<IText, GuiElement> lbl, ToIntFunction<IText> width) {
		return linewrap(textIn, cursor, t -> new StyledText(new LiteralText(t), style), lbl, width);
	}
}