package com.eazybytes.accounts.service;

import com.eazybytes.accounts.dto.CustomerDto;

public interface IAccountsService {

    /**
     * This method is used to save an Accounts object to the DB
     * @param cutsomerDto
     */
    void createAccount(CustomerDto cutsomerDto);
    CustomerDto fetchCustomerDetails(String mobileNumber);
    boolean updateCustomerDetails(CustomerDto cutsomerDto);
    boolean deleteAccount(String mobileNumber);
}
