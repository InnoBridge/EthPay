package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.Contacts;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserService userService;

    public Contacts getById(String id) {
        User user = userService.getById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return contactRepository.findByEmail(user.getEmail()).orElseGet(() -> new Contacts(user.getEmail(), new HashSet<>()));
    }

    public Contacts addContact(String id, String email) {
        User user = userService.getByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Contacts contacts = getById(id);
        contacts.getContacts().add(user.getEmail());
        contactRepository.save(contacts);
        return contacts;
    }

    public Contacts removeContact(String id, String email) {
        User user = userService.getByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Contacts contacts = getById(id);
        contacts.getContacts().remove(user.getEmail());
        contactRepository.save(contacts);
        return contacts;
    }
}
