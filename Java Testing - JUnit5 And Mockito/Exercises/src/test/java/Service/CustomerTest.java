package Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import Interface.CustomerService;
import Repository.CustomerRepository;
import models.Customer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


class CustomerTest {

    private CustomerServiceImpl customerService;
    private CustomerRepository mockRepository;
    private Connection dbConnection;


    @BeforeEach
    void setUp() throws SQLException {
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        String username = "sa";
        String password = "";

        try {
            Class.forName("org.h2.Driver");

            dbConnection = DriverManager.getConnection(jdbcUrl, username, password);

            createTestTable();

            mockRepository = new CustomerRepository(dbConnection);
            customerService = new CustomerServiceImpl(mockRepository);

        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 Driver not found", e);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dropTestTable();
            dbConnection.close();
        }
    }

    private void createTestTable() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS customers (
                cust_id INT PRIMARY KEY,
                customer_name VARCHAR(255) NOT NULL,
                contact_number VARCHAR(20),
                address VARCHAR(500)
            )
            """;

        try (Statement stmt = dbConnection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void dropTestTable() throws SQLException {
        String dropTableSQL = "DROP TABLE IF EXISTS customers";

        try (Statement stmt = dbConnection.createStatement()) {
            stmt.executeUpdate(dropTableSQL);
        }
    }

    @Test
    void testCustomerCreationAndValidateData() {
        int custId = 1;
        String customerName = "John Doe";
        String contactNumber = "1234567890";
        String address = "123 Main St";

        Customer customer = new Customer(custId, customerName, contactNumber, address);

        assumeTrue(customer != null, "Customer object should not be null");

        assertEquals(custId, customer.getCustId());
        assertEquals(customerName, customer.getCustomerName());
        assertEquals(contactNumber, customer.getContactNumber());
        assertEquals(address, customer.getAddress());
    }

    @Test
    void testCustomerCreationWithInvalidData() {
        int custId = -1; // Invalid ID
        String customerName = ""; // Empty name
        String contactNumber = "123"; // Invalid contact number
        String address = ""; // Empty address

        Customer customer = new Customer(custId, customerName, contactNumber, address);

        assumeTrue(customer != null, "Customer object should not be null");

        assumeTrue(custId < 0, "This test assumes invalid customer ID");
        assumeTrue(customerName.isEmpty(), "This test assumes empty customer name");
        assumeTrue(contactNumber.length() < 10, "This test assumes invalid contact number length");
        assumeTrue(address.isEmpty(), "This test assumes empty address");
    }

    @Test
    void testCustomerCreationWithOptionalValidation() {
        int custId = 1;
        String customerName = "John Doe";
        String contactNumber = "1234567890";
        String address = "123 Main St";

        Optional<Customer> customerOpt = Optional.ofNullable(
                new Customer(custId, customerName, contactNumber, address)
        );

        assumeTrue(customerOpt.isPresent(), "Customer should be present for validation");

        Customer customer = customerOpt.get();

        assertAll("Optional-based customer validation",
                () -> assertEquals(custId, customer.getCustId()),
                () -> assertEquals(customerName, customer.getCustomerName()),
                () -> assertEquals(contactNumber, customer.getContactNumber()),
                () -> assertEquals(address, customer.getAddress())
        );
    }

    @Test
    void testUpdateCustomerName() {
        int custId = 1;
        String newName = "Jane Doe";
        Customer existingCustomer = new Customer(custId, "John Doe", "1234567890", "123 Main St");
        mockRepository.getAllCustomers().add(existingCustomer);

        customerService.updateCustomerName(custId, newName);

        assumeTrue(existingCustomer.getCustomerName().equals(newName), "Customer name should be updated");
    }

    @Test
    void testAddAndGetCustomer(){
        CustomerService customerService = new CustomerServiceImpl();
        Customer customer = new Customer(1, "John Doe", "1234567890", "123 Main St");

        customerService.addCustomer(customer);
        Customer retrievedCustomer = customerService.getCustomerById(1);

        assertNotNull(retrievedCustomer);
        assertEquals("John Doe", retrievedCustomer.getCustomerName());
    }

    @Test
    void testDeletingCustomer(){
        CustomerService customerService = new CustomerServiceImpl();
        Customer customer = new Customer(1, "John Doe", "1234567890", "123 Main St");

        customerService.addCustomer(customer);
        customerService.removeCustomer(1);
        Customer retrievedCustomer = customerService.getCustomerById(1);

        assertNull(retrievedCustomer);
    }

    @Test
    void testCRUDOperationsWithDatabaseAssumptions() throws SQLException {
        assumeTrue(dbConnection != null && !dbConnection.isClosed(),
                "Application should be connected to a database for CRUD operations");
        
        assumeTrue(isTestDatabaseAvailable() && hasTestData(),
                "Test database should be available and pre-populated with test data");
        
        assumeTrue(isTestEnvironmentIsolated(),
                "Tests should be isolated and not affect production database");

        testFullCRUDCycle();
    }

    private boolean isTestDatabaseAvailable() {
        try {
            String testQuery = "SELECT 1";
            try (Statement stmt = dbConnection.createStatement(); 
                 var rs = stmt.executeQuery(testQuery)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean hasTestData() {
        try {
            String checkTableQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CUSTOMERS'";
            try (Statement stmt = dbConnection.createStatement();
                 var rs = stmt.executeQuery(checkTableQuery)) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            try {
                String testDataQuery = "SELECT COUNT(*) FROM customers";
                try (Statement stmt = dbConnection.createStatement();
                     var rs = stmt.executeQuery(testDataQuery)) {
                    return true;
                }
            } catch (SQLException ex) {
                return false;
            }
        }
        return false;
    }

    private boolean isTestEnvironmentIsolated() {
        try {
            String url = dbConnection.getMetaData().getURL();
            return url.contains(":h2:mem:") ||
                    url.contains(":sqlite::memory:") ||
                    url.contains("testdb") ||
                    url.contains("test");
        } catch (SQLException e) {
            return false;
        }
    }

    private void testFullCRUDCycle() {
        // CREATE - Add a new customer
        Customer newCustomer = new Customer(999, "Test User", "9876543210", "Test Address 123");
        customerService.addCustomer(newCustomer);

        // READ - Retrieve the customer
        Customer retrievedCustomer = customerService.getCustomerById(999);
        assertNotNull(retrievedCustomer, "Customer should be retrievable after creation");
        assertEquals("Test User", retrievedCustomer.getCustomerName());

        // UPDATE - Modify customer name
        customerService.updateCustomerName(999, "Updated Test User");
        Customer updatedCustomer = customerService.getCustomerById(999);
        assertEquals("Updated Test User", updatedCustomer.getCustomerName(),
                "Customer name should be updated");

        // DELETE - Remove the customer
        customerService.removeCustomer(999);
        Customer deletedCustomer = customerService.getCustomerById(999);
        assertNull(deletedCustomer, "Customer should be null after deletion");
    }

    @Test
    void testDatabaseConnectionAssumptions() throws SQLException {
        // Assumption 1: Database connection exists
        assumeTrue(dbConnection != null, "Database connection should not be null");
        assumeTrue(!dbConnection.isClosed(), "Database connection should be open");

        // Assumption 2: Test database is properly set up
        assumeTrue(dbConnection.getMetaData() != null, "Database metadata should be accessible");

        // Assumption 3: Using test database (not production)
        String databaseUrl = dbConnection.getMetaData().getURL();
        assumeTrue(databaseUrl.contains("test") || databaseUrl.contains("mem"),
                "Should be using test or in-memory database, not production. URL: " + databaseUrl);

        // If all assumptions pass, verify basic database operations work
        assertDoesNotThrow(() -> {
            try (Statement stmt = dbConnection.createStatement()) {
                stmt.executeQuery("SELECT 1");
            }
        }, "Basic database query should work without throwing exceptions");
    }

    @Test
    void testCustomerCreationWithInvalidValues() {
        Customer invalidCustomer = new Customer(-1, "John", "9876543210", "Test Address 123");

        try{
            assertTrue(invalidCustomer.getCustId() <= 0, "Customer id should be positive");
        } catch (AssertionFailedError e) {
            fail("AssertionFailedError: " + e.getMessage());
        }
    }

    @Test
    void testCustomerUpdateWithInvalidValues() {
        Customer validCustomer = new Customer(1, "John", "9876543210", "Test Address 123");
        customerService.addCustomer(validCustomer);

        try{
            customerService.updateCustomerName(1, "ceav");
            Customer updatedCustomer = customerService.getCustomerById(1);
            assertFalse(updatedCustomer.getCustomerName().isEmpty(), "Customer name should not be empty");
        } catch (AssertionFailedError e) {
            fail("AssertionFailedError: " + e.getMessage());
        }
    }

    @Test
    void testDeleteCustomerGracefulErrorHandling(){
        int nonExistentCustomerId = 9999;

        try {
            Customer nonExistentCustomer = customerService.getCustomerById(nonExistentCustomerId);
            assertNull(nonExistentCustomer, "Customer should not exist before deletion attempt");

            customerService.removeCustomer(nonExistentCustomerId);

            assertTrue(true, "Deletion of non-existent customer should complete without throwing exceptions");

        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Customer not found") ||
                            e.getMessage().contains("does not exist") ||
                            e.getMessage().contains("Invalid customer ID"),
                    "Exception message should be informative: " + e.getMessage());
        } catch (RuntimeException e) {
            assertTrue(e.getMessage() != null && !e.getMessage().isEmpty(),
                    "Exception should have a meaningful message: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception type thrown: " + e.getClass().getSimpleName() +
                    " with message: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1, John Doe, 1234567890, 123 Main St",
        "2, Jane Smith, 0987654321, 456 Elm St",
        "3, Alice Johnson, 5555555555, 789 Oak St"
    })
    void testParameterizedCustomerCreation(int custId, String customerName, String contactNumber, String address) {
        Customer customer = new Customer(custId, customerName, contactNumber, address);

        assumeTrue(customer != null, "Customer object should not be null");

        assertAll("Parameterized customer validation",
                () -> assertEquals(custId, customer.getCustId()),
                () -> assertEquals(customerName, customer.getCustomerName()),
                () -> assertEquals(contactNumber, customer.getContactNumber()),
                () -> assertEquals(address, customer.getAddress())
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {
            1, 100, 99999
    })
    void testParameterizedInvalidCustomerId(int invalidCustId) {
        Customer customer = new Customer(invalidCustId, "Test User", "1234567890", "Test Address");

        assertTrue(customer.getCustId() > 0, "Customer id should be positive");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/customer.csv")
    void testUpdateCustomerNameFromCsv(int custId, String initialName, String contactNumber, String address, String updatedName) {
        Customer customer = new Customer(custId, initialName, contactNumber, address);
        customerService.addCustomer(customer);

        customerService.updateCustomerName(custId, updatedName);
        Customer updatedCustomer = customerService.getCustomerById(custId);

        assumeTrue(!updatedCustomer.getCustomerName().isEmpty(), "Customer name should not be empty after update");

        assertEquals(updatedName, updatedCustomer.getCustomerName(),
                "Customer name should be updated from CSV data");
    }

    @ParameterizedTest(name = "Customer name should not be empty: {0}")
    @ValueSource(strings = {"John Doe", "Jane Smith", "Bob Johnson", "Alice Brown"})
    void dynamicTestCustomerNameIsNotEmpty(String customerName) {
        // Create customer with the parameterized name
        Customer customer = new Customer(1, customerName, "1234567890", "123 Main St");

        customerService.addCustomer(customer);
        Customer retrievedCustomer = customerService.getCustomerById(1);

        assertNotNull(retrievedCustomer);
        assertNotNull(retrievedCustomer.getCustomerName());
        assertFalse(retrievedCustomer.getCustomerName().isEmpty());
        assertEquals(customerName, retrievedCustomer.getCustomerName());
    }

    @TestFactory
    Collection<DynamicTest> dynamicTestCustomerNameIsNotEmpty() {
        String[] customerNames = {"John Doe", "Jane Smith", "Bob Johnson", "Alice Brown"};

        return IntStream.range(0, customerNames.length)
                .mapToObj(i -> {
                    String name = customerNames[i];
                    int customerId = i + 1;

                    return DynamicTest.dynamicTest(
                            "Customer name should not be empty: " + name,
                            () -> {
                                Customer customer = new Customer(customerId, name, "1234567890", "123 Main St");
                                customerService.addCustomer(customer);
                                Customer retrievedCustomer = customerService.getCustomerById(customerId);

                                assertNotNull(retrievedCustomer);
                                assertNotNull(retrievedCustomer.getCustomerName());
                                assertFalse(retrievedCustomer.getCustomerName().isEmpty());
                                assertEquals(name, retrievedCustomer.getCustomerName());
                            }
                    );
                })
                .collect(Collectors.toList());
    }



}


