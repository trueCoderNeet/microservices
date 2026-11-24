package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private AccountsRepository accountRepo;
    private CustomerRepository customerRepo;


    @Override
    public void createAccount(CustomerDto cutsomerDto) {
        Customer customer = CustomerMapper.mapToCustomer(cutsomerDto,new Customer());
        Optional<Customer> optionalCustomer= customerRepo.findByMobileNumber(customer.getMobileNumber());
        if(optionalCustomer.isPresent()){
            throw new CustomerAlreadyExistsException("Customer already exists with the mobile number.."+cutsomerDto.getMobileNumber());
        }

        Customer savedCustomer = customerRepo.save(customer);
        accountRepo.save(createNewAccounts(savedCustomer));


    }

    @Override
    public CustomerDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepo.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
        );
        Accounts accounts = accountRepo.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account","customerId",customer.getCustomerId().toString())
        );
        CustomerDto customerDto=CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
        customerDto.setAccountsDto(AccountMapper.mapToAccountDto(accounts,new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateCustomerDetails(CustomerDto cutsomerDto) {
        boolean isUpdate=false;
        AccountsDto accountsDto= cutsomerDto.getAccountsDto();
        if(accountsDto!=null){
            Accounts accounts = accountRepo.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException   ("Account","accountNumber",accountsDto.getAccountNumber().toString())
            );

        AccountMapper.mapToAccount(accountsDto,accounts);
        accounts = accountRepo.save(accounts);

        Long customerId = accounts.getCustomerId();
        Customer customer = customerRepo.findById(customerId).orElseThrow(
                () -> new ResourceNotFoundException("Customer","custmerId",customerId.toString())
        );
        CustomerMapper.mapToCustomer(cutsomerDto,customer);
        customerRepo.save(customer);
        isUpdate=true;
        }
        return isUpdate;
    }



    private Accounts createNewAccounts(Customer customer){
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccountNumber = 100000000L+new Random().nextInt(900000000);
        newAccount.setAccountNumber(randomAccountNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);

        return newAccount;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {

        Customer customer = customerRepo.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
        );
        accountRepo.deleteByCustomerId(customer.getCustomerId());
        customerRepo.deleteById(customer.getCustomerId());
        return true;
    }

}
