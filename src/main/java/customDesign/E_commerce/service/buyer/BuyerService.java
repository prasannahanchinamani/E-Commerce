package customDesign.E_commerce.service.buyer;

import customDesign.E_commerce.dto.AddressRequest;
import customDesign.E_commerce.entity.Address;
import customDesign.E_commerce.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BuyerService {

    private final AddressRepository addressRepository;

    public BuyerService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAddresses(String email) {
        return addressRepository.findByUserEmail(email);
    }

    @Transactional
    public Address addAddress(String email, AddressRequest req) {
        Address address = new Address();
        address.setUserEmail(email);
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        address.setDefault(req.isDefault());
        if (req.isDefault()) {
            addressRepository.findByUserEmail(email).forEach(existing -> {
                if (existing.isDefault()) {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                }
            });
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(String email, Long id, AddressRequest req) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Address not found"));
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        if (req.isDefault()) {
            addressRepository.findByUserEmail(email).forEach(existing -> {
                if (!existing.getId().equals(id) && existing.isDefault()) {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                }
            });
        }
        address.setDefault(req.isDefault());
        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(String email, Long id) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Address not found"));
        addressRepository.delete(address);
    }
}