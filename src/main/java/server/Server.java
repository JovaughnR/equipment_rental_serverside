package server;

/**
   * @author Jovaughn Rose
   * @see https://github.com/jovaughnR
   */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import common.*;

public class Server {
    private ServerSocket serverSocket;
    private Database database;
    private int clientCount = 0;

    HashMap<Integer, LinkedList<Message>> messageReciever = new HashMap<>();

    public static void main(String[] args) {
        String user = "root";
        String pass = "";
        String db = "grizzly_entertainment_equipment_rental";
        int database_port = 3306;
        int server_port = 8000;
        Database database = new Database(user, pass, db, database_port);
        new Server(server_port, database);
    }

    public Server(int serverPort, Database database) {
        this.database = database;
        this.readInMessages();
        this.configureServer(serverPort);
    }

    private void configureServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server running on port " + port);
            while (true) {
                new Thread(new ClientHandler(serverSocket.accept())).start();
                System.out.println("New Client Connected");
                clientCount++;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void readInMessages() {
        LinkedList<Message> messages = database.getMessages();
        while (messages != null && !messages.isEmpty()) {
            Message message = messages.removeFirst();
            int key = 0;

            if (message.getMessageType() == 1)
                key = message.getCustomerId();
            else
                key = message.getEmployeeId();

            if (!messageReciever.containsKey(key)) {
                messageReciever.put(key, new LinkedList<Message>());
                messageReciever.get(key).add(message);
            } else
                messageReciever.get(key).add(message);
        }
    }

    public class ClientHandler implements Runnable {
        private ObjectInputStream clientInput;
        private ObjectOutputStream output;
        private Socket socket;
        public int activeUserId, messageRecieverId;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public boolean equals(ClientHandler other) {
            return this.activeUserId == other.activeUserId;
        }

        private void signUp() throws IOException {
            try {
                String entity = (String) this.write('e', true);
                Person person = (Person) clientInput.readObject();
                int id = database.getGeneratedId(entity);
                database.updateGeneratedId(entity, id);
                person.setId(id);
                database.add(person, entity);
                this.write(id, false);
                this.activeUserId = id;

            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        private void signIn() throws IOException {
            final int id = (Integer) this.write("id", true);
            final String password = (String) this.write('p', true);

            if (Generator.hash(password).equals(database.lookUpPassword(id))) {
                this.activeUserId = id;
                this.write(true, false);
            } else
                this.write(false, false);
        }

        private Object write(Object obj, boolean auto) {
            try {
                output.writeObject(obj);
                output.flush();

                if (auto)
                    return clientInput.readObject();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }

            return null;
        }

        private void sendEquipment() throws IOException {
            final int id = (Integer) this.write('i', true);
            this.write(database.findEquipmentById(id), false);
        }

        private void approveRentalRequest() throws IOException {
            try {
                output.writeObject("id");
                this.output.flush();// clearOutputStream();
                final int id = (Integer) clientInput.readObject();
                Equipment equipment = database.findEquipmentById(id);

                if ("booked".equalsIgnoreCase(equipment.getAvailabilityStatus())) {
                    output.writeObject(false);
                    clientInput.readObject();
                } else {
                    output.writeObject(true);
                    // to be fixed expected a rental
                    Rental rental = (Rental) clientInput.readObject();
                    rental.setEquipmentId(id);
                    database.saveRentalRequest(rental);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        private void sendSingeEquipment() throws IOException {
            int id = (Integer) this.write('e', true);
            this.write(database.findEquipmentById(id), false);
        }

        private void removeRentalRequest() throws IOException {
            int equipmentId = (Integer) this.write('r', true);
            int customerId = (Integer) this.write('r', true);
            database.removeRentalRequest(equipmentId, customerId);
        }

        @Override
        public void run() {
            try {
                configureStreams();

                while (true)
                    doClientRequest((String) clientInput.readObject());

            } catch (ClassNotFoundException | IOException e) {
                clientCount = clientCount - 1;
                System.out.println("Client disconnected!");
            } finally {
                closeConnections();
            }
        }

        private void configureStreams() throws IOException {
            if (clientInput == null)
                clientInput = new ObjectInputStream(this.socket.getInputStream());
            if (output == null)
                output = new ObjectOutputStream(this.socket.getOutputStream());
        }

        private void closeConnections() {
            try {
                if (clientInput != null)
                    clientInput.close();
                if (output != null)
                    output.close();
                this.socket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        private void doClientRequest(String request) throws IOException, ClassNotFoundException {
            if ("signIn".equalsIgnoreCase(request))
                signIn();
            else if ("signUp".equalsIgnoreCase(request))
                signUp();
            else if ("entity".equalsIgnoreCase(request)) {
                output.writeObject(request);
                final int id = (Integer) clientInput.readObject();
                output.writeObject(database.findCustomer(id));
            } else if ("getCustomerIds".equalsIgnoreCase(request)) {
                String idType = "customer_id", table = "customer";
                this.output.writeObject(database.getEntityIds(idType, table));
            } else if ("singleEquipment".equalsIgnoreCase(request)) {
                sendSingeEquipment();
            } else if ("equipments".equalsIgnoreCase(request))
                output.writeObject(database.getAllEquipment());
            else if ("requestEquipment".equalsIgnoreCase(request))
                sendEquipment();

            if ("requestRental".equalsIgnoreCase(request))
                approveRentalRequest();
            else if ("getRentalRequest".equalsIgnoreCase(request))
                output.writeObject(database.getRentalRequests());
            else if ("removeRentalRequest".equalsIgnoreCase(request)) {
                removeRentalRequest();
            }
            // to be tested
            else if ("transaction".equalsIgnoreCase(request))
                database.saveTransaction((Transactions) clientInput.readObject());

            else if ("getTransactions".equals(request)) {
                int id = (Integer) this.write('i', true);
                output.writeObject(database.findTransactionBy(id));
            }
            // to be tested
            else if ("RentalsById".equalsIgnoreCase(request)) {
                int id = (Integer) this.write('i', true);
                this.write(database.getRentalsById(id), false);
            }

            else if ("messageObject".equalsIgnoreCase(request)) {

                Message message = (Message) this.write("sendObject", true);

                // int msgType = message.getMessageType();
                int cusId = message.getCustomerId();
                int empId = message.getEmployeeId();

                // check if the message object receive contains a customer but not a employee id
                // if so then the message is meant for the an employee

                if (empId == 0) {
                    // find all the employees in the database and distribute the message to them
                    String idType = "employee_id", table = "employee";
                    ArrayList<Integer> empIds = database.getEntityIds(idType, table);

                    for (int id : empIds) {
                        message.setEmployeeId(id);
                        if (!messageReciever.containsKey(id)) {
                            messageReciever.put(id, new LinkedList<Message>());
                            messageReciever.get(id).add(message);
                        } else
                            messageReciever.get(id).add(message);
                    }

                } else if (this.activeUserId == message.getCustomerId()) {
                    // this means that the employee id is present in the message
                    // and the message is meant for an employee
                    if (messageReciever.containsKey(empId))
                        messageReciever.get(empId).add(message);
                    else
                        messageReciever.put(empId, new LinkedList<Message>());
                }

                else {
                    // add the recieving customer id with the message to the hashmap
                    if (!messageReciever.containsKey(cusId)) {
                        messageReciever.put(cusId, new LinkedList<Message>());
                        messageReciever.get(cusId).add(message);
                    } else
                        messageReciever.get(cusId).add(message);
                }
            }

            if ("isThereAmessageForMe".equalsIgnoreCase(request)) {
                LinkedList<Message> messages = new LinkedList<>();

                if (messageReciever.containsKey(this.activeUserId) &&
                        !messageReciever.get(this.activeUserId).isEmpty())
                    messages.add(messageReciever.get(this.activeUserId).removeFirst());

                if (messages.size() != 0) {
                    this.write(true, true);
                    this.output.writeObject(messages);

                } else
                    this.write(false, false);
            }
        }
    }

}
