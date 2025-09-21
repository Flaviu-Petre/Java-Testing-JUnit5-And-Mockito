package models;

public class Customer {
    //region fields
    private int _custId;
    private String _customerName;
    private String _contactNumber;
    private String _address;
    //endregion

    //region constructors
    public Customer(int custId, String customerName, String contactNumber, String address) {
        _custId = custId;
        _customerName = customerName;
        _contactNumber = contactNumber;
        _address = address;
    }

    public Customer() {
        _custId = 0;
        _customerName = "";
        _contactNumber = "";
        _address = "";
    }

    public Customer(Customer customer) {
        this._custId = customer._custId;
        this._customerName = customer._customerName;
        this._contactNumber = customer._contactNumber;
        this._address = customer._address;
    }
    //endregion

    //region getters

    public int getCustId() {
        return _custId;
    }

    public String getCustomerName() {
        return _customerName;
    }

    public String getContactNumber() {
        return _contactNumber;
    }

    public String getAddress() {
        return _address;
    }

    //endregion

    //region setters

    public void setCustId(int custId) {
        this._custId = custId;
    }

    public void setCustomerName(String customerName) {
        this._customerName = customerName;
    }

    public void setContactNumber(String contactNumber) {
        this._contactNumber = contactNumber;
    }

    public void setAddress(String address) {
        this._address = address;
    }

    //endregion

    @Override
    public String toString() {
        return "Customer {" +
                "custId = " + _custId +
                ", customerName = '" + _customerName + '\'' +
                ", contactNumber = '" + _contactNumber + '\'' +
                ", address = '" + _address + '\'' +
                '}';
    }

}
