import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HelpDesk extends JFrame implements ActionListener {
    // GUI Components
    private JTextField usernameField, problemField;
    private JPasswordField passwordField;
    private JButton loginButton, submitButton, viewTicketButton;
    private JComboBox<String> ticketTypeComboBox;
    private JTextArea solutionTextArea;
    private JTable ticketTable;
    
    // Database Connection
    private Connection conn;

    public HelpDesk() {
        setTitle("Help Desk System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize and set up panels
        JPanel loginPanel = createLoginPanel();
        JPanel ticketPanel = createTicketPanel();
        JPanel solutionPanel = createSolutionPanel();
        JPanel ticketTablePanel = createTicketTablePanel();

        // Menu Bar setup
        JMenuBar menuBar = new JMenuBar();
        setUpMenu(menuBar);
        setJMenuBar(menuBar);

        // Tabbed Pane setup
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Raise Ticket", ticketPanel);
        tabbedPane.addTab("View Tickets", ticketTablePanel);
        tabbedPane.addTab("Solutions", solutionPanel);
        add(tabbedPane, BorderLayout.CENTER);

        // Establish database connection
        connectToDatabase();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        panel.add(loginButton);
        return panel;
    }

    private JPanel createTicketPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Problem:"));
        problemField = new JTextField(20);
        panel.add(problemField);

        String[] ticketTypes = {"Technical", "Non-Technical"};
        ticketTypeComboBox = new JComboBox<>(ticketTypes);
        panel.add(new JLabel("Ticket Type:"));
        panel.add(ticketTypeComboBox);

        submitButton = new JButton("Submit Ticket");
        submitButton.addActionListener(this);
        submitButton.setEnabled(false);
        panel.add(submitButton);
        return panel;
    }

    private JPanel createSolutionPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Solution:"));
        solutionTextArea = new JTextArea(10, 20);
        solutionTextArea.setEditable(false);
        panel.add(new JScrollPane(solutionTextArea));
        return panel;
    }

    private JPanel createTicketTablePanel() {
        JPanel panel = new JPanel();
        String[] columnNames = {"Ticket ID", "Problem", "Ticket Type", "Status"};
        ticketTable = new JTable(new DefaultTableModel(columnNames, 0));
        panel.add(new JScrollPane(ticketTable));

        viewTicketButton = new JButton("View Ticket");
        viewTicketButton.addActionListener(this);
        viewTicketButton.setEnabled(false);
        panel.add(viewTicketButton);
        return panel;
    }

    private void setUpMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(this);
        fileMenu.add(logoutItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu ticketMenu = new JMenu("Ticket");
        JMenuItem raiseTicketItem = new JMenuItem("Raise Ticket");
        raiseTicketItem.addActionListener(this);
        ticketMenu.add(raiseTicketItem);

        JMenuItem viewTicketsItem = new JMenuItem("View Tickets");
        viewTicketsItem.addActionListener(this);
        ticketMenu.add(viewTicketsItem);
        menuBar.add(ticketMenu);
    }

    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk1", "root", "Sonu@123");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
    
        if (source == loginButton) {
            handleLogin();
        } else if (source == submitButton) {
            handleSubmitTicket();
        } else if (source == viewTicketButton) {
            viewTickets();
        } else if (source instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) source;
            String command = menuItem.getActionCommand();
            if (command.equals("Logout")) {
                handleLogout();
            } else if (command.equals("Exit")) {
                System.exit(0);
            } else if (command.equals("View Tickets")) {
                viewTickets();
            } else if (command.equals("Raise Ticket")) {
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
                tabbedPane.setSelectedIndex(1);
            }
        } else if (source == updateStatusButton) {
            handleUpdateTicketStatus();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful");
            submitButton.setEnabled(true);
            viewTicketButton.setEnabled(true);
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
            loginButton.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Credentials");
        }
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void handleSubmitTicket() {
        String problem = problemField.getText();
        String ticketType = (String) ticketTypeComboBox.getSelectedItem();
        if (raiseTicket(problem, ticketType)) {
            JOptionPane.showMessageDialog(this, "Ticket Raised Successfully");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to Raise Ticket");
        }
    }

    private boolean raiseTicket(String problem, String ticketType) {
        String query = "INSERT INTO Tickets (problem, ticketType, status) VALUES (?, ?, 'Open')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, problem);
            stmt.setString(2, ticketType);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void viewTickets() {
        String query = "SELECT * FROM Tickets";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) ticketTable.getModel();
            model.setRowCount(0); // Clear existing rows
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ticketID"),
                    rs.getString("problem"),
                    rs.getString("ticketType"),
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleLogout() {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        submitButton.setEnabled(false);
        viewTicketButton.setEnabled(false);
        usernameField.setText("");
        passwordField.setText("");
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(0); // Switch back to login tab
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HelpDesk().setVisible(true);
        });
    }
}
