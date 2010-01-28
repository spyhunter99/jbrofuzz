/**
 * JBroFuzz 1.9
 *
 * JBroFuzz - A stateless network protocol fuzzer for web applications.
 * 
 * Copyright (C) 2007 - 2010 subere@uncon.org
 *
 * This file is part of JBroFuzz.
 * 
 * JBroFuzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JBroFuzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JBroFuzz.  If not, see <http://www.gnu.org/licenses/>.
 * Alternatively, write to the Free Software Foundation, Inc., 51 
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Verbatim copying and distribution of this entire program file is 
 * permitted in any medium without royalty provided this notice 
 * is preserved. 
 * 
 */
package org.owasp.jbrofuzz.encode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


import org.apache.commons.lang.StringEscapeUtils;
import org.owasp.jbrofuzz.JBroFuzz;
import org.owasp.jbrofuzz.ui.JBroFuzzWindow;
import org.owasp.jbrofuzz.util.B64;
import org.owasp.jbrofuzz.util.ImageCreator;
import org.owasp.jbrofuzz.version.JBroFuzzFormat;

/**
 * <p>
 * Window inspired from Paros Proxy, in terms of providing an encoder/decoder
 * for a variety of different schemes, as well as hashing functionality.
 * </p>
 * 
 * @author subere@uncon.org
 * @version 1.8
 * @since 1.5
 * 
 */
public class EncoderHashFrame extends JFrame {

	private static final long serialVersionUID = 8808832051334720865L;
	// Dimensions of the frame
	private static final int SIZE_X = 650;
	private static final int SIZE_Y = 400;

	private JSplitPane verticalSplitPane, horizontalSplitPane;

	private JTextPane enTextPane, deTextPane;

	// The tree
	private JTree tree;

	private JButton encode, decode, close;

	private static boolean windowIsShowing = false;

	public EncoderHashFrame(final JBroFuzzWindow parent) {
		
		if (windowIsShowing) {
			return;
		}
		windowIsShowing = true;

		// really inspired from Paros Proxy, but as a frame
		setTitle(" JBroFuzz - Encoder/Hash ");

		setIconImage(ImageCreator.IMG_FRAME.getImage());
		setLayout(new BorderLayout());
		setFont(new Font("SansSerif", Font.PLAIN, 12));

		// Create the nodes
		final DefaultMutableTreeNode top = new DefaultMutableTreeNode("Codes/Hashes");
		// Create a tree that allows one selection at a time
		tree = new JTree(top);
		tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// Selection can only contain one path at a time
		tree.getSelectionModel().setSelectionMode(1);

		// Create the scroll pane and add the tree to it.
		final JScrollPane leftScrollPane = new JScrollPane(tree);

		// Create all the right hand panels
		for (int i = 0; i < EncoderHashCore.CODES.length; i++) {
				top.add(new DefaultMutableTreeNode(EncoderHashCore.CODES[i]));
		}

		final JPanel encoderPanel = new JPanel(new BorderLayout());
		final JPanel decoderPanel = new JPanel(new BorderLayout());

		encoderPanel
		.setBorder(BorderFactory
				.createCompoundBorder(
						BorderFactory
						.createTitledBorder(" Enter the plain text below to be encoded / hashed "),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		decoderPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(" Enter the text below to be decoded "),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// Text panes -> Encode
		enTextPane = new JTextPane();

		enTextPane.putClientProperty("charset", "UTF-8");
		enTextPane.setEditable(true);
		enTextPane.setVisible(true);
		enTextPane.setFont(new Font("Verdana", Font.PLAIN, 12));

		enTextPane.setMargin(new Insets(1, 1, 1, 1));
		enTextPane.setBackground(Color.WHITE);
		enTextPane.setForeground(new Color(51, 102, 102));

		// Set the right click for the encode text area
		popupText(enTextPane);

		final JScrollPane encodeScrollPane = new JScrollPane(enTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		encoderPanel.add(encodeScrollPane, BorderLayout.CENTER);

		// Text panes -> Decode
		deTextPane = new JTextPane();

		deTextPane.putClientProperty("charset", "UTF-8");
		deTextPane.setEditable(true);
		deTextPane.setVisible(true);
		deTextPane.setFont(new Font("Verdana", Font.PLAIN, 12));

		deTextPane.setMargin(new Insets(1, 1, 1, 1));
		deTextPane.setBackground(Color.WHITE);
		deTextPane.setForeground(new Color(204, 51, 0));

		// Set the right click for the decode text area
		popupText(deTextPane);

		final JScrollPane decodeScrollPane = new JScrollPane(deTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		decoderPanel.add(decodeScrollPane, BorderLayout.CENTER);

		horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		horizontalSplitPane.setLeftComponent(leftScrollPane);
		horizontalSplitPane.setRightComponent(verticalSplitPane);

		verticalSplitPane.setTopComponent(encoderPanel);
		verticalSplitPane.setBottomComponent(decoderPanel);

		// Set the minimum size for all components
		final Dimension minimumSize = new Dimension(0, 0);
		leftScrollPane.setMinimumSize(minimumSize);
		verticalSplitPane.setMinimumSize(minimumSize);
		encoderPanel.setMinimumSize(minimumSize);
		decoderPanel.setMinimumSize(minimumSize);

		horizontalSplitPane.setDividerLocation(180);
		verticalSplitPane.setDividerLocation(SIZE_Y / 2);

		// Traverse tree from root
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		parent.getPanelPayloads().expandAll(tree, new TreePath(root), true);

		// Bottom three buttons
		encode = new JButton(" Encode/Hash ");
		decode = new JButton(" Decode ");
		close = new JButton(" Close ");

		final String desc = "Select an encoding or hashing scheme from the left hard side";
		encode.setToolTipText(desc);
		decode.setToolTipText(desc);

		encode.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						calculate(true);

					}
				});
			}
		});

		decode.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						calculate(false);

					}
				});
			}
		});

		close.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						windowIsShowing = false;

						// Save the values of the encode/decode as a preference
						JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
						JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

						dispose();

					}
				});
			}
		});

		// Keyboard listener on the treeView for escape to cancel
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		// Keyboard listener on the treeView for Ctrl+Return to Encode
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});


		// Keyboard listener on the decoded text pane for escape to cancel
		deTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		// Keyboard listener on the encoded text pane for escape to cancel
		enTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		// Keyboard listeners on the buttons for escape to cancel
		encode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		decode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		close.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent ke) {
				if (ke.getKeyCode() == 27) {

					windowIsShowing = false;

					// Save the values of the encode/decode as a preference
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
					JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

					dispose();

				}
			}
		});

		// Bottom buttons

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,
				15, 15));
		buttonPanel.add(encode);
		buttonPanel.add(decode);
		buttonPanel.add(close);

		// Add the split pane to this panel
		getContentPane().add(horizontalSplitPane, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Global frame issues
		this.setLocation(Math.abs(parent.getLocation().x + 100), Math
				.abs(parent.getLocation().y + 100));

		this.setSize(EncoderHashFrame.SIZE_X, EncoderHashFrame.SIZE_Y);
		setMinimumSize(new Dimension(SIZE_X / 2, SIZE_Y / 2));

		setResizable(true);
		setVisible(true);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				windowIsShowing = false;

				// Save the values of the encode/decode as a preference
				JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_ENCODE, enTextPane.getText());
				JBroFuzz.PREFS.put(JBroFuzzFormat.TEXT_DECODE, deTextPane.getText());

				dispose();
			}
		});

		// Load the values of encode/decode from the preferences
		enTextPane.setText(JBroFuzz.PREFS.get(JBroFuzzFormat.TEXT_ENCODE, ""));
		deTextPane.setText(JBroFuzz.PREFS.get(JBroFuzzFormat.TEXT_DECODE, ""));

	}

	/**
	 * <p>
	 * Calculate the value to be encoded/decoded, based on the selected scheme
	 * from the left hand side tree.
	 * </p>
	 * 
	 * @param enDecode
	 *            false implies decode true implies encode
	 * 
	 * @version 1.6
	 * @since 1.5
	 */
	public void calculate(boolean isToEncode) {

		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
		.getLastSelectedPathComponent();

		if (node == null) {
			return;
		}
		
		final String s = node.toString();
		
		if (isToEncode) {
			final String encodeText = enTextPane.getText();
			deTextPane.setText(EncoderHashCore.encode(encodeText, s));
		} else {
			final String decodeText = deTextPane.getText();
			enTextPane.setText(EncoderHashCore.decode(decodeText, s));
		}
	}

	/**
	 * <p>
	 * Method for setting up the right click copy paste cut and select all menu.
	 * </p>
	 * <p>
	 * It passes the parameters of which options in the right click menu are
	 * enabled.
	 * </p>
	 * 
	 * @param area
	 *            JTextComponent
	 */
	public final void popupText(final JTextComponent area) {

		final JPopupMenu popmenu = new JPopupMenu();

		final JMenuItem i1_cut = new JMenuItem("Cut");
		final JMenuItem i2_copy = new JMenuItem("Copy");
		final JMenuItem i3_paste = new JMenuItem("Paste");
		final JMenuItem i4_select = new JMenuItem("Select All");

		i1_cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		i2_copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		i3_paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		i4_select.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));

		popmenu.add(i1_cut);
		popmenu.add(i2_copy);
		popmenu.add(i3_paste);
		popmenu.addSeparator();
		popmenu.add(i4_select);

		i1_cut.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.cut();
			}
		});

		i2_copy.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.copy();
			}
		});

		i3_paste.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (area.isEditable()) {
					area.paste();
				}
			}
		});

		i4_select.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.selectAll();
			}
		});

		area.addMouseListener(new MouseAdapter() {
			private void checkForTriggerEvent(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					area.requestFocus();
					popmenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				checkForTriggerEvent(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				checkForTriggerEvent(e);
			}
		});
	}
}
