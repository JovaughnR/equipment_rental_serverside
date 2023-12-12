package server;

/**
   * @author Jovaughn Rose
   * @see https://github.com/jovaughnR
   */

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import common.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import common.Equipment;
import common.Message;
import common.Rental;
import common.Transactions;

public class Database {
    private static final SessionFactory sessionFactory;
    static {
        try {
            // Create a SessionFactory when the application starts
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    Connection connection;

    public Database(String user, String pass, String db, int port) {
        createConnection(user, pass, db, port);
    }

    private void createConnection(String user, String pass, String db, int port) {
        try {
            String url = "jdbc:mysql://localhost:" + port + "/" + db;
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.out.println("Could not make connection!");
            e.printStackTrace();
        }
    }

    public void add(Person p, String table) {

        String sql = "insert into " + table + " values (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, p.getId());
            statement.setString(2, p.getFirstName());
            statement.setString(3, p.getLastName());
            statement.setString(4, p.getEmail());
            statement.setString(5, p.getPhone());
            statement.executeUpdate();
            System.out.println("Person added successfully!");
            String id_type = "";
            if (table.equals("customer")) {
                id_type = "customer_id";
                table = "customer_authentication";
            } else {
                id_type = "employee_id";
                table = "employee_authentication";
            }
            addAddress(p, id_type);
            addPassword(p, table);
        } catch (SQLException e) {
            System.out.println("Error while trying to add person!");
            e.printStackTrace();
        }
    }

    private void addAddress(Person p, String id_type) {
        String sql = "INSERT INTO address (" +
                id_type + ", street_number, street_name, city, parish_state, zip_code,country) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, p.getId());
            statement.setInt(2, p.address.getStreetNumber());
            statement.setString(3, p.address.getStreetName());
            statement.setString(4, p.address.getCity());
            statement.setString(5, p.address.get_parish_state());
            statement.setString(6, p.address.getZipCode());
            statement.setString(7, p.address.getCountry());
            statement.executeUpdate();
            System.out.println("Address added successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private void addPassword(Person p, String table) {
        String sql = "INSERT INTO " + table + " VALUES (?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, p.getId());
            statement.setString(2, p.getPassword());
            statement.executeUpdate();
            System.out.println("Password added successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Person findCustomer(int id) {
        String sql = "select * from customer where customer_id = ?";
        Person p = new Person();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                p.setId(result.getInt(1));
                p.setFirstName(result.getString(2));
                p.setLastName(result.getString(3));
                p.setEmail(result.getString(4));
                p.setPhone(result.getString(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public ArrayList<Integer> getEntityIds(String idType, String table) {
        ArrayList<Integer> ids = new ArrayList<>();
        String sql = "select " + idType + " from " + table;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(idType);
                ids.add(id);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return ids;
    }

    public int getGeneratedId(String type) {
        int customer_constant = 0, employee_constant = 0;
        String sql = "select * from generator";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                customer_constant = result.getInt(2);
                employee_constant = result.getInt(3);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if (type.equalsIgnoreCase("customer"))
            return customer_constant;

        return employee_constant;
    }

    public void updateGeneratedId(String type, int value) {
        String column = "";
        if (type.equalsIgnoreCase("customer")) {
            column = "customer_constant";
        } else {
            column = "employee_constant";
        }
        String sql = "update generator set " + column +
                " = ?  where generator_id = 1";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, value + 1);
            statement.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public String lookUpPassword(int id) {
        String table = "", id_type = "", password = "";
        if (id >= 111111) {
            id_type = "employee_id";
            table = "employee_authentication";
        } else {
            table = "customer_authentication";
            id_type = "customer_id";
        }

        String sql = "select * from " + table +
                " where " + id_type + " =" + " ?";
        System.out.println(sql);
        try {
            PreparedStatement stat = connection.prepareStatement(sql);
            stat.setInt(1, id);
            ResultSet result = stat.executeQuery();

            while (result.next())
                password = result.getString(2);

        } catch (SQLException e) {
        }
        return password;
    }

    public void saveEquipment(Equipment equipment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(equipment); // Save the equipment entity
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public Equipment findEquipmentById(int equipmentId) {
        Session session = sessionFactory.openSession();
        Equipment equipment = null;

        try {
            equipment = session.get(Equipment.class, equipmentId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return equipment;
    }

    public List<Equipment> getAllEquipment() {
        Session session = sessionFactory.openSession();
        List<Equipment> equipmentList = null;

        try {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Equipment> criteriaQuery = criteriaBuilder.createQuery(Equipment.class);
            Root<Equipment> root = criteriaQuery.from(Equipment.class);
            criteriaQuery.select(root);

            Query<Equipment> query = session.createQuery(criteriaQuery);
            equipmentList = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return equipmentList;
    }

    public void decrementEquipmentQuantity(int equipment_id) {
        Equipment equipment = this.findEquipmentById(equipment_id);
        int quantity = equipment.getQuantity();
        String sql = "update equipment set quantity = ? where equipment_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            if (quantity > 0) {
                System.out.println("Decrementing the quantity of the equipment");
                statement.setInt(1, quantity - 1);
                statement.setInt(2, equipment_id);
            } else {
                System.out.println("Equipment marked as booked");
                sql = "update equipment set availability_status = ? where equipment_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, "Booked");
                statement.setInt(2, equipment_id);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());

        }
    }

    public void saveRentalRequest(Rental rental) {
        String sql = "insert into rental_request (customer_id, equipment_id, date_requested) " +
                "VALUES (?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, rental.getCustomerId());
            statement.setInt(2, rental.getEquipmentId());
            statement.setString(3, rental.getDate());
            statement.executeUpdate();
            this.decrementEquipmentQuantity(rental.getEquipmentId());
            System.out.println("Rental request added to database successfully");
        } catch (SQLException e) {
            System.out.println("Error Saving rental request: " + e.getMessage());
        }
    }

    public void singleTransaction() {
        // String sql = "select * from rental_request"
    }

    public ArrayList<Rental> getRentalRequests() {
        ArrayList<Rental> rentals = new ArrayList<>();
        String sql = "select * from rental_request";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int customerId = result.getInt("customer_id");
                int equipmentId = result.getInt("equipment_id");
                String date = result.getString("date_requested");
                Rental rental = new Rental(date, customerId, equipmentId);
                rentals.add(rental);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return rentals;
    }

    public ArrayList<Rental> getRentalsById(int equipmentId) {
        ArrayList<Rental> rentals = new ArrayList<>();
        String sql = "select * from rental_request where equipment_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, equipmentId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String date = result.getString("customer_id");
                int cusId = result.getInt("customer_id");
                int equipId = result.getInt("equipment_id");
                rentals.add(new Rental(date, cusId, equipId));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return rentals;
    }

    public void removeRentalRequest(int equipmentId, int customerId) {
        String sql = "delete from rental_request where equipment_id = ? and customer_id = ? limit 1";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, equipmentId);
            statement.setInt(2, customerId);
            statement.executeUpdate();
            System.out.println("Request deleted successfully");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void saveTransaction(Transactions transaction) {
        String sql = "insert into transactions (customer_id, equipment_id, dateTransacted, cost) + VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, transaction.getCustomerId());
            statement.setInt(2, transaction.getEquipmentId());
            statement.setString(3, transaction.getDateOfTransaction());
            statement.setBigDecimal(0, transaction.getMoneyAmount());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public LinkedList<Transactions> findTransactionBy(int customerId) {
        LinkedList<Transactions> transactions = new LinkedList<>();
        String sql = "select * from transactions where customer_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, customerId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Transactions trans = new Transactions();
                trans.setCustomerId(result.getInt("customer_id"));
                trans.setEquipmentId(result.getInt("equipment_id"));
                trans.setDateOfTransaction(result.getString("dateTransacted"));
                trans.setMoneyAmount(result.getBigDecimal("cost"));
                transactions.add(trans);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return transactions;
    }

    public void saveMessages(Message message) {
        String sql = "insert into messages (message_type, customer_id, employee_id, message)" +
                "VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, message.getMessageType()); // to be continued
            statement.setInt(2, message.getCustomerId());
            statement.setInt(3, message.getEmployeeId());
            statement.setString(4, message.getMessage());
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public LinkedList<Message> getMessages() {
        LinkedList<Message> messages = new LinkedList<>();

        String sql = "select * from messages";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int msgType = result.getInt("message_type");
                int cusId = result.getInt("customer_id");
                int empId = result.getInt("employee_id");
                String msg = result.getString("message");
                messages.add(new Message(msgType, cusId, empId, msg));
            }
            return messages;

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

}
