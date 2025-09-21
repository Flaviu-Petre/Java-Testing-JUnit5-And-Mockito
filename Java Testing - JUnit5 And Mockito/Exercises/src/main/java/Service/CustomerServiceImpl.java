package Service;

import Interface.CustomerService;
import Repository.CustomerRepository;
import models.Customer;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {
    //region fields
    private CustomerRepository customerRepository;
    //endregion

    //region constructors
    public CustomerServiceImpl() {
        this.customerRepository = new CustomerRepository();
    }

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    //endregion

    @Override
    public void addCustomer(Customer customer) {
        customerRepository.addCustomer(customer);
    }

    @Override
    public Customer getCustomerById(int custId) {
        return customerRepository.getCustomerById(custId);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.getAllCustomers();
    }

    @Override
    public void removeCustomer(int custId) {
        customerRepository.removeCustomer(custId);
    }

    @Override
    public void updateCustomerName(int custId, String updatedCustomerName) {
        customerRepository.updateCustomerName(custId, updatedCustomerName);
    }
    //endregion




}
