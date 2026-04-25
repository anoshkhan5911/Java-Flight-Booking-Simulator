import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FlightBookingEnterprise {

    
    static final String ADMIN_PASSWORD = "admin"; 
    static final DecimalFormat MONEY = new DecimalFormat("#,###.00");
    
     
    static final Color CLR_SIDEBAR = new Color(34, 47, 62);    
    static final Color CLR_BG      = new Color(245, 246, 250); 
    static final Color CLR_ACCENT  = new Color(16, 172, 132);  
    static final Color CLR_PRIMARY = new Color(84, 160, 255);  
    static final Color CLR_DANGER  = new Color(238, 82, 83);   
    static final Color CLR_TEXT    = new Color(47, 53, 66);

    static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD, 13);

    
    static class Flight implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String route, time;
        int capacity;
        boolean[] seats;
        double fare;

        Flight(int id, String src, String dest, String time, int cap, double fare) {
            this.id = id;
            this.route = src + " ➔ " + dest;
            this.time = time;
            this.capacity = cap;
            this.fare = fare;
            this.seats = new boolean[cap];
        }
    }

    static class Booking implements Serializable {
        private static final long serialVersionUID = 1L;
        int bookingId;
        Flight flight;
        String passengerName, cnic;
        int seatNumber;
        double amountPaid;

        Booking(int id, Flight f, String name, String cnic, int seat, double amt) {
            this.bookingId = id; this.flight = f; this.passengerName = name;
            this.cnic = cnic; this.seatNumber = seat; this.amountPaid = amt;
        }
    }

    
    static ArrayList<Flight> flights = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();
    static int bookingCounter = 5000;
    static JTextArea logArea;

    

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        SwingUtilities.invokeLater(() -> {
            loadDatabase();
            new DashboardFrame();
        });
    }

    

    static class DashboardFrame extends JFrame {
        public DashboardFrame() {
            setTitle("SkyLine Enterprise - Flight Reservation System");
            setSize(1100, 700);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            
            JPanel sidebar = new JPanel();
            sidebar.setPreferredSize(new Dimension(260, 700));
            sidebar.setBackground(CLR_SIDEBAR);
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setBorder(new EmptyBorder(30, 0, 0, 0));

            JLabel logo = new JLabel("SKYLINE AIR");
            logo.setForeground(Color.WHITE);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebar.add(logo);
            sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

            
            sidebar.add(new SmartButton("Search Flights", IconType.SEARCH, true, e -> actionSearch()));
            sidebar.add(new SmartButton("Book Ticket", IconType.TICKET, true, e -> actionBook()));
            sidebar.add(new SmartButton("Cancel Booking", IconType.CANCEL, true, e -> actionCancel()));
            sidebar.add(Box.createVerticalGlue());
            sidebar.add(new SmartButton("Admin Panel", IconType.LOCK, true, e -> actionAdmin()));
            sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

            
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(CLR_BG);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            header.setPreferredSize(new Dimension(100, 70));
            header.setBorder(new EmptyBorder(0, 30, 0, 30));
            
            JLabel title = new JLabel("System Status: Online");
            title.setFont(F_HEADER);
            title.setForeground(CLR_TEXT);
            header.add(title, BorderLayout.WEST);

            logArea = new JTextArea();
            logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            logArea.setEditable(false);
            logArea.setText("Welcome to SkyLine Enterprise.\nDatabase Connection: Stable.\nSelect an option from the sidebar to begin.");
            logArea.setMargin(new Insets(20, 20, 20, 20));

            content.add(header, BorderLayout.NORTH);
            content.add(new JScrollPane(logArea), BorderLayout.CENTER);

            add(sidebar, BorderLayout.WEST);
            add(content, BorderLayout.CENTER);
            setVisible(true);
        }
    }

    

    enum IconType { SEARCH, TICKET, CANCEL, LOCK, ADD, DELETE, USERS, CHART }

    static class SmartButton extends JButton {
        private IconType type;
        private boolean isSidebar; 
        private boolean hover = false;

        public SmartButton(String text, IconType type, boolean isSidebar, ActionListener action) {
            super(text);
            this.type = type;
            this.isSidebar = isSidebar;
            addActionListener(action);
            setFont(F_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if(isSidebar) {
                setForeground(new Color(189, 195, 199));
                setBackground(CLR_SIDEBAR);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(new Dimension(260, 55));
            } else {
                setForeground(Color.WHITE);
                setBackground(CLR_PRIMARY);
            }
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            
            if (isSidebar) {
                if (hover) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(10, 5, getWidth()-20, getHeight()-10, 10, 10);
                    g2.setColor(CLR_ACCENT);
                    g2.fillRect(0, 10, 4, getHeight()-20);
                    setForeground(Color.WHITE);
                } else {
                    setForeground(new Color(189, 195, 199));
                }
            } else {
                
                g2.setColor(hover ? getBackground().darker() : getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                setForeground(Color.WHITE);
            }

            
            int x = isSidebar ? 40 : 15; 
            int y = getHeight() / 2;
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(2f));

            switch (type) {
                case SEARCH:
                    g2.drawOval(x, y-6, 8, 8);
                    g2.drawLine(x+6, y+3, x+10, y+7);
                    break;
                case TICKET:
                    AffineTransform old = g2.getTransform();
                    g2.rotate(Math.toRadians(-45), x+6, y);
                    g2.fillRect(x, y-4, 14, 8);
                    g2.setTransform(old);
                    break;
                case CANCEL:
                    g2.drawLine(x, y-4, x+8, y+4);
                    g2.drawLine(x+8, y-4, x, y+4);
                    break;
                case LOCK:
                    g2.drawRect(x, y-2, 8, 6);
                    g2.drawArc(x+1, y-6, 6, 6, 0, 180);
                    break;
                case ADD: 
                    g2.drawLine(x, y-5, x, y+5);
                    g2.drawLine(x-5, y, x+5, y);
                    break;
                case DELETE: 
                    g2.drawRect(x-4, y-4, 8, 8);
                    g2.drawLine(x-5, y-4, x+5, y-4);
                    break;
                case USERS: 
                    g2.drawOval(x-2, y-6, 4, 4);
                    g2.drawArc(x-5, y, 10, 8, 0, 180);
                    break;
                case CHART: 
                    g2.fillRect(x-5, y, 2, 6);
                    g2.fillRect(x-1, y-4, 2, 10);
                    g2.fillRect(x+3, y-2, 2, 8);
                    break;
            }
            
            
            super.paintComponent(g);
        }
        
        @Override 
        public Insets getInsets() { 
            return isSidebar ? new Insets(0, 80, 0, 0) : new Insets(0, 40, 0, 15); 
        }
    }

    

    static void actionSearch() {
        logArea.setText("");
        logArea.append(" === FLIGHT SCHEDULE ===\n\n");
        logArea.append(String.format(" %-6s | %-25s | %-15s | %-10s | %-12s\n", "ID", "ROUTE", "TIME", "SEATS", "FARE"));
        logArea.append(" --------------------------------------------------------------------------------------\n");
        for (Flight f : flights) {
            int available = 0;
            for(boolean b : f.seats) if(!b) available++;
            logArea.append(String.format(" #%-5d | %-25s | %-15s | %-10d | Rs. %s\n", 
                f.id, f.route, f.time, available, MONEY.format(f.fare)));
        }
    }

    static void actionBook() {
        try {
            String idStr = JOptionPane.showInputDialog("Enter Flight ID:");
            if(idStr == null) return;
            int fid = Integer.parseInt(idStr);
            Flight fl = flights.stream().filter(f -> f.id == fid).findFirst().orElse(null);
            
            if(fl == null) throw new Exception("Flight ID not found.");

            JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
            JTextField tName = new JTextField();
            JTextField tCnic = new JTextField();
            JTextField tSeat = new JTextField();
            JTextField tPay  = new JTextField();
            
            p.add(new JLabel("Flight Route:")); p.add(new JLabel(fl.route));
            p.add(new JLabel("Fare Required:")); p.add(new JLabel("Rs. " + MONEY.format(fl.fare)));
            p.add(new JLabel("Passenger Name:")); p.add(tName);
            p.add(new JLabel("CNIC Number:")); p.add(tCnic);
            p.add(new JLabel("Seat Number (0-" + (fl.capacity-1) + "):")); p.add(tSeat);
            p.add(new JLabel("Payment Amount:")); p.add(tPay);

            if(JOptionPane.showConfirmDialog(null, p, "Booking Counter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if(tName.getText().isEmpty()) throw new Exception("Name required.");
                int seat = Integer.parseInt(tSeat.getText());
                double pay = Double.parseDouble(tPay.getText());

                if(seat < 0 || seat >= fl.capacity || fl.seats[seat]) throw new Exception("Seat unavailable.");
                if(pay < fl.fare) throw new Exception("Insufficient Payment.");

                simulateBankProcessing();

                fl.seats[seat] = true;
                Booking b = new Booking(bookingCounter++, fl, tName.getText(), tCnic.getText(), seat, pay);
                bookings.add(b);
                saveDatabase();

                logArea.setText(" ✅ TICKET CONFIRMED\n ------------------------\n");
                logArea.append(" ID: " + b.bookingId + " | Passenger: " + b.passengerName + "\n");
                JOptionPane.showMessageDialog(null, "Booking Successful!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    static void simulateBankProcessing() {
        JDialog d = new JDialog((Frame)null, "Bank Gateway", true);
        JPanel p = new JPanel(new FlowLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT, 2));
        JLabel l = new JLabel("Contacting Bank... Verifying Funds...");
        p.add(l);
        d.add(p);
        d.setSize(300, 80);
        d.setLocationRelativeTo(null);
        d.setUndecorated(true);
        new javax.swing.Timer(1500, e -> d.dispose()).start();
        d.setVisible(true);
    }

    static void actionCancel() {
        String input = JOptionPane.showInputDialog("Enter Booking ID to Cancel:");
        if(input == null) return;
        boolean found = false;
        Iterator<Booking> it = bookings.iterator();
        while(it.hasNext()){
            Booking b = it.next();
            if(b.bookingId == Integer.parseInt(input)){
                b.flight.seats[b.seatNumber] = false;
                it.remove();
                found = true;
                saveDatabase();
                logArea.setText(" ❌ CANCELLED: Booking " + input + " removed.");
                break;
            }
        }
        if(!found) JOptionPane.showMessageDialog(null, "ID not found.");
    }

    static void actionAdmin() {
        JPasswordField pf = new JPasswordField();
        if(JOptionPane.showConfirmDialog(null, pf, "Enter Admin Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if(new String(pf.getPassword()).equals(ADMIN_PASSWORD)) openAdminDashboard();
            else JOptionPane.showMessageDialog(null, "Access Denied!");
        }
    }

    

    static void openAdminDashboard() {
        JFrame f = new JFrame("Administrator Panel");
        f.setSize(700, 500);
        f.setLocationRelativeTo(null);
        f.setLayout(new BorderLayout());

        JTextArea log = new JTextArea("Admin Mode Active.\n");
        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        
        JPanel toolbar = new JPanel(new GridLayout(1, 4, 10, 10));
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        
        SmartButton b1 = new SmartButton("Add Flight", IconType.ADD, false, e -> {
            try {
                int id = flights.isEmpty() ? 101 : flights.get(flights.size()-1).id + 1;
                String src = JOptionPane.showInputDialog("Source:");
                String dst = JOptionPane.showInputDialog("Destination:");
                String time = JOptionPane.showInputDialog("Time:");
                double fare = Double.parseDouble(JOptionPane.showInputDialog("Fare:"));
                flights.add(new Flight(id, src, dst, time, 60, fare));
                saveDatabase();
                log.append("Added Flight " + id + "\n");
            } catch(Exception ex) {}
        });

        SmartButton b2 = new SmartButton("Delete", IconType.DELETE, false, e -> {
            String id = JOptionPane.showInputDialog("Flight ID:");
            if(id != null) {
                flights.removeIf(flight -> flight.id == Integer.parseInt(id));
                saveDatabase();
                log.append("Deleted Flight " + id + "\n");
            }
        });
        b2.setBackground(CLR_DANGER); 

        SmartButton b3 = new SmartButton("Passengers", IconType.USERS, false, e -> {
            log.setText(" --- PASSENGER MANIFEST ---\n");
            for(Booking b : bookings) 
                log.append("Flight " + b.flight.id + " | " + b.passengerName + " | Seat " + b.seatNumber + "\n");
        });
        b3.setBackground(new Color(155, 89, 182)); 

        SmartButton b4 = new SmartButton("Revenue", IconType.CHART, false, e -> {
            double total = 0;
            for(Booking b : bookings) total += b.amountPaid;
            log.setText(" --- FINANCIAL REPORT ---\nTotal Revenue: Rs. " + MONEY.format(total));
        });
        b4.setBackground(CLR_ACCENT); 

        toolbar.add(b1); toolbar.add(b2); toolbar.add(b3); toolbar.add(b4);
        f.add(new JScrollPane(log), BorderLayout.CENTER);
        f.add(toolbar, BorderLayout.SOUTH);
        f.setVisible(true);
    }

    
    static void saveDatabase() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("database.dat"))) {
            out.writeObject(flights); out.writeObject(bookings); out.writeInt(bookingCounter);
        } catch (Exception e) {}
    }

    @SuppressWarnings("unchecked")
    static void loadDatabase() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("database.dat"))) {
            flights = (ArrayList<Flight>) in.readObject();
            bookings = (ArrayList<Booking>) in.readObject();
            bookingCounter = in.readInt();
        } catch (Exception e) {
            flights.add(new Flight(101, "New York", "London", "10:00 AM", 50, 85000));
            flights.add(new Flight(102, "Dubai", "Paris", "02:30 PM", 40, 65000));
        }
    }
}