import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HelpDesk extends JFrame implements ActionListener {
    JTextField usernameField, problemField;
    JPasswordField passwordField;
    JButton loginButton, submitButton, viewTicketButton;
    JComboBox<String> ticketTypeComboBox;
    JTextArea solutionTextArea;
    JTable ticketTable;
    Connection conn;

    public HelpDesk() {
        setTitle("Help Desk System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField);
        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        loginPanel.add(loginButton);

        // Ticket Panel
        JPanel ticketPanel = new JPanel();
        ticketPanel.add(new JLabel("Problem:"));
        problemField = new JTextField(20);
        ticketPanel.add(problemField);
        String[] ticketTypes = {"Technical", "Non-Technical"};
        ticketTypeComboBox = new JComboBox<>(ticketTypes);
        ticketPanel.add(new JLabel("Ticket Type:"));
        ticketPanel.add(ticketTypeComboBox);
        submitButton = new JButton("Submit Ticket");
        submitButton.addActionListener(this);
        submitButton.setEnabled(false);
        ticketPanel.add(submitButton);

        // Solution Panel
        JPanel solutionPanel = new JPanel();
        solutionPanel.add(new JLabel("Solution:"));
        solutionTextArea = new JTextArea(10, 20);
        solutionPanel.add(new JScrollPane(solutionTextArea));

        // Ticket Table Panel
        JPanel ticketTablePanel = new JPanel();
        String[] columnNames = {"Ticket ID", "Problem", "Ticket Type", "Status"};
        ticketTable = new JTable(new DefaultTableModel(columnNames, 0));
        ticketTablePanel.add(new JScrollPane(ticketTable));
        viewTicketButton = new JButton("View Ticket");
        viewTicketButton.addActionListener(this);
        viewTicketButton.setEnabled(false);
        ticketTablePanel.add(viewTicketButton);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
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
        setJMenuBar(menuBar);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Raise Ticket", ticketPanel);
        tabbedPane.addTab("View Tickets", ticketTablePanel);
        tabbedPane.addTab("Solutions", solutionPanel);
        add(tabbedPane, BorderLayout.CENTER);

        // Database Connection
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk1", "root", "Sonu@123");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful");
                submitButton.setEnabled(true);
                viewTicketButton.setEnabled(true);
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
                loginButton.setEnabled(false);
                viewTickets(); // Update ticket table on successful login
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials");
                passwordField.setText(""); // Clear password field on failed login
            }
        } else if (e.getSource() == submitButton) {
            String problem = problemField.getText();
            String ticketType = (String) ticketTypeComboBox.getSelectedItem();
            if (raiseTicket(problem, ticketType)) {
                JOptionPane.showMessageDialog(this, "Ticket Raised Successfully");
                problemField.setText(""); // Clear problem field after ticket submission
                viewTickets(); // Refresh tickets after raising a new one
            } else {
                JOptionPane.showMessageDialog(this, "Failed to Raise Ticket");
            }
        } else if (e.getSource() == viewTicketButton) {
            viewTickets();
        } else if (e.getActionCommand().equals("Logout")) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                logout();
            }
        } else if (e.getActionCommand().equals("Exit")) {
            System.exit(0);
        } else if (e.getActionCommand().equals("Raise Ticket")) {
            JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            tabbedPane.setSelectedIndex(1);
        } else if (e.getActionCommand().equals("View Tickets")) {
            JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            tabbedPane.setSelectedIndex(2);
            viewTickets();
        }
    }

    public boolean authenticateUser(String username, String password) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean raiseTicket(String problem, String ticketType) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Tickets (problem, ticketType) VALUES (?, ?)")) {
            stmt.setString(1, problem);
            stmt.setString(2, ticketType);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void viewTickets() {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Tickets")) {
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) ticketTable.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[] {rs.getInt("ticketID"), rs.getString("problem"), rs.getString("ticketType"), rs.getString("status")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void logout() {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        submitButton.setEnabled(false);
        viewTicketButton.setEnabled(false);
        usernameField.setText("");
        passwordField.setText("");
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HelpDesk().setVisible(true));
    }
}
