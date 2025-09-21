package Interface;

import models.Customer;

import java.util.List;

public interface CustomerService {
    void addCustomer(Customer customer);
    Customer getCustomerById(int custId);
    List<Customer> getAllCustomers();
    void removeCustomer(int custId);
    void updateCustomerName(int custId, String updatedCustomerName);
}
