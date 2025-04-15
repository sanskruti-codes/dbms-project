import java.awt.*;
import java.util.List;
import javax.swing.*;

public class MainFrame extends JFrame {
    private int userId;
    private JList<Package> packageList;
    private JList<Package> bookedPackageList;
    private JTextArea descriptionArea;
    private JTextArea bookedDescriptionArea;

    public MainFrame(int userId) {
        this.userId = userId;
        setTitle("Tourism Management System - Tour Packages");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Available Packages Panel
        JSplitPane availablePackagesPanel = createAvailablePackagesPanel();
        tabbedPane.addTab("Available Packages", availablePackagesPanel);

        // Booked Packages Panel
        JSplitPane bookedPackagesPanel = createBookedPackagesPanel();
        tabbedPane.addTab("My Bookings", bookedPackagesPanel);

        add(tabbedPane);
    }

    private JSplitPane createAvailablePackagesPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left panel - Package list
        JPanel leftPanel = new JPanel(new BorderLayout());
        packageList = new JList<>(getPackagesArray());
        packageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(packageList);
        leftPanel.add(new JLabel("Available Packages", SwingConstants.CENTER), BorderLayout.NORTH);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Right panel - Package details and booking
        JPanel rightPanel = new JPanel(new BorderLayout());
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);

        JButton bookButton = new JButton("Book Package");
        JButton deleteAccountButton = new JButton("Delete Account");
        JButton logoutButton = new JButton("Logout");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(bookButton);
        buttonPanel.add(deleteAccountButton);
        buttonPanel.add(logoutButton);

        rightPanel.add(new JLabel("Package Details", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(descScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(300);

        // Add listeners
        addAvailablePackagesListeners(packageList, descriptionArea, bookButton, deleteAccountButton, logoutButton);

        return splitPane;
    }

    private JSplitPane createBookedPackagesPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left panel - Booked Package list
        JPanel leftPanel = new JPanel(new BorderLayout());
        bookedPackageList = new JList<>(getBookedPackagesArray());
        bookedPackageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(bookedPackageList);
        leftPanel.add(new JLabel("My Bookings", SwingConstants.CENTER), BorderLayout.NORTH);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Right panel - Package details and cancel
        JPanel rightPanel = new JPanel(new BorderLayout());
        bookedDescriptionArea = new JTextArea();
        bookedDescriptionArea.setEditable(false);
        bookedDescriptionArea.setWrapStyleWord(true);
        bookedDescriptionArea.setLineWrap(true);
        JScrollPane descScrollPane = new JScrollPane(bookedDescriptionArea);

        JButton cancelButton = new JButton("Cancel Booking");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cancelButton);

        rightPanel.add(new JLabel("Booking Details", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(descScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(300);

        // Add listeners
        addBookedPackagesListeners(bookedPackageList, bookedDescriptionArea, cancelButton);

        return splitPane;
    }

    private void addAvailablePackagesListeners(JList<Package> packageList, JTextArea descriptionArea,
            JButton bookButton, JButton deleteAccountButton, JButton logoutButton) {
        packageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Package selectedPackage = packageList.getSelectedValue();
                if (selectedPackage != null) {
                    descriptionArea.setText(
                            "Title: " + selectedPackage.getTitle() + "\n\n" +
                                    "Description: " + selectedPackage.getDescription() + "\n\n" +
                                    "Price: ₹" + selectedPackage.getPrice());
                }
            }
        });

        bookButton.addActionListener(e -> {
            Package selectedPackage = packageList.getSelectedValue();
            if (selectedPackage == null) {
                JOptionPane.showMessageDialog(this, "Please select a package first");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to book " + selectedPackage.getTitle() + " for ₹" + selectedPackage.getPrice() + "?",
                    "Confirm Booking",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (Package.bookPackage(userId, selectedPackage.getId(), selectedPackage.getPrice())) {
                    JOptionPane.showMessageDialog(this, "Booking successful!");
                    refreshBookedPackages();
                } else {
                    JOptionPane.showMessageDialog(this, "Booking failed. Please try again.");
                }
            }
        });

        deleteAccountButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete your account? This action cannot be undone.",
                    "Confirm Account Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String result = User.deleteAccount(userId);
                if (result.equals("success")) {
                    JOptionPane.showMessageDialog(this, "Account deleted successfully!");
                    new LoginFrame().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, result);
                }
            }
        });

        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void addBookedPackagesListeners(JList<Package> bookedPackageList, JTextArea bookedDescriptionArea,
            JButton cancelButton) {
        bookedPackageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Package selectedPackage = bookedPackageList.getSelectedValue();
                if (selectedPackage != null) {
                    bookedDescriptionArea.setText(
                            "Title: " + selectedPackage.getTitle() + "\n\n" +
                                    "Description: " + selectedPackage.getDescription() + "\n\n" +
                                    "Price: ₹" + selectedPackage.getPrice());
                }
            }
        });

        cancelButton.addActionListener(e -> {
            Package selectedPackage = bookedPackageList.getSelectedValue();
            if (selectedPackage == null) {
                JOptionPane.showMessageDialog(this, "Please select a booking to cancel");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to cancel your booking for " + selectedPackage.getTitle() + "?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (Package.cancelBooking(userId, selectedPackage.getId())) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");
                    refreshBookedPackages();
                } else {
                    JOptionPane.showMessageDialog(this, "Cancellation failed. Please try again.");
                }
            }
        });
    }

    private DefaultListModel<Package> getPackagesArray() {
        DefaultListModel<Package> model = new DefaultListModel<>();
        List<Package> packages = Package.getAllPackages();
        for (Package pkg : packages) {
            model.addElement(pkg);
        }
        return model;
    }

    private DefaultListModel<Package> getBookedPackagesArray() {
        DefaultListModel<Package> model = new DefaultListModel<>();
        List<Package> packages = Package.getBookedPackages(userId);
        for (Package pkg : packages) {
            model.addElement(pkg);
        }
        return model;
    }

    private void refreshBookedPackages() {
        bookedPackageList.setModel(getBookedPackagesArray());
        bookedDescriptionArea.setText("");
    }
}