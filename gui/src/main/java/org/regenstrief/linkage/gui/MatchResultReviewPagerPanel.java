package org.regenstrief.linkage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.db.SavedResultDBConnection;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;

public class MatchResultReviewPagerPanel extends JPanel implements ActionListener, WindowListener{

	private static final long serialVersionUID = 1L;
	public static int VIEWS_PER_PAGE = 5;
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private int view_index;
	private final List<MatchResultReviewPanel> rpanels = new ArrayList<MatchResultReviewPanel>();
	private DBMatchResultStore mrs;
	private DateFormat ui_datefmt;
	
	private JButton next_page, prev_page, goto_mr, goto_first_unreviewed, open, save;
	private JTextField row, first_unreviewed, total;
	
	private final Hashtable<Integer, MatchResult> reviewed_match_results = new Hashtable<Integer, MatchResult>();
	
	private Connection db;
	
	private static String db_file_path;
	private static PrintStream review_log;
	
	public MatchResultReviewPagerPanel() {
		super();
		mrs = null;
		ui_datefmt = new SimpleDateFormat(DATE_FORMAT);
		view_index = 0;
		initGUI();
	}
	
	private final void setOrder(final List<String> order) {
		for (final MatchResultReviewPanel mrrp : rpanels) {
			mrrp.setOrder(order);
		}
	}
	
	public final int getViewIndex() {
		return view_index;
	}
	
	private final void setNonDisplay(final Set<String> fields) {
		for (final MatchResultReviewPanel mrrp : rpanels) {
			mrrp.setNonDisplayFields(fields);
		}
	}
	
	private final Date chooseDate(final List<Date> options) {
		// Date object toStrings can be ambiguous, so create more precise string
		final Hashtable<String,Date> option_dates = new Hashtable<String, Date>();
		
		for (final Date d : options) {
			option_dates.put(ui_datefmt.format(d), d);
		}
		
		final Object[] o = option_dates.keySet().toArray();
		final String choice = (String) JOptionPane.showInputDialog(
                this,
                "Multiple runs exist in review database.\nWhich run to open?",
                "Select date",
                JOptionPane.PLAIN_MESSAGE,
                null,
                o,
                o[0]);
		return (choice == null) ? null : option_dates.get(choice);
	}
	
	private final void initGUI() {
		setLayout(new BorderLayout());
		
		// add top section
		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		open = new JButton("Open File");
		open.addActionListener(this);
		save = new JButton("Save Changes");
		save.addActionListener(this);
		final JLabel first = new JLabel("First Unreviewed Row:");
		first_unreviewed = new JTextField(6);
		first_unreviewed.setEditable(false);
		final JLabel total_label = new JLabel("Total Rows:");
		total = new JTextField(6);
		total.setEditable(false);
		
		add(top, open);
		add(top, save);
		add(top, first);
		add(top, first_unreviewed);
		add(top, total_label);
		add(top, total);
		top.add(Box.createHorizontalGlue());
		
		final JPanel ttop = new JPanel();
		ttop.setLayout(new BorderLayout());
		ttop.add(top, BorderLayout.LINE_START);
		add(ttop, BorderLayout.PAGE_START);
		
		// create review panels
		final JPanel middle = new JPanel();
		MatchResultReviewKeyboardAccelerator.INSTANCE.setReviewPanelList(rpanels);
		middle.setLayout(new BoxLayout(middle, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < VIEWS_PER_PAGE; i++) {
			final String[] dummy_header = new String[8];
			for (int j = 0; j < dummy_header.length; j++) {
				dummy_header[j] = "field";
			}
			final MatchResultReviewPanel mrrp = new MatchResultReviewPanel(dummy_header);
			rpanels.add(mrrp);
			middle.add(mrrp);
			middle.add(new JSeparator(JSeparator.HORIZONTAL));
		}
		final JScrollPane jsp = new JScrollPane(middle);
		//this.add(middle, BorderLayout.CENTER);
		add(jsp, BorderLayout.CENTER);
		
		// create paging buttons
		final JPanel bottom = new JPanel();
		prev_page = new JButton("Previous");
		prev_page.addActionListener(this);
		next_page = new JButton("Next");
		next_page.addActionListener(this);
		goto_mr = new JButton("Goto Row");
		goto_mr.addActionListener(this);
		row = new JTextField(4);
		goto_first_unreviewed = new JButton("Goto Unreviewed");
		goto_first_unreviewed.addActionListener(this);
		bottom.add(prev_page);
		add(bottom, next_page);
		add(bottom, goto_mr);
		add(bottom, row);
		add(bottom, goto_first_unreviewed);
		
		add(bottom, BorderLayout.PAGE_END);
		MatchResultReviewKeyboardAccelerator.INSTANCE.setPagerPanel(this);
	}
	
	private final static void add(final JPanel panel, final Component comp) {
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(comp);
	}
	
	public final static void main(final String[] args) {
		final JFrame window = new JFrame();
		window.setSize(1200, 800);
		final MatchResultReviewPagerPanel mrrpp = new MatchResultReviewPagerPanel();
		window.add(mrrpp);
		window.setVisible(true);
		
		// parse arguments
		if (args.length >= 1) {
			mrrpp.setOrder(parseOrder(args[0]));
		}
		if (args.length >= 2) {
			mrrpp.setNonDisplay(parseNoDisplay(args[1]));
		}
		
		// close database on exit, so implement window listener to close connection on exit
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addWindowListener(mrrpp);
	}
	
	private final static List<String> parseOrder(final String order) {
		return Arrays.asList(order.split(","));
	}
	
	private final static Set<String> parseNoDisplay(final String fields) {
		final String[] f = fields.split(",");
		final int size = f.length;
		final Set<String> ret = new HashSet<String>(size);
		for (int i = 0; i < size; i++) {
			ret.add(f[i]);
		}
		return ret;
	}
	
	public final DBMatchResultStore getMatchResultStore() {
		return mrs;
	}
	
	public final void setMatchResultStore(final DBMatchResultStore mrs) {
		this.mrs = mrs;
	}
	
	public final void updateView(final int index) {
		for (MatchResultReviewPanel rpanel : rpanels) {
			rpanel.clearFocus();
		}
		// get VIEWS_PER_PAGE MatchResults out of MatchResult store beginning at given index
		// set GUI elements to show new MatchResults
		view_index = index;
		if (index < 0) {
			view_index = 0;
		}
		final int size = mrs.getSize();
		if (index > size - VIEWS_PER_PAGE) {
			view_index = size - VIEWS_PER_PAGE;
			if (view_index < 0) {
				view_index = 0;
			}
		}
		for (int i = 0; i < rpanels.size(); i++) {
			final MatchResultReviewPanel mrrp = rpanels.get(i);
			final Integer key = Integer.valueOf(view_index + i);
			MatchResult mr = reviewed_match_results.get(key);
			if (mr == null) {
				mr = mrs.getMatchResult(view_index + i);
			}
			if (mr != null) {
				mrrp.setMatchResult(mr);
				mrrp.setRow(view_index + i);
				
				reviewed_match_results.put(key, mr);
			}
		}
		MatchResultReviewKeyboardAccelerator.INSTANCE.setFocusIndex(0);
	}
	
	private final void showPrevPage() {
		updateView(view_index - VIEWS_PER_PAGE);
	}
	
	private final void showNextPage() {
		updateView(view_index + VIEWS_PER_PAGE);
	}
	
	private final void setTotal() {
		total.setText(Integer.toString(mrs.getSize()));
	}
	
	private final void setFirstUnreviewed() {
		first_unreviewed.setText(Integer.toString(mrs.getMinUnknownID()));
	}
	
	private final void saveChanges() {
		// iterate over MatchResult objects in reviewed_match_results and update or delete/add them to persist possible changes
		if (mrs != null) {
			for (final Entry<Integer, MatchResult> entry : reviewed_match_results.entrySet()) {
				final int id = entry.getKey().intValue();
				final MatchResult reviewed = entry.getValue();
				mrs.updateMatchResult(id, reviewed.getNote(), reviewed.getMatch_status(), reviewed.getCertainty());
			}
			reviewed_match_results.clear();
			refreshCurrentMatchResults();
			closeReviewLog();
		}
	}
	
	private final void refreshCurrentMatchResults() {
		final int size = rpanels.size();
		for (int i = 0; i < size; i++) {
			reviewed_match_results.put(Integer.valueOf(view_index + i), rpanels.get(i).mr);
		}
	}
	
	protected final static void printReviewLog(final Object s) {
		try {
			if (review_log == null) {
				review_log = new PrintStream(new FileOutputStream(db_file_path + ".review.log", true), true);
			}
			review_log.println(s);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final static void closeReviewLog() {
		if (review_log == null) {
			return;
		}
		review_log.flush();
		review_log.close();
		review_log = null;
	}
	
	public final void closeDBConnection() {
		if (mrs != null) {
			mrs.close();
		}
		if (db != null) {
			try {
				db.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public final boolean isOnFirstPage() {
		return view_index == 0;
	}
	
	public final boolean isOnLastPage() {
		return mrs.getSize() < VIEWS_PER_PAGE || view_index >= mrs.getSize() - VIEWS_PER_PAGE;
	}
	
	private final String getLastDirectoryConfigFile() {
		return System.getProperty("user.home") + System.getProperty("file.separator") + "manrevdir.cfg";
	}
	
	private final String getLastDirectory() throws IOException {
		final FileReader r;
		try {
			r = new FileReader(getLastDirectoryConfigFile());
		} catch (final FileNotFoundException e) {
			return null;
		}
		final String lastDirectory;
		try {
			final BufferedReader b = new BufferedReader(r);
			lastDirectory = b.readLine();
			b.close();
		} finally {
			r.close();
		}
		return lastDirectory;
	}
	
	private final void setLastDirectory(final String lastDirectory) throws IOException {
		final FileWriter w = new FileWriter(getLastDirectoryConfigFile());
		try {
			w.write(lastDirectory);
			w.flush();
		} finally {
			w.close();
		}
	}

	@Override
	public final void actionPerformed(final ActionEvent ae) {
		try {
			actionPerformedEx(ae);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final void actionPerformedEx(final ActionEvent ae) throws IOException {
		if (ae.getSource() instanceof JButton) {
			final JButton source = (JButton) ae.getSource();
			if (source == open) {
				// get MatchResultStore set and updateview
				final JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jfc.setFileFilter(new FileNameExtensionFilter("SQLite database file", "db"));
				final String lastDirectory = getLastDirectory();
				if (lastDirectory != null) {
					jfc.setCurrentDirectory(new File(lastDirectory));
				}
				final int retval = jfc.showOpenDialog(this);
				if (retval == JFileChooser.APPROVE_OPTION) {
					info("Closing previous file (if needed)");
					closeDBConnection();
					closeReviewLog();
					final File db_file = jfc.getSelectedFile();
					info("Saving last directory");
					setLastDirectory(db_file.getParentFile().getAbsolutePath());
					db_file_path = db_file.getAbsolutePath();
					info("Opening " + db_file_path);
					db = SavedResultDBConnection.openDBResults(db_file);
					info("Validating " + db_file_path + " tables");
					SavedResultDBConnection.validateMatchResultTables(db);
					if (db != null) {
						info("Creating DBMatchResultStore");
						mrs = new DBMatchResultStore(db);
						info("Adding indexes (if needed)");
						mrs.addIndexes();
						info("Getting Dates");
						final List<Date> dates = mrs.getDates();
						if (dates.size() > 1) {
							mrs.setDate(chooseDate(dates));
						} else {
							mrs.setDate(dates.get(0));
						}
						reviewed_match_results.clear();
						updateView(0);
						info("Setting first unreviewed");
						setFirstUnreviewed();
						info("Setting total");
						setTotal();
					}
				}
			} else if (source == next_page) {
				showNextPage();
			} else if (source == prev_page) {
				showPrevPage();
			} else if (source == save) {
				final MatchResultReviewPanel focused = getFocusedPanel();
				saveChanges();
				setFirstUnreviewed();
				if (focused != null) {
					focused.setFocus();
				}
			} else if (source == goto_mr) {
				try{
					updateView(Integer.parseInt(row.getText()));
				} catch(NumberFormatException nfe) {
				}
			} else if (source == goto_first_unreviewed) {
				try{
					updateView(Integer.parseInt(first_unreviewed.getText()));
				} catch(NumberFormatException nfe) {
				}
			}
		}
	}
	
	private final MatchResultReviewPanel getFocusedPanel() {
		for (final MatchResultReviewPanel panel : this.rpanels) {
			if (panel.focused) {
				return panel;
			}
		}
		return null;
	}

	@Override
	public final void windowActivated(final WindowEvent arg0) {
	}

	@Override
	public final void windowClosed(final WindowEvent arg0) {
	}

	@Override
	public final void windowClosing(final WindowEvent arg0) {
		if (db != null) {
			final int n = JOptionPane.showConfirmDialog(this, "Save review changes before closing?", "Exitting program", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				saveChanges();
			}
			closeDBConnection();
		}
	}

	@Override
	public final void windowDeactivated(final WindowEvent arg0) {
	}

	@Override
	public final void windowDeiconified(final WindowEvent arg0) {
	}

	@Override
	public final void windowIconified(final WindowEvent arg0) {
	}

	@Override
	public final void windowOpened(final WindowEvent arg0) {
	}
	
	private final static void info(final Object s) {
		System.out.println(new Date() + " - " + s);
	}
}
