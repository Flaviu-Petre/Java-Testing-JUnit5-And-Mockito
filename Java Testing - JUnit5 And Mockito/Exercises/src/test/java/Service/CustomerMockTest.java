package Service;

import Interface.CustomerService;
import Repository.CustomerRepository;
import models.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerMockTest {

    @Mock
    private CustomerRepository mockRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Connection realDbConnection;
    private CustomerRepository realRepository;
    private CustomerService realService;

    @BeforeEach
    void setUp() throws SQLException {
        setupRealDatabase();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (realDbConnection != null && !realDbConnection.isClosed()) {
            dropTestTable();
            realDbConnection.close();
        }
    }

    private void setupRealDatabase() throws SQLException {
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        String username = "sa";
        String password = "";

        try {
            Class.forName("org.h2.Driver");
            realDbConnection = DriverManager.getConnection(jdbcUrl, username, password);
            createTestTable();
            realRepository = new CustomerRepository(realDbConnection);
            realService = new CustomerServiceImpl(realRepository);
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 Driver not found", e);
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

        try (Statement stmt = realDbConnection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
        }
    }

    private void dropTestTable() throws SQLException {
        String dropTableSQL = "DROP TABLE IF EXISTS customers";
        try (Statement stmt = realDbConnection.createStatement()) {
            stmt.executeUpdate(dropTableSQL);
        }
    }

    @Test
    void addNewCustomerWithMockito() {
        Customer customer = new Customer(1, "John Doe", "1234567890", "123 Main St");

        doNothing().when(mockRepository).addCustomer(any(Customer.class));
        when(mockRepository.getCustomerById(1)).thenReturn(customer);

        customerService.addCustomer(customer);
        Customer fetchedCustomer = customerService.getCustomerById(1);

        assertNotNull(fetchedCustomer);
        assertEquals("John Doe", fetchedCustomer.getCustomerName());
        assertEquals("1234567890", fetchedCustomer.getContactNumber());
        assertEquals("123 Main St", fetchedCustomer.getAddress());

        verify(mockRepository).addCustomer(customer);
        verify(mockRepository).getCustomerById(1);
    }

    @Test
    void getCustomerByIdWithMockito() {
        Customer customer = new Customer(2, "Jane Smith", "0987654321", "456 Elm St");
        when(mockRepository.getCustomerById(2)).thenReturn(customer);

        Customer fetchedCustomer = customerService.getCustomerById(2);

        assertNotNull(fetchedCustomer);
        assertEquals("Jane Smith", fetchedCustomer.getCustomerName());
        assertEquals("0987654321", fetchedCustomer.getContactNumber());
        assertEquals("456 Elm St", fetchedCustomer.getAddress());

        verify(mockRepository).getCustomerById(2);
    }

    @Test
    void databaseConnectionFailureTest() throws SQLException {
        when(mockRepository.getCustomerById(anyInt()))
                .thenThrow(new SQLException("Database connection failed"));

        SQLException thrownException = assertThrows(SQLException.class, () -> {
            customerService.getCustomerById(1);
        });

        assertNotNull(thrownException);
        assertEquals("Database connection failed", thrownException.getMessage());

        verify(mockRepository).getCustomerById(1);
    }



    @Test
    void testMethod() {
        List mockedList = Mockito.mock(List.class);
        Mockito.when(mockedList.get(Mockito.anyInt())).thenReturn("element");

        System.out.print(mockedList.get(0));
        System.out.print(mockedList.get(1));
        System.out.print(mockedList.get(2));
    }


}