package Repository;

import models.Customer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    //region fields
    private List<Customer> _customers;
    //endregion

    //region constructors
    public CustomerRepository() {
        this._customers = new ArrayList<Customer>();
    }

    public CustomerRepository(Connection dbConnection) {
        this._customers = new ArrayList<Customer>();
    }
    //endregion

    //region methods

    public void addCustomer(Customer customer) {
        this._customers.add(customer);
    }

    public Customer getCustomerById(int custId) {
        for(Customer customer : _customers){
            if(customer.getCustId() == custId){
                return customer;
            }
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        return this._customers;
    }

    public void removeCustomer(int custId) {
        Customer customerToRemove = null;
        for(Customer customer : _customers){
            if(customer.getCustId() == custId){
                customerToRemove = customer;
                break;
            }
        }
        if(customerToRemove != null){
            _customers.remove(customerToRemove);
        }
    }

    public void updateCustomerName(int custId, String updatedCustomerName) {
        for(int i = 0; i < _customers.size(); i++){
            if(_customers.get(i).getCustId() == custId){
                _customers.get(i).setCustomerName(updatedCustomerName);
                break;
            }
        }
    }

    //endregion
}
