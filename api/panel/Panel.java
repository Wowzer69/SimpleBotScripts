package api.panel;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.JTextComponent;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import api.Variables;
import api.listeners.ConfigChangeEvent;
import api.panel.options.Range;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;
import simple.robot.api.ClientContext;
import simple.robot.utils.ScriptUtils;

public class Panel extends PluginPanel {

	private static ImageIcon SECTION_EXPAND_ICON;
	private static ImageIcon SECTION_EXPAND_ICON_HOVER;
	private static ImageIcon SECTION_RETRACT_ICON;
	private static ImageIcon SECTION_RETRACT_ICON_HOVER;

	private static final int SPINNER_FIELD_WIDTH = 6;

	static {
		try {
			BufferedImage sectionRetractIcon = ImageIO
					.read(Panel.class.getClassLoader().getResource("resources/arrow_right.png"));
			sectionRetractIcon = ImageUtil.luminanceOffset(sectionRetractIcon, -121);
			SECTION_EXPAND_ICON = new ImageIcon(sectionRetractIcon);
			SECTION_EXPAND_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionRetractIcon, -100));
			final BufferedImage sectionExpandIcon = ImageUtil.rotateImage(sectionRetractIcon, Math.PI / 2);
			SECTION_RETRACT_ICON = new ImageIcon(sectionExpandIcon);
			SECTION_RETRACT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionExpandIcon, -100));
		} catch (Exception e) {
			ClientContext.instance().log(e.getMessage());
		}
	}

	public Panel() {
		add(getHeader());

		Config.TABS.forEach(tab -> {
			JPanel t = buildTab(tab.getTitle(), tab.getDescription());
			buildConfig(tab.getIndex(), t);
			add(t);
			add(Box.createRigidArea(new Dimension(0, 10)));
		});
		add(getButton());
	}

	private JFrame frame;

	public JFrame init(String title, Panel panel) throws IOException {
		frame = new JFrame();
		BufferedImage icon = ImageIO.read(getClass().getClassLoader().getResource("resources/KS.png"));
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setViewportView(panel);
		scriptName.setText(title);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setTitle("KS GUI");
		frame.setIconImage(icon);
		frame.setResizable(false);
		frame.setMinimumSize(new Dimension(275, 0));
		frame.add(scroller);
		frame.pack();
		frame.repaint();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					ClientContext.instance().stopScript();
				}
			}
		});
		return frame;
	}

	private JLabel scriptName, status, runTime, count;

	private JPanel getHeader() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(0, 1));
		pan.setMinimumSize(new Dimension(PANEL_WIDTH, 25));

		scriptName = new JLabel("", 0);
		scriptName.setForeground(new Color(199, 201, 201));
		pan.add(scriptName);

		status = new JLabel("Booting up...", 0);
		status.setForeground(new Color(199, 201, 201));
		pan.add(status);

		runTime = new JLabel("00:00:00", 0);
		runTime.setForeground(new Color(199, 201, 201));
		pan.add(runTime);

		count = new JLabel("", 0);
		count.setForeground(new Color(199, 201, 201));
		pan.add(count);
		pan.setBorder(
				new CompoundBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(0, 0, 3, 1)));
		return pan;
	}

	public void update(String text) {
		status.setText("<html>" + text + "</html>");
		runTime.setText(Variables.START_TIME.toElapsedString());
		if (Variables.COUNT > 0) count.setText(String.format("Count: %s (%s p/hr)", Variables.COUNT, ScriptUtils
				.getValuePerHour(Variables.START_TIME.getStart(), Variables.START_TIME.getElapsed(), (int) Variables.COUNT)));
	}

	private void toggleSection(JButton button, JPanel contents) {
		boolean newState = !contents.isVisible();
		contents.setVisible(newState);
		button.setIcon(newState ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		button.setRolloverIcon(newState ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		button.setToolTipText(newState ? "Retract" : "Expand");
		SwingUtilities.invokeLater(contents::revalidate);
	}

	private JPanel buildTab(String name, String desc) {
		final boolean isOpen = name.equals("Script Config");
		final JPanel section = new JPanel();
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
		section.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

		final JPanel sectionHeader = new JPanel();
		sectionHeader.setBorder(
				new CompoundBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(0, 0, 3, 1)));
		section.add(sectionHeader, BorderLayout.NORTH);

		final JButton sectionToggle = new JButton(isOpen ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		sectionToggle.setRolloverIcon(isOpen ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		sectionToggle.setBorder(new EmptyBorder(0, 0, 0, 5));
		sectionToggle.setToolTipText("Expand");
		SwingUtil.removeButtonDecorations(sectionToggle);
		sectionHeader.add(sectionToggle, BorderLayout.WEST);

		final JLabel sectionName = new JLabel(name);
		sectionName.setForeground(ColorScheme.BRAND_ORANGE);
		sectionName.setFont(FontManager.getRunescapeBoldFont());
		sectionName.setAlignmentX(JLabel.WEST);
		sectionName.setToolTipText("<html>" + name + ":<br>" + desc + "</html>");
		sectionHeader.add(sectionName, BorderLayout.CENTER);

		final JPanel sectionContents = new JPanel();
		sectionContents.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		sectionContents.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
		sectionContents.setVisible(isOpen);
		section.add(sectionContents, BorderLayout.SOUTH);

		final MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggleSection(sectionToggle, sectionContents);
				repaint();
				frame.pack();
			}
		};
		sectionToggle.addMouseListener(adapter);
		sectionName.addMouseListener(adapter);
		sectionHeader.addMouseListener(adapter);

		return section;
	}

	private JPanel buildConfig(int i, JPanel section) {
		Config.CONFIGURATION.forEach(config -> {
			if (i != config.getSection()) return;
			JPanel item = new JPanel();
			item.setLayout(new BorderLayout());
			item.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

			String name = config.getText();
			if (config.getType() != Button.class) {
				JLabel configEntryName = new JLabel(name);
				configEntryName.setForeground(Color.WHITE);
				configEntryName.setToolTipText("<html>" + name + ":<br>" + config.getTooltip() + "</html>");
				item.add(configEntryName, BorderLayout.CENTER);
			}

			if (config.getType() == boolean.class) {
				JCheckBox checkbox = new JCheckBox();
				checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
				checkbox.setSelected((boolean) config.getValue());
				checkbox.addActionListener(ae -> changeConfiguration(checkbox, config));
				item.add(checkbox, BorderLayout.EAST);
			}

			if (config.getType() == String.class) {
				JTextComponent textField;
				final JTextArea textArea = new JTextArea();
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				textField = textArea;
				textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				textField.setText("");

				textField.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						changeConfiguration(textField, config);
					}
				});
				item.add(textField, BorderLayout.SOUTH);
			}

			if (config.getType() == Button.class) {
				final JButton button = new JButton(name);
				item.add(button, BorderLayout.SOUTH);
				button.addActionListener(ct -> onButtonClick(button, config));
			}

			if (config.getType() == int.class) {
				int value = (int) config.getValue();
				Range range = config.getRange();
				int min = 0, max = Integer.MAX_VALUE;
				if (range != null) {
					min = range.getMin();
					max = range.getMax();
				}
				value = Ints.constrainToRange(value, min, max);

				SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
				JSpinner spinner = new JSpinner(model);
				Component editor = spinner.getEditor();
				JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
				spinnerTextField.setColumns(SPINNER_FIELD_WIDTH);
				spinner.addChangeListener(ce -> changeConfiguration(spinner, config));
				item.add(spinner, BorderLayout.EAST);
			}
			
			if (config.getType() instanceof Object[]) {
				String[] words = Arrays.stream((String[])config.getType()).map(String::toString).toArray(String[]::new);
				
				Config val = Config.getItem("greater demons");
				String selected = Strings.isNullOrEmpty((String) val.getValue()) ? words[0] : (String) val.getValue();
				
				JComboBox box = new JComboBox(words);
				box.setPreferredSize(new Dimension(box.getPreferredSize().width, 25));
				box.setRenderer(new ComboBoxListRenderer());
				box.setForeground(Color.WHITE);
				box.setFocusable(false);
				box.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXS");
				try {
					box.setSelectedItem(selected);
				} catch (IllegalArgumentException ex) {
					ClientContext.instance().log("invalid selected item", ex);
				}
				box.addItemListener(e -> {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						changeConfiguration(box, config);
						box.setToolTipText(String.valueOf(box.getSelectedItem()));
					}
				});
				item.add(box, BorderLayout.EAST);
				
			} else if (((Class<? extends Enum>) config.getType()).isEnum()) {
				Class<? extends Enum> type = (Class<? extends Enum>) config.getType();
				JComboBox box = new JComboBox(type.getEnumConstants());
				box.setPreferredSize(new Dimension(box.getPreferredSize().width, 25));
				box.setRenderer(new ComboBoxListRenderer());
				box.setForeground(Color.WHITE);
				box.setFocusable(false);
				box.setPrototypeDisplayValue("XXXXXXXX");
				try {
					Config val = Config.getItem(config.getKeyName());
					Enum selectedItem = Enum.valueOf(type, (String) val.getValue());
					box.setSelectedItem(selectedItem);
					box.setToolTipText(Text.titleCase(selectedItem));
				} catch (IllegalArgumentException ex) {
					ClientContext.instance().log("invalid selected item", ex);
				}
				box.addItemListener(e -> {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						changeConfiguration(box, config);
						box.setToolTipText(Text.titleCase((Enum) box.getSelectedItem()));
					}
				});
				item.add(box, BorderLayout.EAST);
			}

			JPanel p = (JPanel) section.getComponent(1);
			p.add(item);
		});
		return this;
	}

	private void onButtonClick(Object component, Config config) {
		if (config == null) return;

		final Config oldConfig = config;

		if (component instanceof JButton) {
			JButton button = (JButton) component;

			// int cached = config.getCached();
			// if (cached > -1) Variables.CACHED_BOOLEANS[cached] = true;

			if (config.getKeyName().equals("testHook")) {
				String url = Config.getValue("discordURL");
				if (Strings.isNullOrEmpty(url)) return;
			}
		}

		ClientContext.instance().log("Clicked " + config.getText() + " button");
		Variables.DISPATCHER.fireEvent(new ConfigChangeEvent(config, oldConfig));
	}

	private Object changeConfiguration(Object component, Config config) {
		if (config == null) return null;

		final Config oldConfig = config;

		if (component instanceof JCheckBox) {
			JCheckBox checkbox = (JCheckBox) component;
			if (Config.isConfigChanged() && !config.isChangeable()) {
				JOptionPane.showMessageDialog(frame, "This setting cannot be changed after run time");
				checkbox.setSelected(!checkbox.isSelected());
				return null;
			}

			config.setValue(checkbox.isSelected());
		} else if (component instanceof JSpinner) {
			JSpinner spinner = (JSpinner) component;
			config.setValue(spinner.getValue());
		} else if (component instanceof JTextComponent) {
			JTextComponent textField = (JTextComponent) component;
			config.setValue(textField.getText());
		} else if (component instanceof JComboBox) {
			JComboBox jComboBox = (JComboBox) component;
			if (jComboBox.getSelectedItem() instanceof String) 
				config.setValue(jComboBox.getSelectedItem());
			else
			config.setValue(((Enum) jComboBox.getSelectedItem()).name());
		}
		ClientContext.instance().log("Changed " + config.getText() + " option");
		Variables.DISPATCHER.fireEvent(new ConfigChangeEvent(config, oldConfig));
		Config.setConfigChanged(true);
		return null;
	}

	public JPanel getButton() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(
				new CompoundBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(0, 0, 3, 1)));
		final JButton start = new JButton("Start");
		start.addActionListener((e) -> {
			if (!Variables.STARTED) {
				Variables.STARTED = true;
				Variables.PAUSED = false;
				start.setText("Pause");
			} else {
				Variables.PAUSED = !Variables.PAUSED;
				start.setText(Variables.PAUSED ? "Resume" : "Pause");
			}

		});
		panel.add(start, BorderLayout.SOUTH);
		return panel;
	}
}
