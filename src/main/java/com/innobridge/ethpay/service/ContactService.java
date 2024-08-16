package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.Contacts;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import static com.innobridge.ethpay.Utility.getAuthentication;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserService userService;

    public Contacts getByEmail() {
        User user = userService.getById(getAuthentication().getId()).get();
        return contactRepository.findByEmail(user.getEmail()).orElseGet(() -> new Contacts(user.getEmail(), new HashSet<>()));
    }

    public Contacts addContact(String email) {
        User user = userService.getByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Contacts contacts = getByEmail();
        contacts.getContacts().add(user.getEmail());
        contactRepository.save(contacts);
        return contacts;
    }

    public Contacts removeContact(String email) {
        User user = userService.getByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Contacts contacts = getByEmail();
        contacts.getContacts().remove(user.getEmail());
        contactRepository.save(contacts);
        return contacts;
    }
}
