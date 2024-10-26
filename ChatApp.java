import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ChatApp extends JFrame {
    private static final long serialVersionUID = 1L;
    Connection connection;
    JTextField messageField;
    JTextArea messageArea;
    JButton sendButton, videoCallButton, clearChatButton;
    String currentUser;
    Timer messagePollingTimer;
    Set<Integer> displayedMessageIds; // To store already displayed message IDs
    JList<String> userList; // JList for the recipient users
    DefaultListModel<String> userListModel; // Model for the list

    // Constructor accepting the username
    public ChatApp(String username) {
        currentUser = username; // Set the current user
        connectDatabase();
        displayedMessageIds = new HashSet<>(); // Initialize the set to track displayed messages

        // Create the main layout panel
        JPanel mainPanel = new BackgroundPanel(); // Use the custom BackgroundPanel with color
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the message area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder("Messages"));
        mainPanel.add(messageScrollPane, BorderLayout.CENTER);

        // Create the left panel for recipient list (User list)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Recipients"));

        // Initialize the user list model and JList
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add scroll pane to the user list
        JScrollPane userScrollPane = new JScrollPane(userList);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        fetchUsers(); // Fetch users to populate the recipient list

        // Add the left panel to the main layout on the left side
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Create message sending panel at the bottom
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.setBorder(BorderFactory.createTitledBorder("Send Message"));

        // Create the message input field
        messageField = new JTextField();
        sendPanel.add(messageField, BorderLayout.CENTER); // Add text field to the center

        // Create the send button
        sendButton = new JButton("Send");
        sendPanel.add(sendButton, BorderLayout.EAST); // Add button to the east

        // Video call button
        videoCallButton = new JButton("Start Video Call");
        sendPanel.add(videoCallButton, BorderLayout.SOUTH); // Adjusted to avoid overlap

        // Clear chat button
        clearChatButton = new JButton("Clear Chat");
        sendPanel.add(clearChatButton, BorderLayout.WEST); // Add clear chat button to the west

        mainPanel.add(sendPanel, BorderLayout.SOUTH);

        // Add the main panel to the frame
        add(mainPanel);

        // Event listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        videoCallButton.addActionListener(e -> startVideoCall());
        clearChatButton.addActionListener(e -> messageArea.setText("")); // Clear chat area

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null) {
                    messageArea.append("Chatting with: " + selectedUser + "\n");
                }
            }
        });

        // Setup window
        setTitle("Chat Application - " + currentUser);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Start fetching messages after loading the chat app
        startMessagePolling();
    }

    private void fetchUsers() {
        try {
            PreparedStatement pst = connection.prepareStatement("SELECT username FROM users");
            ResultSet rs = pst.executeQuery();

            userListModel.clear(); // Clear previous users
            while (rs.next()) {
                String username = rs.getString("username");
                userListModel.addElement(username); // Add only the username to the list
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch users: " + e.getMessage());
        }
    }

    private void connectDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatdata_b", "bharath", "Bharath@1608");
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String recipient = userList.getSelectedValue(); // Use selected user from the list
        String message = messageField.getText();

        if (recipient == null || message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a recipient and type a message.");
            return;
        }

        try {
            PreparedStatement pst = connection.prepareStatement("INSERT INTO messages(sender, recipient, message) VALUES(?, ?, ?)");
            pst.setString(1, currentUser);
            pst.setString(2, recipient);
            pst.setString(3, message);
            pst.executeUpdate();

            messageArea.append("To " + recipient + ": " + message + "\n");
            messageField.setText(""); // Clear the message field after sending
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Message sending failed: " + e.getMessage());
        }
    }

    private void startMessagePolling() {
        messagePollingTimer = new Timer(3000, e -> fetchMessages()); // Fetch messages every 3 seconds
        messagePollingTimer.start();
    }

    private void fetchMessages() {
        try {
            PreparedStatement pst = connection.prepareStatement("SELECT id, sender, message FROM messages WHERE recipient = ?");
            pst.setString(1, currentUser);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int messageId = rs.getInt("id");
                String sender = rs.getString("sender");
                String message = rs.getString("message");

                // Only display the message if it has not been displayed already
                if (!displayedMessageIds.contains(messageId)) {
                    messageArea.append("From " + sender + ": " + message + "\n");
                    displayedMessageIds.add(messageId); // Add to the set to avoid re-displaying
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch messages: " + e.getMessage());
        }
    }

    private void startVideoCall() {
        String roomName = JOptionPane.showInputDialog(this, "Enter Room Name for Video Call:");
        if (roomName != null && !roomName.isEmpty()) {
            String jitsiMeetUrl = "https://meet.jit.si/" + roomName;
            try {
                Desktop.getDesktop().browse(new java.net.URI(jitsiMeetUrl));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to open video call: " + e.getMessage());
            }
        }
    }

    // Custom JPanel class for background color
    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Set a background color 
            g.setColor(new Color(220, 248, 198));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}